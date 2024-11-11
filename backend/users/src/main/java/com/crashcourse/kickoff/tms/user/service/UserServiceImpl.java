package com.crashcourse.kickoff.tms.user.service;

import java.util.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.host.service.HostProfileService;
import com.crashcourse.kickoff.tms.player.service.PlayerProfileService;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.Role;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final HostProfileService hostProfileService;
    private final PlayerProfileService playerProfileService;
    private final BCryptPasswordEncoder encoder;

    /**
     * Retrieve all users.
     *
     * @return List of User entities.
     */
    public List<User> getUsers() {
        return users.findAll();
    }

    /**
     * Register a new user.
     *
     * @param newUserDTO DTO containing new user data.
     * @return The created User entity.
     * @throws IllegalArgumentException If the username or email is already registered, or if the role is invalid.
     */
    @Transactional
    @Override
    public User addUser(NewUserDTO newUserDTO) {
        if (users.findByUsername(newUserDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("An account with the username " + newUserDTO.getUsername() + " has been registered!");
        }
        if (users.findByEmail(newUserDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with the email " + newUserDTO.getEmail() + " has been registered!");
        }


        User newUser = new User();
        newUser.setUsername(newUserDTO.getUsername());
        newUser.setPassword(encoder.encode(newUserDTO.getPassword()));
        newUser.setEmail(newUserDTO.getEmail());
        
        Role newUserRole;
        try {
            newUserRole = Role.valueOf("ROLE_" + newUserDTO.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newUserDTO.getRole());
        }
        newUser.setRoles(new HashSet<>(Arrays.asList(newUserRole)));

        newUser = users.save(newUser);
        // Build the entire object graph before saving
        switch (newUserRole) {
            case Role.ROLE_PLAYER:
                playerProfileService.addPlayerProfile(newUser, newUserDTO);
                break;
            case Role.ROLE_HOST:
                hostProfileService.addHostProfile(newUser);
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + newUserDTO.getRole());
        }

        // Save the user along with the associated profile
        return users.save(newUser);
    }

    /**
     * Add a HostProfile to an existing user.
     *
     * @param user The User entity to update.
     * @return The updated User entity.
     */
    @Transactional
    @Override
    public User addHostProfileToUser(User user) {
        User loadedUser = getUserById(user.getId());
        hostProfileService.addHostProfile(loadedUser);
        return users.save(loadedUser);
    }

    /**
     * Load a user by their username.
     *
     * @param userName Username of the user.
     * @return The User entity if found, or null.
     */
    @Transactional
    @Override
    public User loadUserByUsername(String userName) {
        Optional<User> user = users.findByUsername(userName);
        if (user.isPresent()) {
            return user.get();
        }

        return null;
    }

    /**
     * Retrieve a user by their ID.
     *
     * @param userId ID of the user.
     * @return The User entity if found, or null.
     */
    @Transactional
    @Override
    public User getUserById(Long userId) {
        return users.findById(userId).orElse(null);  
    }

    /**
     * Save a User entity to database.
     *
     * @param user The User entity to save.
     * @return The saved User entity.
     */
    @Transactional
    public User save(User user) {
        return users.save(user);  // Save the user and persist changes to the database
    }

    /**
     * Delete a user by their ID.
     *
     * @param userId ID of the user to delete.
     */
    @Transactional
    public void deleteUserById(Long userId) {
        users.deleteById(userId);
    }

    /**
     * Add roles to an existing user.
     *
     * @param user  The User entity to update.
     * @param roles Set of roles to assign to the user.
     * @return The updated User entity.
     */
    @Transactional
    public User addRolesToUser(User user, Set<Role> roles) {
        User loadedUser = getUserById(user.getId());
        loadedUser.setRoles(roles);
        return users.save(loadedUser);
    }
    
    /**
     * Set the profile picture URL for a user.
     *
     * @param userId            ID of the user.
     * @param profilePictureUrl URL of the new profile picture.
     * @return The updated User entity.
     */
    @Transactional
    public User setUserProfilePicture(Long userId, String profilePictureUrl) {
        User loadedUser = getUserById(userId);
        loadedUser.setProfilePictureUrl(profilePictureUrl);
        return users.save(loadedUser);
    }
}
