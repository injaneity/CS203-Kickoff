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

@Service
public class UserServiceImpl implements UserService {
    private UserRepository users;
    private HostProfileService hostProfileService;
    private PlayerProfileService playerProfileService;
    private BCryptPasswordEncoder encoder;

    public UserServiceImpl(UserRepository users, HostProfileService hostProfileService,
            PlayerProfileService playerProfileService, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.hostProfileService = hostProfileService;
        this.playerProfileService = playerProfileService;
        this.encoder = encoder;
    }

    public List<User> getUsers() {
        return users.findAll();
    }

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
        newUser.setRoles(new HashSet<Role>(Arrays.asList(newUserRole)));

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

    @Transactional
    @Override
    public User addHostProfileToUser(User user) {
        User loadedUser = getUserById(user.getId());
        hostProfileService.addHostProfile(loadedUser);
        return users.save(loadedUser);
    }

    @Transactional
    @Override
    public User loadUserByUsername(String userName) {
        if (users.findByUsername(userName).isPresent()) {
            return users.findByUsername(userName).get();
        }

        return null;
    }

    @Transactional
    @Override
    public User getUserById(Long userId) {
        return users.findById(userId).orElse(null);  
    }

    @Transactional
    public User save(User user) {
        return users.save(user);  // Save the user and persist changes to the database
    }

    @Transactional
    public void deleteUserById(Long userId) {
        users.deleteById(userId);
    }

    @Transactional
    public User addRolesToUser(User user, Set<Role> roles) {
        User loadedUser = getUserById(user.getId());
        loadedUser.setRoles(roles);
        return users.save(loadedUser);
    }

    @Transactional
    public User setUserProfilePicture(Long userId, String profilePictureUrl) {
        User loadedUser = getUserById(userId);
        loadedUser.setProfilePictureUrl(profilePictureUrl);
        return users.save(loadedUser);
    }
}
