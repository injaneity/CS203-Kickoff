package com.crashcourse.kickoff.tms.player.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crashcourse.kickoff.tms.exception.PlayerNotFoundException;
import com.crashcourse.kickoff.tms.player.model.PlayerPosition;
import com.crashcourse.kickoff.tms.player.model.PlayerProfile;
import com.crashcourse.kickoff.tms.player.model.PlayerStatus;
import com.crashcourse.kickoff.tms.player.dto.PlayerProfileUpdateDTO;
import com.crashcourse.kickoff.tms.player.respository.PlayerProfileRepository;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service("playerProfileService")
@RequiredArgsConstructor
public class PlayerProfileServiceImpl implements PlayerProfileService {
    private final PlayerProfileRepository playerProfiles;

    /**
     * Retrieve all Player Profiles.
     *
     * @return List of PlayerProfile entities.
     */
    @Override
    public List<PlayerProfile> getPlayerProfiles() {
        return playerProfiles.findAll();
    }

    /**
     * Retrieve a Player Profile by its ID.
     *
     * @param playerProfileId ID of the PlayerProfile.
     * @return The PlayerProfile entity if found, or null.
     */
    @Override
    public PlayerProfile getPlayerProfile(Long playerProfileId) {
        return playerProfiles.findById(playerProfileId).orElse(null);
    }

    // not currently used ("deprecated")
    // @Override
    // public PlayerProfile updatePlayerPosition(Long playerProfileId,
    // PlayerPosition preferredPosition) {
    // Optional<PlayerProfile> userOpt = playerProfiles.findById(playerProfileId);
    // if (userOpt.isPresent()) {
    // PlayerProfile playerProfile = userOpt.get();
    // if (playerProfile != null) {
    // playerProfile.setPreferredPositions(Collections.singletonList(preferredPosition));
    // // outdated method to
    // // set to only one
    // // position
    // playerProfiles.save(playerProfile); // Save the user and the updated profile
    // return playerProfile;
    // } else {
    // throw new IllegalArgumentException("Player profile not found for user with id
    // " + playerProfileId);
    // }
    // } else {
    // throw new IllegalArgumentException("User not found with id " +
    // playerProfileId);
    // }
    // }

    /**
     * Converts an array of preferred position strings to a list of PlayerPosition enums.
     *
     * @param preferredPositionArray Array of preferred position strings.
     * @return List of PlayerPosition enums.
     */
    private List<PlayerPosition> getListOfPreferredPosition(String[] preferredPositionArray) {
        return Arrays.stream(preferredPositionArray)
                .map(position -> PlayerPosition.valueOf(position.toUpperCase()))
                .collect(Collectors.toList());
    }
    /**
     * Checks if the username in the claim matches the profile ID in the request variable.
     *
     * @param profileId ID of the PlayerProfile.
     * @param username  Username to verify ownership.
     * @return True if the username matches the profile's user, false otherwise.
     */
    public boolean isOwner(Long profileId, String username) {
        Optional<PlayerProfile> playerProfileOpt = playerProfiles.findById(profileId);
        if (playerProfileOpt.isPresent()) {
            PlayerProfile playerProfile = playerProfileOpt.get();
            return playerProfile.getUser().getUsername().equals(username);
        }
        return false;
    }

    /**
     * Update a Player Profile with new data.
     *
     * @param playerProfile            The existing PlayerProfile entity.
     * @param playerProfileUpdateDTO   DTO containing updated player profile data.
     * @return The updated PlayerProfile entity.
     */
    @Transactional
    @Override
    public PlayerProfile updatePlayerProfile(PlayerProfile playerProfile,
            PlayerProfileUpdateDTO playerProfileUpdateDTO) {
        List<PlayerPosition> preferredPositions = getListOfPreferredPosition(
                playerProfileUpdateDTO.getPreferredPositions());
        playerProfile.setPreferredPositions(preferredPositions);
        playerProfile.setProfileDescription(playerProfileUpdateDTO.getProfileDescription());
        return playerProfiles.save(playerProfile);
    }

    /**
     * Add a new Player Profile for a user.
     *
     * @param newUser     The User entity associated with the new profile.
     * @param newUserDTO  DTO containing new user data.
     * @return The created PlayerProfile entity.
     */
    @Transactional
    @Override
    public PlayerProfile addPlayerProfile(User newUser, NewUserDTO newUserDTO) {
        PlayerProfile newPlayerProfile = new PlayerProfile();
        List<PlayerPosition> preferredPositions = getListOfPreferredPosition(newUserDTO.getPreferredPositions());
        newPlayerProfile.setPreferredPositions(preferredPositions);
        newPlayerProfile.setUser(newUser);
        return playerProfiles.save(newPlayerProfile);
    }

    /**
     * Update the status of a Player Profile.
     *
     * @param playerId ID of the player.
     * @param status   New status to set.
     * @throws PlayerNotFoundException If the player profile is not found.
     */
    @Override
    @Transactional
    public void updatePlayerStatus(Long playerId, PlayerStatus status) throws PlayerNotFoundException {
        PlayerProfile playerProfile = playerProfiles.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

        playerProfile.setStatus(status);
        playerProfiles.save(playerProfile);
    }
}
