package com.crashcourse.kickoff.tms.club;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crashcourse.kickoff.tms.club.dto.ClubRatingUpdateDTO;
import com.crashcourse.kickoff.tms.club.dto.PlayerApplicationDTO;
import com.crashcourse.kickoff.tms.club.exception.ClubAlreadyExistsException;
import com.crashcourse.kickoff.tms.club.exception.ClubNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PenaltyNotFoundException;
import com.crashcourse.kickoff.tms.club.exception.PlayerLimitExceededException;
import com.crashcourse.kickoff.tms.club.model.ApplicationStatus;
import com.crashcourse.kickoff.tms.club.model.Club;
import com.crashcourse.kickoff.tms.club.model.ClubInvitation;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus;
import com.crashcourse.kickoff.tms.club.model.ClubPenaltyStatus.PenaltyType;
import com.crashcourse.kickoff.tms.club.model.ClubProfile;
import com.crashcourse.kickoff.tms.club.model.PlayerApplication;
import com.crashcourse.kickoff.tms.club.repository.ClubInvitationRepository;
import com.crashcourse.kickoff.tms.club.repository.ClubRepository;
import com.crashcourse.kickoff.tms.club.repository.PlayerApplicationRepository;
import com.crashcourse.kickoff.tms.club.service.ClubServiceImpl;
import com.crashcourse.kickoff.tms.player.PlayerPosition;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PlayerApplicationRepository applicationRepository;

    @Mock
    private ClubInvitationRepository clubInvitationRepository;

    @InjectMocks
    private ClubServiceImpl clubService;

    // ================== createClub ==================
    @Test
    void createClub_ValidClub_CreatedSuccessfully() throws Exception {
        // Arrange
        Club club = new Club();
        club.setName("Unique Club Name");
        club.setPlayers(new ArrayList<>());
        club.setId(1L);

        when(clubRepository.findByName("Unique Club Name")).thenReturn(Optional.empty());
        when(clubRepository.save(any(Club.class))).thenReturn(club);
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

        // Act
        Club createdClub = clubService.createClub(club, 1L);

        // Assert
        assertNotNull(createdClub);
        assertEquals("Unique Club Name", createdClub.getName());
        assertEquals(1L, createdClub.getCaptainId());
        verify(clubRepository, times(3)).save(any(Club.class)); // Called 3x in createClub cuz addPlayerToClub also saves
    }

    @Test
    void createClub_DuplicateName_ThrowsException() {
        // Arrange
        Club club = new Club();
        club.setName("Existing Club Name");

        when(clubRepository.findByName("Existing Club Name")).thenReturn(Optional.of(new Club()));

        // Act
        try {
            clubService.createClub(club, 1L);
            fail("Expected ClubAlreadyExistsException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubAlreadyExistsException);
            assertEquals("Club name must be unique", e.getMessage());
        }
    }

    // ================== getClubById ==================
    @Test
    void getClubById_ExistingId_ReturnsClub() {
        // Arrange
        Club club = new Club();
        club.setId(1L);
        club.setName("Test Club");

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

        // Act
        Optional<Club> result = clubService.getClubById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Club", result.get().getName());
    }

    @Test
    void getClubById_NonExistingId_ThrowsException() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        try {
            clubService.getClubById(1L);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID 1 not found.", e.getMessage());
        }
    }




    // ================== deleteClub ==================
    @Test
    void deleteClub_ExistingId_DeletesClubSuccessfully() {
        // Arrange
        Long clubId = 1L;
        when(clubRepository.existsById(clubId)).thenReturn(true);

        // Act
        try {
            clubService.deleteClub(clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        verify(clubRepository, times(1)).deleteById(clubId);
    }

    @Test
    void deleteClub_NonExistingId_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        when(clubRepository.existsById(clubId)).thenReturn(false);

        // Act
        try {
            clubService.deleteClub(clubId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }

        // Verify that deleteById is not called
        verify(clubRepository, times(0)).deleteById(anyLong());
    }

    // ================== getAllClubs ==================
    @Test
    void getAllClubs_ClubsExist_ReturnsListOfClubs() {
        // Arrange
        Club club1 = new Club();
        club1.setId(1L);
        club1.setName("Club One");

        Club club2 = new Club();
        club2.setId(2L);
        club2.setName("Club Two");

        List<Club> clubList = Arrays.asList(club1, club2);

        when(clubRepository.findAll()).thenReturn(clubList);

        // Act
        List<Club> result = clubService.getAllClubs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Club One", result.get(0).getName());
        assertEquals("Club Two", result.get(1).getName());
        verify(clubRepository, times(1)).findAll();
    }

    @Test
    void getAllClubs_NoClubsExist_ReturnsEmptyList() {
        // Arrange
        when(clubRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Club> result = clubService.getAllClubs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clubRepository, times(1)).findAll();
    }

    // ================== addPlayerToClub ==================
    @Test
    void addPlayerToClub_ValidClubAndPlayer_PlayerAddedSuccessfully() throws Exception {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;

        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(new ArrayList<>());

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        // Act
        Club updatedClub = null;
        try {
            updatedClub = clubService.addPlayerToClub(clubId, playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(updatedClub);
        assertTrue(updatedClub.getPlayers().contains(playerId));
        verify(clubRepository, times(1)).save(club);
    }

    @Test
    void addPlayerToClub_NonExistentClub_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act
        try {
            clubService.addPlayerToClub(clubId, playerId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }

        // Verify that save is not called
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void addPlayerToClub_ClubFull_ThrowsPlayerLimitExceededException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;

        Club club = new Club();
        club.setId(clubId);
        List<Long> players = new ArrayList<>();
        for (int i = 0; i < Club.MAX_PLAYERS_IN_CLUB; i++) {
            players.add((long) i);
        }
        club.setPlayers(players);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        try {
            clubService.addPlayerToClub(clubId, playerId);
            fail("Expected PlayerLimitExceededException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof PlayerLimitExceededException);
            assertEquals(String.format("A club cannot have more than %d players.", Club.MAX_PLAYERS_IN_CLUB), e.getMessage());
        }

        // Verify that save is not called
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void addPlayerToClub_PlayerAlreadyMember_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;

        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(new ArrayList<>(Arrays.asList(playerId)));

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        try {
            clubService.addPlayerToClub(clubId, playerId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("Player is already a member of this club", e.getMessage());
        }

        // Verify that save is not called
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    // ================== getPlayers ==================
    @Test
    void getPlayers_ExistingClubId_ReturnsListOfPlayerIds() {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        List<Long> players = Arrays.asList(100L, 101L, 102L);
        club.setPlayers(players);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        List<Long> result = null;
        try {
            result = clubService.getPlayers(clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(players, result);
        verify(clubRepository, times(1)).findById(clubId);
    }

    @Test
    void getPlayers_NonExistentClubId_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act
        try {
            clubService.getPlayers(clubId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }

        // Verify that findById was called
        verify(clubRepository, times(1)).findById(clubId);
    }

    // ================== removePlayerFromClub ==================
    @Test
    void removePlayerFromClub_ValidClubAndPlayer_PlayerRemovedSuccessfully() throws Exception {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        List<Long> players = new ArrayList<>(Arrays.asList(100L, 101L, 102L));
        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(players);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        // Act
        Club updatedClub = null;
        try {
            updatedClub = clubService.removePlayerFromClub(clubId, playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(updatedClub);
        assertFalse(updatedClub.getPlayers().contains(playerId));
        assertEquals(2, updatedClub.getPlayers().size());
        verify(clubRepository, times(1)).save(club);
    }

    @Test
    void removePlayerFromClub_NonExistentClubId_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act
        try {
            clubService.removePlayerFromClub(clubId, playerId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }

        // Verify that save was not called
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void removePlayerFromClub_PlayerNotInClub_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        List<Long> players = new ArrayList<>(Arrays.asList(101L, 102L));
        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(players);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        try {
            clubService.removePlayerFromClub(clubId, playerId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("Player is not a member of this club", e.getMessage());
        }

        // Verify that save was not called
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    // ================== getClubByPlayerId ==================
    @Test
    void getClubByPlayerId_PlayerInClub_ReturnsClub() {
        // Arrange
        Long playerId = 100L;
        Club club = new Club();
        club.setId(1L);
        club.setName("Test Club");
        club.setPlayers(Arrays.asList(playerId));

        when(clubRepository.findClubByPlayerId(playerId)).thenReturn(Optional.of(club));

        // Act
        Optional<Club> result = null;
        try {
            result = clubService.getClubByPlayerId(playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("Test Club", result.get().getName());
        verify(clubRepository, times(1)).findClubByPlayerId(playerId);
    }

    @Test
    void getClubByPlayerId_PlayerNotInAnyClub_ReturnsEmptyOptional() {
        // Arrange
        Long playerId = 100L;
    
        when(clubRepository.findClubByPlayerId(playerId)).thenReturn(Optional.empty());
    
        // Act
        Optional<Club> result = null;
        try {
            result = clubService.getClubByPlayerId(playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(clubRepository, times(1)).findClubByPlayerId(playerId);
    }

    // ================== isCaptain ==================
    @Test
    void isCaptain_PlayerIsCaptain_ReturnsTrue() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(playerId);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        boolean result = clubService.isCaptain(clubId, playerId);
    
        // Assert
        assertTrue(result);
        verify(clubRepository, times(1)).findById(clubId);
    }

    @Test
    void isCaptain_PlayerIsNotCaptain_ReturnsFalse() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(101L); // Different captain
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        boolean result = clubService.isCaptain(clubId, playerId);
    
        // Assert
        assertFalse(result);
        verify(clubRepository, times(1)).findById(clubId);
    }

    @Test
    void isCaptain_ClubDoesNotExist_ReturnsFalse() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        boolean result = clubService.isCaptain(clubId, playerId);
    
        // Assert
        assertFalse(result);
        verify(clubRepository, times(1)).findById(clubId);
    }

    // ================== invitePlayerToClub ==================
    @Test
    void invitePlayerToClub_ValidCaptainAndClub_InvitationCreated() throws Exception {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Long captainId = 200L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(captainId);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubInvitationRepository.save(any(ClubInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    
        // Act
        Club resultClub = null;
        try {
            resultClub = clubService.invitePlayerToClub(clubId, playerId, captainId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(resultClub);
        assertEquals(clubId, resultClub.getId());
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubInvitationRepository, times(1)).save(any(ClubInvitation.class));
    }

    @Test
    void invitePlayerToClub_ClubDoesNotExist_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Long captainId = 200L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.invitePlayerToClub(clubId, playerId, captainId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubInvitationRepository, times(0)).save(any(ClubInvitation.class));
    }

    @Test
    void invitePlayerToClub_UserNotCaptain_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Long captainId = 200L; // User attempting to invite
        Long actualCaptainId = 300L; // Actual captain
    
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(actualCaptainId);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        try {
            clubService.invitePlayerToClub(clubId, playerId, captainId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("Only the club captain can invite players.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubInvitationRepository, times(0)).save(any(ClubInvitation.class));
    }

    // ================== acceptinvite ==================
    @Test
    void acceptInvite_ValidClubAndPlayer_PlayerAddedToClub() throws Exception {
        // Arrange
        Long playerId = 100L;
        Long clubId = 1L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(new ArrayList<>());
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);
    
        // Act
        Club resultClub = null;
        try {
            resultClub = clubService.acceptInvite(playerId, clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(resultClub);
        assertTrue(resultClub.getPlayers().contains(playerId));
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(1)).save(club);
    }

    @Test
    void acceptInvite_ClubDoesNotExist_ThrowsException() {
        // Arrange
        Long playerId = 100L;
        Long clubId = 1L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.acceptInvite(playerId, clubId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void acceptInvite_ClubAtMaxCapacity_ThrowsPlayerLimitExceededException() {
        // Arrange
        Long playerId = 100L;
        Long clubId = 1L;
    
        Club club = new Club();
        club.setId(clubId);
        List<Long> players = new ArrayList<>();
        for (int i = 0; i < Club.MAX_PLAYERS_IN_CLUB; i++) {
            players.add((long) i);
        }
        club.setPlayers(players);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        try {
            clubService.acceptInvite(playerId, clubId);
            fail("Expected PlayerLimitExceededException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof PlayerLimitExceededException);
            assertEquals(
                String.format("A club cannot have more than %d players.", Club.MAX_PLAYERS_IN_CLUB),
                e.getMessage()
            );
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    // ================== getPlayerInvitations ==================
    @Test
    void getPlayerInvitations_PlayerHasInvitations_ReturnsListOfInvitations() {
        // Arrange
        Long playerId = 100L;
        ClubInvitation invitation1 = new ClubInvitation();
        invitation1.setId(1L);
        invitation1.setPlayerId(playerId);
        invitation1.setStatus(ApplicationStatus.PENDING);

        ClubInvitation invitation2 = new ClubInvitation();
        invitation2.setId(2L);
        invitation2.setPlayerId(playerId);
        invitation2.setStatus(ApplicationStatus.PENDING);

        List<ClubInvitation> invitations = Arrays.asList(invitation1, invitation2);

        when(clubInvitationRepository.findByPlayerIdAndStatus(playerId, ApplicationStatus.PENDING))
                .thenReturn(invitations);

        // Act
        List<ClubInvitation> result = null;
        try {
            result = clubService.getPlayerInvitations(playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(invitations, result);
        verify(clubInvitationRepository, times(1))
                .findByPlayerIdAndStatus(playerId, ApplicationStatus.PENDING);
    }

    @Test
    void getPlayerInvitations_PlayerHasNoInvitations_ReturnsEmptyList() {
        // Arrange
        Long playerId = 100L;

        when(clubInvitationRepository.findByPlayerIdAndStatus(playerId, ApplicationStatus.PENDING))
                .thenReturn(new ArrayList<>());

        // Act
        List<ClubInvitation> result = null;
        try {
            result = clubService.getPlayerInvitations(playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clubInvitationRepository, times(1))
                .findByPlayerIdAndStatus(playerId, ApplicationStatus.PENDING);
    }

    // ================== applyToClub ==================
    @Test
    void applyToClub_Success_ApplicationSaved() {
        // Arrange
        Club club = new Club();
        club.setId(1L);
        club.setPlayers(new ArrayList<>());

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(applicationRepository.existsByPlayerIdAndClub(1L, club)).thenReturn(false);

        PlayerApplicationDTO applicationDTO = new PlayerApplicationDTO();
        applicationDTO.setClubId(1L);
        applicationDTO.setPlayerId(1L);
        applicationDTO.setDesiredPosition(PlayerPosition.POSITION_FORWARD);

        // Act
        clubService.applyToClub(applicationDTO);

        // Assert
        verify(applicationRepository, times(1)).save(any(PlayerApplication.class));
    }

    @Test
    void applyToClub_PlayerAlreadyApplied_ApplicationSaved() {
        // Arrange
        Club club = new Club();
        club.setId(1L);
        club.setPlayers(new ArrayList<>());

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(applicationRepository.existsByPlayerIdAndClub(1L, club)).thenReturn(true);

        PlayerApplicationDTO applicationDTO = new PlayerApplicationDTO();
        applicationDTO.setClubId(1L);
        applicationDTO.setPlayerId(1L);

        // Act
        clubService.applyToClub(applicationDTO);

        //Assert
        verify(applicationRepository, times(1)).save(any(PlayerApplication.class));
    }

    // ================== getPlayerApplications ==================
    @Test
    void getPlayerApplications_ClubExistsWithApplicants_ReturnsPlayerIds() {
        // Arrange
        Long clubId = 1L;
        Long applicationId1 = 10L;
        Long applicationId2 = 20L;
        Long playerId1 = 100L;
        Long playerId2 = 200L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setApplicants(Arrays.asList(applicationId1, applicationId2));
    
        PlayerApplication application1 = new PlayerApplication();
        application1.setId(applicationId1);
        application1.setPlayerId(playerId1);
    
        PlayerApplication application2 = new PlayerApplication();
        application2.setId(applicationId2);
        application2.setPlayerId(playerId2);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(applicationRepository.findById(applicationId1)).thenReturn(Optional.of(application1));
        when(applicationRepository.findById(applicationId2)).thenReturn(Optional.of(application2));
    
        // Act
        List<Long> result = null;
        try {
            result = clubService.getPlayerApplications(clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(playerId1));
        assertTrue(result.contains(playerId2));
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(1)).findById(applicationId1);
        verify(applicationRepository, times(1)).findById(applicationId2);
    }

    @Test
    void getPlayerApplications_ClubExistsNoApplicants_ReturnsEmptyList() {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        club.setApplicants(new ArrayList<>());
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        List<Long> result = null;
        try {
            result = clubService.getPlayerApplications(clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(0)).findById(anyLong());
    }

    @Test
    void getPlayerApplications_ClubDoesNotExist_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.getPlayerApplications(clubId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(0)).findById(anyLong());
    }

    // ================== acceptApplication ==================
    @Test
    void acceptApplication_ValidClubAndApplication_PlayerAddedToClub() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Long applicationId = 10L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setPlayers(new ArrayList<>());
        club.setApplicants(new ArrayList<>(Arrays.asList(applicationId)));
    
        PlayerApplication playerApplication = new PlayerApplication();
        playerApplication.setId(applicationId);
        playerApplication.setPlayerId(playerId);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(applicationRepository.findByClubIdAndPlayerId(clubId, playerId)).thenReturn(playerApplication);
        when(clubRepository.save(any(Club.class))).thenReturn(club);
    
        // Act
        try {
            clubService.acceptApplication(clubId, playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertTrue(club.getPlayers().contains(playerId));
        assertFalse(club.getApplicants().contains(applicationId));
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(1)).findByClubIdAndPlayerId(clubId, playerId);
        verify(clubRepository, times(1)).save(club);
        verify(applicationRepository, times(1)).deleteAllByPlayerId(playerId);
    }

    @Test
    void acceptApplication_ClubDoesNotExist_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.acceptApplication(clubId, playerId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(0)).findByClubIdAndPlayerId(anyLong(), anyLong());
        verify(clubRepository, times(0)).save(any(Club.class));
        verify(applicationRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void acceptApplication_ApplicationDoesNotExist_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setApplicants(new ArrayList<>());
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(applicationRepository.findByClubIdAndPlayerId(clubId, playerId)).thenReturn(null);
    
        // Act
        try {
            clubService.acceptApplication(clubId, playerId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof NullPointerException);
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(1)).findByClubIdAndPlayerId(clubId, playerId);
        verify(clubRepository, times(0)).save(any(Club.class));
        verify(applicationRepository, times(0)).deleteById(anyLong());
    }

    // ================== rejectApplication ==================
    @Test
    void rejectApplication_ValidClubAndApplication_ApplicationRejected() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
        Long applicationId = 10L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setApplicants(new ArrayList<>(Arrays.asList(applicationId)));
    
        PlayerApplication playerApplication = new PlayerApplication();
        playerApplication.setId(applicationId);
        playerApplication.setPlayerId(playerId);
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(applicationRepository.findByClubIdAndPlayerId(clubId, playerId)).thenReturn(playerApplication);
        when(clubRepository.save(any(Club.class))).thenReturn(club);
    
        // Act
        try {
            clubService.rejectApplication(clubId, playerId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertFalse(club.getApplicants().contains(applicationId));
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(1)).findByClubIdAndPlayerId(clubId, playerId);
        verify(clubRepository, times(1)).save(club);
    }

    @Test
    void rejectApplication_ClubDoesNotExist_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.rejectApplication(clubId, playerId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(0)).findByClubIdAndPlayerId(anyLong(), anyLong());
        verify(clubRepository, times(0)).save(any(Club.class));
        verify(applicationRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void rejectApplication_ApplicationDoesNotExist_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long playerId = 100L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setApplicants(new ArrayList<>());
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(applicationRepository.findByClubIdAndPlayerId(clubId, playerId)).thenReturn(null);
    
        // Act
        try {
            clubService.rejectApplication(clubId, playerId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof NullPointerException);
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(applicationRepository, times(1)).findByClubIdAndPlayerId(clubId, playerId);
        verify(clubRepository, times(0)).save(any(Club.class));
        verify(applicationRepository, times(0)).deleteById(anyLong());
    }

    // transferCaptaincy is the most annoying one... but not used yet anyway
    @Test
    void transferCaptaincy_ValidInputs_CaptainTransferred() throws Exception {
        // Arrange
        Long clubId = 1L;
        Long currentCaptainId = 100L;
        Long newCaptainId = 200L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(currentCaptainId);
        club.setPlayers(new ArrayList<>(Arrays.asList(currentCaptainId, newCaptainId)));
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);
    
        // Act
        Club updatedClub = null;
        try {
            updatedClub = clubService.transferCaptaincy(clubId, currentCaptainId, newCaptainId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    
        // Assert
        assertNotNull(updatedClub);
        assertEquals(newCaptainId, updatedClub.getCaptainId());
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(1)).save(club);
    }

    @Test
    void transferCaptaincy_ClubDoesNotExist_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Long currentCaptainId = 100L;
        Long newCaptainId = 200L;
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());
    
        // Act
        try {
            clubService.transferCaptaincy(clubId, currentCaptainId, newCaptainId);
            fail("Expected ClubNotFoundException to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e instanceof ClubNotFoundException);
            assertEquals("Club with ID " + clubId + " not found.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void transferCaptaincy_UserNotCurrentCaptain_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long currentCaptainId = 100L; // The user attempting the transfer
        Long actualCaptainId = 150L;  // The actual captain
        Long newCaptainId = 200L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(actualCaptainId);
        club.setPlayers(new ArrayList<>(Arrays.asList(actualCaptainId, newCaptainId)));
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        try {
            clubService.transferCaptaincy(clubId, currentCaptainId, newCaptainId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("Only the current captain can transfer the captaincy.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    @Test
    void transferCaptaincy_NewCaptainNotInClub_ThrowsException() {
        // Arrange
        Long clubId = 1L;
        Long currentCaptainId = 100L;
        Long newCaptainId = 200L;
    
        Club club = new Club();
        club.setId(clubId);
        club.setCaptainId(currentCaptainId);
        club.setPlayers(new ArrayList<>(Arrays.asList(currentCaptainId)));
    
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    
        // Act
        try {
            clubService.transferCaptaincy(clubId, currentCaptainId, newCaptainId);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertEquals("The new captain must be a player in the club.", e.getMessage());
        }
    
        verify(clubRepository, times(1)).findById(clubId);
        verify(clubRepository, times(0)).save(any(Club.class));
    }

    // ================== updateClubRating ==================
    @Test
    void testUpdateClubRating_Success_EloUpdatedCorrectly() {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        club.setElo(1500);
        club.setRatingDeviation(200);

        ClubRatingUpdateDTO ratingUpdateDTO = new ClubRatingUpdateDTO();
        ratingUpdateDTO.setRating(1600);
        ratingUpdateDTO.setRatingDeviation(180);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        clubService.updateClubRating(clubId, ratingUpdateDTO);

        // Assert
        assertEquals(1600, club.getElo());
        assertEquals(180, club.getRatingDeviation());
        verify(clubRepository).save(club);
    }

    @Test
    void testUpdateClubRating_ClubNotFound_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        ClubRatingUpdateDTO ratingUpdateDTO = new ClubRatingUpdateDTO();
        ratingUpdateDTO.setRating(1600);
        ratingUpdateDTO.setRatingDeviation(180);

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ClubNotFoundException.class, () -> {
            clubService.updateClubRating(clubId, ratingUpdateDTO);
        });

        assertEquals("Club with ID " + clubId + " not found.", exception.getMessage());
    }

    // ================== updateClubPenaltyStatus ==================
    @Test
    void testUpdateClubPenaltyStatus_Success_ClubCorrectlyBlacklisted() throws Exception {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        ClubPenaltyStatus currentStatus = new ClubPenaltyStatus();
        currentStatus.setPenaltyType(PenaltyType.NONE);
        club.setPenaltyStatus(currentStatus);

        ClubPenaltyStatus newStatus = new ClubPenaltyStatus();
        newStatus.setPenaltyType(PenaltyType.BLACKLISTED);
        newStatus.setBanUntil(LocalDateTime.now().plusDays(7));

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        ClubProfile updatedProfile = clubService.updateClubPenaltyStatus(clubId, newStatus);

        // Assert
        assertEquals(PenaltyType.BLACKLISTED, club.getPenaltyStatus().getPenaltyType());
        verify(clubRepository).save(club);
        assertNotNull(updatedProfile);
    }

    /**
     * Test for updating club penalty status when club is not found.
     */
    @Test
    void testUpdateClubPenaltyStatus_ClubNotFound_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;
        ClubPenaltyStatus newStatus = new ClubPenaltyStatus();
        newStatus.setPenaltyType(PenaltyType.BLACKLISTED);
        newStatus.setBanUntil(LocalDateTime.now().plusDays(7));

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ClubNotFoundException.class, () -> {
            clubService.updateClubPenaltyStatus(clubId, newStatus);
        });

        assertEquals("Club with ID " + clubId + " not found.", exception.getMessage());
    }

    /**
     * Test for updating club penalty status when penalty is not found.
     */
    @Test
    void testUpdateClubPenaltyStatus_PenaltyNotFound_PenaltyNotFoundException() {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        ClubPenaltyStatus currentStatus = mock(ClubPenaltyStatus.class);
        club.setPenaltyStatus(currentStatus);

        ClubPenaltyStatus newStatus = new ClubPenaltyStatus();
        newStatus.setPenaltyType(PenaltyType.BLACKLISTED);
        newStatus.setBanUntil(LocalDateTime.now().plusDays(7));

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        doThrow(new PenaltyNotFoundException("Penalty not found")).when(currentStatus).applyPenalty(newStatus);

        // Act & Assert
        Exception exception = assertThrows(PenaltyNotFoundException.class, () -> {
            clubService.updateClubPenaltyStatus(clubId, newStatus);
        });

        assertEquals("Penalty not found", exception.getMessage());
    }

    // ================== getPenaltyStatusByClubId ==================
    @Test
    void testGetPenaltyStatusByClubId_Success_ReturnsCorrectPenaltyStatus() throws Exception {
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        ClubPenaltyStatus penaltyStatus = new ClubPenaltyStatus();
        penaltyStatus.setPenaltyType(PenaltyType.BLACKLISTED);
        penaltyStatus.setBanUntil(LocalDateTime.now().plusDays(7));
        club.setPenaltyStatus(penaltyStatus);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        // Act
        ClubPenaltyStatus result = clubService.getPenaltyStatusByClubId(clubId);

        // Assert
        assertEquals(penaltyStatus, result);
    }

    @Test
    void testGetPenaltyStatusByClubId_ClubNotFound_ThrowsClubNotFoundException() {
        // Arrange
        Long clubId = 1L;

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ClubNotFoundException.class, () -> {
            clubService.getPenaltyStatusByClubId(clubId);
        });

        assertEquals("Club with ID " + clubId + " not found.", exception.getMessage());
    }
}