package com.crashcourse.kickoff.tms.user.controller;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.crashcourse.kickoff.tms.security.JwtUtil;
import com.crashcourse.kickoff.tms.security.JwtAuthService;
import com.crashcourse.kickoff.tms.user.dto.LoginDetails;
import com.crashcourse.kickoff.tms.user.dto.LoginResponseDTO;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.Role;
import com.crashcourse.kickoff.tms.user.dto.UserResponseDTO;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.service.UserService;

import lombok.RequiredArgsConstructor;

import com.crashcourse.kickoff.tms.client.AmazonClient;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtAuthService jwtAuthService;
    private final AmazonClient amazonClient;

    /**
     * Retrieve all users.
     *
     * @return List of User objects.
     */
    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }
    
    /**
     * Retrieve a user by their ID.
     *
     * @param user_id ID of the user to retrieve.
     * @param token   Authorization token from the request header.
     * @return ResponseEntity containing the User entity or an error message.
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<?> getUserById(@PathVariable Long user_id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {
        // Validate token and authorization
        ResponseEntity<String> authResponse = jwtAuthService.validateToken(token, user_id);
        if (authResponse != null)
            return authResponse;

        try {
            User foundUser = userService.getUserById(user_id);
            if (foundUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User with ID " + user_id + " not found.");
            }

            return ResponseEntity.ok(foundUser);
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error fetching user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    /**
     * Retrieve public information of all users.
     *
     * @param token Authorization token from the request header (optional).
     * @return ResponseEntity containing a list of UserResponseDTO or an error message.
     */
    @GetMapping("/publicinfo/all")
    public ResponseEntity<?> getAllUsersPublicInfo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        try {
            List<User> users = userService.getUsers();
            
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No users found.");
            }

            List<UserResponseDTO> userDTOs = users.stream()
                    .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getProfilePictureUrl()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    /**
     * Retrieve public information of a user by their ID.
     *
     * @param user_id ID of the user.
     * @param token   Authorization token from the request header (optional).
     * @return ResponseEntity containing UserResponseDTO or an error message.
     */
    @GetMapping("/publicinfo/{user_id}")
    public ResponseEntity<?> getUserPublicInfoById(
            @PathVariable Long user_id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        try {
            User foundUser = userService.getUserById(user_id);
            if (foundUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User with ID " + user_id + " not found.");
            }

            UserResponseDTO userDTO = new UserResponseDTO(
                    foundUser.getId(),
                    foundUser.getUsername(),
                    foundUser.getProfilePictureUrl());

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error fetching user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    /**
     * Using BCrypt encoder to encrypt the password for storage
     * 
     * @param user
     * @return
     */
    @PostMapping
    public ResponseEntity<?> signup(@RequestBody NewUserDTO newUserDTO) {
        try {
            User newUser = userService.addUser(newUserDTO);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a user by their ID.
     *
     * @param idToDelete ID of the user to delete.
     * @param token      Authorization token from the request header.
     * @return ResponseEntity with a success message or an error message.
     */
    @DeleteMapping("/{idToDelete}")
    public ResponseEntity<String> delete(@PathVariable Long idToDelete,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) {

        // Validate token and authorization
        ResponseEntity<String> authResponse = jwtAuthService.validateToken(token, idToDelete);
        if (authResponse != null)
            return authResponse;

        if (userService.getUserById(idToDelete) != null) {
            userService.deleteUserById(idToDelete);
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    /**
     * Authenticate a user and provide a JWT token.
     *
     * @param loginDetails DTO containing username and password.
     * @return ResponseEntity containing LoginResponseDTO with userId, JWT token, and admin status.
     * @throws Exception If authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDetails loginDetails) throws Exception {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDetails.getUsername(), loginDetails.getPassword()));
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid username or password", e);
        }

        // Load user details and generate JWT token
        final User user = userService.loadUserByUsername(loginDetails.getUsername());
        final String jwt = jwtUtil.generateToken(user);

        // Assuming User has a getId() method to retrieve userId
        Long userId = user.getId();

        boolean isAdmin = user.getRoles().contains(Role.ROLE_ADMIN);

        // Return both userId and jwtToken in the response
        LoginResponseDTO loginResponse = new LoginResponseDTO(userId, jwt, isAdmin);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Set the profile picture URL for a user.
     *
     * @param user_id  ID of the user.
     * @param imageUrl URL of the new profile picture.
     * @param token    Authorization token from the request header.
     * @return ResponseEntity with a success message or an error message.
     * @throws Exception If an error occurs during the update.
     */
    @PostMapping(value = "/{user_id}/profilePicture", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> setProfilePicture(@PathVariable Long user_id,
        @RequestBody String imageUrl,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) throws Exception {
        
        // CHANGE LATER, SHOULD CHECK LOGGED IN USER CORRESPONDS TO USER_ID
        ResponseEntity<String> authResponse = jwtAuthService.validateToken(token, user_id);
        if (authResponse != null)
            return authResponse;
        userService.setUserProfilePicture(user_id, imageUrl);
        return ResponseEntity.ok("Updated Profile Picture"); 
    }

    /**
     * Upload a profile picture for a user.
     *
     * @param user_id        ID of the user.
     * @param profilePicture Base64-encoded image data.
     * @param token          Authorization token from the request header.
     * @return ResponseEntity containing the image URL or an error message.
     * @throws Exception If an error occurs during the upload.
     */
    @PostMapping(value = "/{user_id}/upload", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> upload(@PathVariable Long user_id,
        @RequestBody String profilePicture,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String token) throws Exception {
        // CHANGE LATER, SHOULD CHECK LOGGED IN USER CORRESPONDS TO USER_ID
        ResponseEntity<String> authResponse = jwtAuthService.validateToken(token, user_id);
        if (authResponse != null)
            return authResponse;

        try {
            // Use the `confirmationUrl` from `verificationData`
            String[] parts = profilePicture.split(",");
            byte[] data = Base64.getDecoder().decode(parts[1]); // Skip the data URI prefix
            MultipartFile file = new MockMultipartFile("file", user_id + "-profilePicture.jpg", "image/jpeg", data);
            String imageUrl = this.amazonClient.uploadFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
