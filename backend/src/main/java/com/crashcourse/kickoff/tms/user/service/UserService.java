package com.crashcourse.kickoff.tms.user.service;

import java.util.List;

import com.crashcourse.kickoff.tms.player.PlayerPosition;
import com.crashcourse.kickoff.tms.player.PlayerProfile;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.User;

// Business logic of UserService, actual implementation in UserServiceImpl
public interface UserService {
    public List<User> getUsers();
    User addUser(NewUserDTO newUserDTO);
    PlayerProfile addPlayerProfile(Long userId, PlayerProfile playerProfile);
}
