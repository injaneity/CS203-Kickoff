package com.crashcourse.kickoff.tms.host.service;

import java.util.List;
import java.util.Optional;

import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.host.*;

public interface HostProfileService {
    List<HostProfile> getHostProfiles();
    HostProfile addHostProfile(User newUser);
    Optional<HostProfile> getHostProfileByID(Long id);
}
