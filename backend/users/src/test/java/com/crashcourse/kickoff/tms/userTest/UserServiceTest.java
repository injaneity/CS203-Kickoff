package com.crashcourse.kickoff.tms.userTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import com.crashcourse.kickoff.tms.user.service.UserServiceImpl;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.repository.UserRepository;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.crashcourse.kickoff.tms.host.service.*;
import com.crashcourse.kickoff.tms.player.service.PlayerProfileService;

public class UserServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private HostProfileService hostProfileService;

    @Mock
    private PlayerProfileService playerProfileService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    // ============= getUsers=================
    @Test
    void getUsers_UsersExist_ReturnsListOfUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
    
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
    
        List<User> userList = Arrays.asList(user1, user2);
    
        when(users.findAll()).thenReturn(userList);
    
        // Act
        List<User> result = userService.getUsers();
    
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(users, times(1)).findAll();
    }

    @Test
    void getUsers_NoUsersExist_ReturnsEmptyList() {
        // Arrange
        when(users.findAll()).thenReturn(new ArrayList<>());
    
        // Act
        List<User> result = userService.getUsers();
    
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(users, times(1)).findAll();
    }

    // ============= addUser=================
    @Test
    void addUser_ValidPlayerUser_UserAddedSuccessfully() {
        // Arrange
        String[] positions = new String[] {"Forward", "Midfielder"}; // Example positions
        NewUserDTO newUserDTO = new NewUserDTO(
            "player1",
            "password",
            "player1@example.com",
            positions,
            "player"
        );
    
        User newUser = new User();
        newUser.setId(1L);
        newUser.setUsername("player1");
        newUser.setPassword("encodedPassword");
        newUser.setEmail("player1@example.com");
        newUser.setRoles(new HashSet<Role>(Arrays.asList(Role.ROLE_PLAYER)));
    
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(users.save(any(User.class))).thenReturn(newUser);
    
        // Act
        User resultUser = null;
        try {
            resultUser = userService.addUser(newUserDTO);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(resultUser);
        assertEquals("player1", resultUser.getUsername());
        assertEquals("encodedPassword", resultUser.getPassword());
        assertEquals("player1@example.com", resultUser.getEmail());
        assertTrue(resultUser.getRoles().contains(Role.ROLE_PLAYER));
    
        verify(users, times(2)).save(any(User.class)); // Saved twice
        verify(playerProfileService, times(1)).addPlayerProfile(any(User.class), eq(newUserDTO));
        verify(hostProfileService, times(0)).addHostProfile(any(User.class));
    }

    @Test
    void addUser_ValidHostUser_UserAddedSuccessfully() {
        // Arrange
        String[] positions = new String[0]; // Hosts may not have positions
        NewUserDTO newUserDTO = new NewUserDTO(
            "host1",
            "password",
            "host1@example.com",
            positions,
            "host"
        );

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("host1");
        newUser.setPassword("encodedPassword");
        newUser.setEmail("host1@example.com");
        newUser.setRoles(new HashSet<Role>(Arrays.asList(Role.ROLE_HOST)));

        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(users.save(any(User.class))).thenReturn(newUser);

        // Act
        User resultUser = null;
        try {
            resultUser = userService.addUser(newUserDTO);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(resultUser);
        assertEquals("host1", resultUser.getUsername());
        assertEquals("encodedPassword", resultUser.getPassword());
        assertEquals("host1@example.com", resultUser.getEmail());
        assertTrue(resultUser.getRoles().contains(Role.ROLE_HOST));

        verify(users, times(2)).save(any(User.class)); // Saved twice
        verify(hostProfileService, times(1)).addHostProfile(any(User.class));
        verify(playerProfileService, times(0)).addPlayerProfile(any(User.class), any(NewUserDTO.class));
    }

    @Test
    void addUser_InvalidRole_ThrowsIllegalArgumentException() {
        // Arrange
        String[] positions = new String[0];
        NewUserDTO newUserDTO = new NewUserDTO(
            "user1",
            "password",
            "user1@example.com",
            positions,
            "invalidRole"
        );
    
        when(encoder.encode("password")).thenReturn("encodedPassword");
    
        // Act
        try {
            userService.addUser(newUserDTO);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("Invalid role: invalidRole", e.getMessage());
        }
    
        // Since the exception occurs before users.save(), it should not be called
        verify(users, times(0)).save(any(User.class));
        verify(playerProfileService, times(0)).addPlayerProfile(any(User.class), any(NewUserDTO.class));
        verify(hostProfileService, times(0)).addHostProfile(any(User.class));
    }

    // ============= loadUserByUsername=================
    @Test
    void loadUserByUsername_UserExists_ReturnsUser() {
        // Arrange
        String username = "user1";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
    
        when(users.findByUsername(username)).thenReturn(Optional.of(user));
    
        // Act
        User result = null;
        try {
            result = userService.loadUserByUsername(username);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(users, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ReturnsNull() {
        // Arrange
        String username = "nonexistentUser";
    
        when(users.findByUsername(username)).thenReturn(Optional.empty());
    
        // Act
        User result = null;
        try {
            result = userService.loadUserByUsername(username);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNull(result);
        verify(users, times(1)).findByUsername(username);
    }

    // ============= getUserById=================
    @Test
    void getUserById_UserExists_ReturnsUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("user1");
    
        when(users.findById(userId)).thenReturn(Optional.of(user));
    
        // Act
        User result = null;
        try {
            result = userService.getUserById(userId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(users, times(1)).findById(userId);
    }

    @Test
    void getUserById_UserDoesNotExist_ReturnsNull() {
        // Arrange
        Long userId = 999L;
    
        when(users.findById(userId)).thenReturn(Optional.empty());
    
        // Act
        User result = null;
        try {
            result = userService.getUserById(userId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNull(result);
        verify(users, times(1)).findById(userId);
    }

    // ============= save=================
    @Test
    void save_ValidUser_UserSavedSuccessfully() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
    
        when(users.save(user)).thenReturn(user); // note this is repo save
    
        // Act
        User result = null;
        try {
            result = userService.save(user); // note this is service save
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertEquals("user1", result.getUsername());
        verify(users, times(1)).save(user);
    }

    // ================= deleteUserById =================
    @Test
    void deleteUserById_UserExists_UserDeletedSuccessfully() {
        // Arrange
        Long userId = 1L;

        doNothing().when(users).deleteById(userId);

        // Act
        try {
            userService.deleteUserById(userId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        verify(users, times(1)).deleteById(userId);
    }

    @Test
    void deleteUserById_UserDoesNotExist_NoExceptionThrown() {
        // Arrange
        Long userId = 999L;

        doNothing().when(users).deleteById(userId);

        // Act
        try {
            userService.deleteUserById(userId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        verify(users, times(1)).deleteById(userId);
    }

    // ================= addRolesToUser =================
    @Test
    void addRolesToUser_UserExists_RolesAddedSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("user1");

        Set<Role> roles = new HashSet<>(Arrays.asList(Role.ROLE_PLAYER, Role.ROLE_HOST));

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);

        // Act
        User result = null;
        try {
            result = userService.addRolesToUser(user, roles);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(roles, result.getRoles());
        verify(users, times(1)).findById(userId);
        verify(users, times(1)).save(user);
    }

    @Test
    void addRolesToUser_UserDoesNotExist_ThrowsException() {
        // Arrange
        Long userId = 999L;
        User user = new User();
        user.setId(userId);

        Set<Role> roles = new HashSet<>(Arrays.asList(Role.ROLE_PLAYER));

        when(users.findById(userId)).thenReturn(Optional.empty());

        // Act
        try {
            userService.addRolesToUser(user, roles);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof NullPointerException);
        }

        verify(users, times(1)).findById(userId);
        verify(users, times(0)).save(any(User.class));
    }

    // ================= setUserProfilePicture =================
    @Test
    void setUserProfilePicture_UserExists_ProfilePictureSetSuccessfully() {
        // Arrange
        Long userId = 1L;
        String profilePictureUrl = "http://example.com/profile.jpg";
        User user = new User();
        user.setId(userId);

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);

        // Act
        User result = null;
        try {
            result = userService.setUserProfilePicture(userId, profilePictureUrl);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(profilePictureUrl, result.getProfilePictureUrl());
        verify(users, times(1)).findById(userId);
        verify(users, times(1)).save(user);
    }

    @Test
    void setUserProfilePicture_UserDoesNotExist_ThrowsException() {
        // Arrange
        Long userId = 999L;
        String profilePictureUrl = "http://example.com/profile.jpg";

        when(users.findById(userId)).thenReturn(Optional.empty());

        // Act
        try {
            userService.setUserProfilePicture(userId, profilePictureUrl);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof NullPointerException);
        }

        verify(users, times(1)).findById(userId);
        verify(users, times(0)).save(any(User.class));
    }
}