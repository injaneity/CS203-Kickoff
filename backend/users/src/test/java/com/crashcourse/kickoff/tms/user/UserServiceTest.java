package com.crashcourse.kickoff.tms.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import com.crashcourse.kickoff.tms.user.service.UserServiceImpl;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.repository.UserRepository;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.crashcourse.kickoff.tms.host.service.*;
import com.crashcourse.kickoff.tms.player.service.PlayerProfileService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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
    void addUser_NewPlayerUser_SuccessfullyRegistersPlayer() {
        // Arrange
        NewUserDTO newUserDTO = new NewUserDTO(
            "player1",
            "player1@example.com",
            "password123",
            new String[] { "Forward", "Midfielder" },
            "player"
        );

        when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());
        when(users.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(newUserDTO.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(newUserDTO.getUsername());
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail(newUserDTO.getEmail());
        savedUser.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_PLAYER)));

        when(users.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.addUser(newUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newUserDTO.getUsername(), result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(newUserDTO.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains(Role.ROLE_PLAYER));

        verify(users, times(2)).save(any(User.class));
        verify(playerProfileService, times(1)).addPlayerProfile(savedUser, newUserDTO);
        verify(hostProfileService, never()).addHostProfile(any(User.class));
    }

    @Test
    void addUser_NewHostUser_SuccessfullyRegistersHost() {
        // Arrange
        NewUserDTO newUserDTO = new NewUserDTO(
            "host1",
            "host1@example.com",
            "password123",
            null,
            "host"
        );

        when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());
        when(users.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(newUserDTO.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername(newUserDTO.getUsername());
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail(newUserDTO.getEmail());
        savedUser.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_HOST)));

        when(users.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.addUser(newUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newUserDTO.getUsername(), result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(newUserDTO.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains(Role.ROLE_HOST));

        verify(users, times(2)).save(any(User.class));
        verify(hostProfileService, times(1)).addHostProfile(savedUser);
        verify(playerProfileService, never()).addPlayerProfile(any(User.class), any(NewUserDTO.class));
    }

    // @Test
    // void addUser_NewAdminUser_SuccessfullyRegistersAdmin() {
    //     // Arrange
    //     NewUserDTO newUserDTO = new NewUserDTO(
    //         "admin1",
    //         "admin1@example.com",
    //         "password123",
    //         null,
    //         "admin"
    //     );

    //     when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());
    //     when(users.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());
    //     when(encoder.encode(newUserDTO.getPassword())).thenReturn("encodedPassword");

    //     User savedUser = new User();
    //     savedUser.setId(3L);
    //     savedUser.setUsername(newUserDTO.getUsername());
    //     savedUser.setPassword("encodedPassword");
    //     savedUser.setEmail(newUserDTO.getEmail());
    //     savedUser.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_ADMIN)));

    //     when(users.save(any(User.class))).thenReturn(savedUser);

    //     // Act & Assert
    //     IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
    //         userService.addUser(newUserDTO);
    //     });

    //     assertEquals("Invalid role: admin", exception.getMessage());

    //     verify(users, times(1)).save(any(User.class));
    //     verify(playerProfileService, never()).addPlayerProfile(any(User.class), any(NewUserDTO.class));
    //     verify(hostProfileService, never()).addHostProfile(any(User.class));
    // }

    @Test
    void addUser_UsernameAlreadyExists_ThrowsIllegalArgumentException() {
        // Arrange
        NewUserDTO newUserDTO = new NewUserDTO(
            "existingUser",
            "newemail@example.com",
            "password123",
            null,
            "player"
        );

        User existingUser = new User();
        existingUser.setId(4L);
        existingUser.setUsername("existingUser");
        existingUser.setEmail("existing@example.com");

        when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUser(newUserDTO);
        });

        assertEquals("An account with the username existingUser has been registered!", exception.getMessage());

        verify(users, never()).findByEmail(anyString());
        verify(users, never()).save(any(User.class));
        verify(playerProfileService, never()).addPlayerProfile(any(User.class), any(NewUserDTO.class));
        verify(hostProfileService, never()).addHostProfile(any(User.class));
    }

    @Test
    void addUser_EmailAlreadyExists_ThrowsIllegalArgumentException() {
        // Arrange
        NewUserDTO newUserDTO = new NewUserDTO(
            "newUser",
            "existingemail@example.com",
            "password123",
            null,
            "host"
        );

        when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setId(5L);
        existingUser.setUsername("existingUser");
        existingUser.setEmail("existingemail@example.com");

        when(users.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUser(newUserDTO);
        });

        assertEquals("An account with the email existingemail@example.com has been registered!", exception.getMessage());

        verify(users, times(1)).findByUsername(newUserDTO.getUsername());
        verify(users, times(1)).findByEmail(newUserDTO.getEmail());
        verify(users, never()).save(any(User.class));
        verify(playerProfileService, never()).addPlayerProfile(any(User.class), any(NewUserDTO.class));
        verify(hostProfileService, never()).addHostProfile(any(User.class));
    }

    @Test
    void addUser_InvalidRole_ThrowsIllegalArgumentException() {
        // Arrange
        NewUserDTO newUserDTO = new NewUserDTO(
            "userWithBadRole",
            "user@example.com",
            "password123",
            null,
            "invalidRole"
        );

        when(users.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());
        when(users.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.addUser(newUserDTO);
        });

        assertEquals("Invalid role: invalidRole", exception.getMessage());

        verify(users, times(1)).findByUsername(newUserDTO.getUsername());
        verify(users, times(1)).findByEmail(newUserDTO.getEmail());
        verify(users, never()).save(any(User.class));
        verify(playerProfileService, never()).addPlayerProfile(any(User.class), any(NewUserDTO.class));
        verify(hostProfileService, never()).addHostProfile(any(User.class));
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