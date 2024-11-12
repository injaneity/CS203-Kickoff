package com.crashcourse.kickoff.tms.tournament;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import com.crashcourse.kickoff.tms.tournament.dto.*;
import com.crashcourse.kickoff.tms.tournament.model.*;
import com.crashcourse.kickoff.tms.tournament.repository.*;
import com.crashcourse.kickoff.tms.tournament.service.TournamentServiceImpl;
import com.crashcourse.kickoff.tms.bracket.dto.MatchUpdateDTO;
import com.crashcourse.kickoff.tms.bracket.model.Bracket;
import com.crashcourse.kickoff.tms.bracket.model.Match;
import com.crashcourse.kickoff.tms.bracket.repository.MatchRepository;
import com.crashcourse.kickoff.tms.bracket.service.BracketService;
import com.crashcourse.kickoff.tms.bracket.service.MatchService;
import com.crashcourse.kickoff.tms.client.ClubServiceClient;
import com.crashcourse.kickoff.tms.club.ClubProfile;
import com.crashcourse.kickoff.tms.location.model.Location;
import com.crashcourse.kickoff.tms.location.repository.LocationRepository;
import com.crashcourse.kickoff.tms.security.JwtTokenProvider;
import com.crashcourse.kickoff.tms.security.JwtUtil;

import com.crashcourse.kickoff.tms.tournament.exception.*;
import com.crashcourse.kickoff.tms.client.exception.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PlayerAvailabilityRepository playerAvailabilityRepository;

    @Mock
    private ClubServiceClient clubServiceClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private BracketService bracketService;

    @Mock
    private MatchService matchService;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    // ================= createTournament =================
    @Test
    void createTournament_ValidData_TournamentCreatedSuccessfully() {
        // Arrange
        Long userIdFromToken = 1L;
        Long locationId = 100L;
        String locationName = "Stadium";
        String tournamentName = "Champions League";
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(2);
        Integer maxTeams = 16;
        TournamentFormat tournamentFormat = TournamentFormat.FIVE_SIDE;
        KnockoutFormat knockoutFormat = KnockoutFormat.SINGLE_ELIM;
        List<Float> prizePool = Arrays.asList(1000.0f);
        Integer minRank = 1;
        Integer maxRank = 100;

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Initialize TournamentCreateDTO
        TournamentCreateDTO dto = new TournamentCreateDTO();
        dto.setName(tournamentName);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);
        dto.setLocation(location);
        dto.setMaxTeams(maxTeams);
        dto.setTournamentFormat(tournamentFormat);
        dto.setKnockoutFormat(knockoutFormat);
        dto.setPrizePool(prizePool);
        dto.setMinRank(minRank);
        dto.setMaxRank(maxRank);

        // Initialize Tournament to be saved
        Tournament tournamentToSave = new Tournament();
        tournamentToSave.setName(tournamentName);
        tournamentToSave.setStartDateTime(startDateTime);
        tournamentToSave.setEndDateTime(endDateTime);
        tournamentToSave.setLocation(location);
        tournamentToSave.setMaxTeams(maxTeams);
        tournamentToSave.setTournamentFormat(tournamentFormat);
        tournamentToSave.setKnockoutFormat(knockoutFormat);
        tournamentToSave.setPrizePool(prizePool);
        tournamentToSave.setMinRank(minRank);
        tournamentToSave.setMaxRank(maxRank);
        tournamentToSave.setHost(userIdFromToken);
        tournamentToSave.setJoinedClubIds(new ArrayList<>());
        tournamentToSave.setPlayerAvailabilities(new ArrayList<>());
        tournamentToSave.setVerificationStatus(Tournament.VerificationStatus.AWAITING_PAYMENT);
        tournamentToSave.setVenueBooked(false);

        // Initialize Tournament after save (with ID and other fields)
        Tournament savedTournament = new Tournament();
        savedTournament.setId(10L);
        savedTournament.setName(tournamentName);
        savedTournament.setStartDateTime(startDateTime);
        savedTournament.setEndDateTime(endDateTime);
        savedTournament.setLocation(location);
        savedTournament.setMaxTeams(maxTeams);
        savedTournament.setTournamentFormat(tournamentFormat);
        savedTournament.setKnockoutFormat(knockoutFormat);
        savedTournament.setPrizePool(prizePool);
        savedTournament.setMinRank(minRank);
        savedTournament.setMaxRank(maxRank);
        savedTournament.setHost(userIdFromToken);
        savedTournament.setJoinedClubIds(new ArrayList<>());
        savedTournament.setPlayerAvailabilities(new ArrayList<>());
        savedTournament.setVerificationStatus(Tournament.VerificationStatus.AWAITING_PAYMENT);
        savedTournament.setVenueBooked(false);

        // Mock repository behavior
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(savedTournament);

        // Act
        TournamentResponseDTO result = null;
        try {
            result = tournamentService.createTournament(dto, userIdFromToken);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(savedTournament.getId(), result.getId());
        assertEquals(savedTournament.getName(), result.getName());
        assertEquals(savedTournament.isOver(), result.isOver());
        assertEquals(savedTournament.getStartDateTime(), result.getStartDateTime());
        assertEquals(savedTournament.getEndDateTime(), result.getEndDateTime());
        assertNotNull(result.getLocation());
        assertEquals(locationId, result.getLocation().getId());
        assertEquals(locationName, result.getLocation().getName());
        assertEquals(savedTournament.getMaxTeams(), result.getMaxTeams());
        assertEquals(savedTournament.getTournamentFormat().toString(), result.getTournamentFormat());
        assertEquals(savedTournament.getKnockoutFormat().toString(), result.getKnockoutFormat());
        assertEquals(savedTournament.getPrizePool(), result.getPrizePool());
        assertEquals(savedTournament.getMinRank(), result.getMinRank());
        assertEquals(savedTournament.getMaxRank(), result.getMaxRank());
        assertEquals(savedTournament.getJoinedClubIds(), result.getJoinedClubIds());
        assertEquals(savedTournament.getHost(), result.getHost());
        assertEquals(savedTournament.getVerificationStatus().toString(), result.getVerificationStatus());
        assertEquals(savedTournament.getVenueBooked(), result.isVenueBooked());
        assertEquals(savedTournament.getBracket(), result.getBracket());

        // Verify interactions
        verify(locationRepository, times(1)).findById(locationId);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void createTournament_LocationNotFound_ThrowsLocationNotFoundException() {
        // Arrange
        Long userIdFromToken = 2L;
        Long locationId = 200L;
        String tournamentName = "Europa League";
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(3);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(4);
        Integer maxTeams = 8;
        TournamentFormat tournamentFormat = TournamentFormat.ELEVEN_SIDE;
        KnockoutFormat knockoutFormat = KnockoutFormat.DOUBLE_ELIM;
        List<Float> prizePool = Arrays.asList(2000.0f, 1000.0f);
        Integer minRank = 10;
        Integer maxRank = 80;

        // Initialize Location (which will not be found)
        Location location = new Location();
        location.setId(locationId);
        location.setName("Non-existent Stadium");
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Initialize TournamentCreateDTO
        TournamentCreateDTO dto = new TournamentCreateDTO();
        dto.setName(tournamentName);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);
        dto.setLocation(location);
        dto.setMaxTeams(maxTeams);
        dto.setTournamentFormat(tournamentFormat);
        dto.setKnockoutFormat(knockoutFormat);
        dto.setPrizePool(prizePool);
        dto.setMinRank(minRank);
        dto.setMaxRank(maxRank);

        // Mock repository behavior
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LocationNotFoundException.class, () -> {
            tournamentService.createTournament(dto, userIdFromToken);
        });

        // Verify interactions
        verify(locationRepository, times(1)).findById(locationId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= getTournamentById =================
    @Test
    void getTournamentById_ExistingId_ReturnsTournamentResponseDTO() {
        // Arrange
        Long tournamentId = 1L;
        Long locationId = 100L;
        String locationName = "Stadium";
        String tournamentName = "Champions League";
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(2);
        Integer maxTeams = 16;
        TournamentFormat tournamentFormat = TournamentFormat.FIVE_SIDE;
        KnockoutFormat knockoutFormat = KnockoutFormat.SINGLE_ELIM;
        List<Float> prizePool = Arrays.asList(1000.0f);
        Integer minRank = 1;
        Integer maxRank = 100;

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName(tournamentName);
        tournament.setOver(false);
        tournament.setStartDateTime(startDateTime);
        tournament.setEndDateTime(endDateTime);
        tournament.setLocation(location);
        tournament.setMaxTeams(maxTeams);
        tournament.setTournamentFormat(tournamentFormat);
        tournament.setKnockoutFormat(knockoutFormat);
        tournament.setPrizePool(prizePool);
        tournament.setMinRank(minRank);
        tournament.setMaxRank(maxRank);
        tournament.setHost(1L);
        tournament.setJoinedClubIds(new ArrayList<>());
        tournament.setVerificationStatus(Tournament.VerificationStatus.AWAITING_PAYMENT);
        tournament.setVenueBooked(false);
        tournament.setBracket(null);

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        TournamentResponseDTO result = null;
        try {
            result = tournamentService.getTournamentById(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournament.getId(), result.getId());
        assertEquals(tournament.getName(), result.getName());
        assertEquals(tournament.isOver(), result.isOver());
        assertEquals(tournament.getStartDateTime(), result.getStartDateTime());
        assertEquals(tournament.getEndDateTime(), result.getEndDateTime());
        assertNotNull(result.getLocation());
        assertEquals(locationId, result.getLocation().getId());
        assertEquals(locationName, result.getLocation().getName());
        assertEquals(tournament.getMaxTeams(), result.getMaxTeams());
        assertEquals(tournament.getTournamentFormat().toString(), result.getTournamentFormat());
        assertEquals(tournament.getKnockoutFormat().toString(), result.getKnockoutFormat());
        assertEquals(tournament.getPrizePool(), result.getPrizePool());
        assertEquals(tournament.getMinRank(), result.getMinRank());
        assertEquals(tournament.getMaxRank(), result.getMaxRank());
        assertEquals(tournament.getJoinedClubIds(), result.getJoinedClubIds());
        assertEquals(tournament.getHost(), result.getHost());
        assertEquals(tournament.getVerificationStatus().toString(), result.getVerificationStatus());
        assertEquals(tournament.getVenueBooked(), result.isVenueBooked());
        assertEquals(tournament.getBracket(), result.getBracket());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    @Test
    void getTournamentById_NonExistingId_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.getTournamentById(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    // ================= getAllTournaments =================
    @Test
    void getAllTournaments_NoTournaments_ReturnsEmptyList() {
        // Arrange
        when(tournamentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getAllTournaments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findAll();
    }

    @Test
    void getAllTournaments_WithTournaments_ReturnsListOfTournaments() {
        // Arrange
        Long locationId1 = 100L;
        String locationName1 = "Stadium A";
        Long locationId2 = 101L;
        String locationName2 = "Stadium B";

        // Initialize Locations
        Location location1 = new Location();
        location1.setId(locationId1);
        location1.setName(locationName1);
        location1.setTournaments(new ArrayList<>()); // Initialize tournaments list

        Location location2 = new Location();
        location2.setId(locationId2);
        location2.setName(locationName2);
        location2.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Initialize Tournaments
        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        tournament1.setName("Champions League");
        tournament1.setOver(false);
        tournament1.setStartDateTime(LocalDateTime.now().plusDays(1));
        tournament1.setEndDateTime(LocalDateTime.now().plusDays(2));
        tournament1.setLocation(location1);
        tournament1.setMaxTeams(16);
        tournament1.setTournamentFormat(TournamentFormat.FIVE_SIDE);
        tournament1.setKnockoutFormat(KnockoutFormat.SINGLE_ELIM);
        tournament1.setPrizePool(Arrays.asList(1000.0f));
        tournament1.setMinRank(1);
        tournament1.setMaxRank(100);
        tournament1.setHost(1L);
        tournament1.setJoinedClubIds(new ArrayList<>());
        tournament1.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament1.setVenueBooked(false);
        tournament1.setBracket(null);

        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);
        tournament2.setName("Europa League");
        tournament2.setOver(false);
        tournament2.setStartDateTime(LocalDateTime.now().plusDays(5));
        tournament2.setEndDateTime(LocalDateTime.now().plusDays(6));
        tournament2.setLocation(location2);
        tournament2.setMaxTeams(8);
        tournament2.setTournamentFormat(TournamentFormat.ELEVEN_SIDE);
        tournament2.setKnockoutFormat(KnockoutFormat.DOUBLE_ELIM);
        tournament2.setPrizePool(Arrays.asList(2000.0f, 1000.0f));
        tournament2.setMinRank(10);
        tournament2.setMaxRank(80);
        tournament2.setHost(2L);
        tournament2.setJoinedClubIds(Arrays.asList(201L, 202L));
        tournament2.setVerificationStatus(Tournament.VerificationStatus.PENDING);
        tournament2.setVenueBooked(true);
        tournament2.setBracket(null);

        List<Tournament> tournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findAll()).thenReturn(tournaments);

        // Act
        List<Tournament> result = tournamentService.getAllTournaments();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Champions League", result.get(0).getName());
        assertEquals("Europa League", result.get(1).getName());

        // Verify interactions
        verify(tournamentRepository, times(1)).findAll();
    }

    // ================= updateTournament =================
    @Test
    void updateTournament_ValidData_TournamentUpdatedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;
        Long newLocationId = 100L;
        String newLocationName = "New Stadium";
        TournamentUpdateDTO dto = new TournamentUpdateDTO();
        dto.setName("Updated Champions League");
        dto.setStartDateTime(LocalDateTime.now().plusDays(3));
        dto.setEndDateTime(LocalDateTime.now().plusDays(4));

        // Initialize new Location
        Location newLocation = new Location();
        newLocation.setId(newLocationId);
        newLocation.setName(newLocationName);
        newLocation.setTournaments(new ArrayList<>()); // Initialize tournaments list

        dto.setLocation(newLocation);
        dto.setPrizePool(Arrays.asList(1500.0f, 500.0f));
        dto.setMinRank(5);
        dto.setMaxRank(90);

        // Existing Tournament's current Location
        Long existingLocationId = 101L;
        String existingLocationName = "Old Stadium";
        Location existingLocation = new Location();
        existingLocation.setId(existingLocationId);
        existingLocation.setName(existingLocationName);
        existingLocation.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Existing Tournament
        Tournament existingTournament = new Tournament();
        existingTournament.setId(tournamentId);
        existingTournament.setName("Champions League");
        existingTournament.setStartDateTime(LocalDateTime.now().plusDays(1));
        existingTournament.setEndDateTime(LocalDateTime.now().plusDays(2));
        existingTournament.setLocation(existingLocation);
        existingTournament.setMaxTeams(16);
        existingTournament.setTournamentFormat(TournamentFormat.FIVE_SIDE);
        existingTournament.setKnockoutFormat(KnockoutFormat.SINGLE_ELIM);
        existingTournament.setPrizePool(new ArrayList<>());
        existingTournament.setMinRank(1);
        existingTournament.setMaxRank(100);
        existingTournament.setHost(1L);
        existingTournament.setJoinedClubIds(new ArrayList<>());

        // Updated Tournament after applying DTO
        Tournament updatedTournament = new Tournament();
        updatedTournament.setId(tournamentId);
        updatedTournament.setName(dto.getName());
        updatedTournament.setStartDateTime(dto.getStartDateTime());
        updatedTournament.setEndDateTime(dto.getEndDateTime());
        updatedTournament.setLocation(newLocation);
        updatedTournament.setMaxTeams(existingTournament.getMaxTeams());
        updatedTournament.setTournamentFormat(existingTournament.getTournamentFormat());
        updatedTournament.setKnockoutFormat(existingTournament.getKnockoutFormat());
        updatedTournament.setPrizePool(dto.getPrizePool());
        updatedTournament.setMinRank(dto.getMinRank());
        updatedTournament.setMaxRank(dto.getMaxRank());
        updatedTournament.setHost(existingTournament.getHost());
        updatedTournament.setJoinedClubIds(existingTournament.getJoinedClubIds());

        // Mocking repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(existingTournament));
        when(locationRepository.findById(newLocationId)).thenReturn(Optional.of(newLocation));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(updatedTournament);

        // Act
        TournamentResponseDTO result = null;
        try {
            result = tournamentService.updateTournament(tournamentId, dto);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(updatedTournament.getId(), result.getId());
        assertEquals(updatedTournament.getName(), result.getName());
        assertEquals(updatedTournament.getStartDateTime(), result.getStartDateTime());
        assertEquals(updatedTournament.getEndDateTime(), result.getEndDateTime());
        assertEquals(updatedTournament.getLocation().getId(), result.getLocation().getId());
        assertEquals(updatedTournament.getLocation().getName(), result.getLocation().getName());
        assertEquals(updatedTournament.getPrizePool(), result.getPrizePool());
        assertEquals(updatedTournament.getMinRank(), result.getMinRank());
        assertEquals(updatedTournament.getMaxRank(), result.getMaxRank());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(locationRepository, times(1)).findById(newLocationId);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void updateTournament_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;
        TournamentUpdateDTO dto = new TournamentUpdateDTO();
        dto.setName("Non-existent Tournament");
        dto.setStartDateTime(LocalDateTime.now().plusDays(5));
        dto.setEndDateTime(LocalDateTime.now().plusDays(6));

        // Initialize Location in DTO
        Location location = new Location();
        location.setId(102L);
        location.setName("Another Stadium");
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list
        dto.setLocation(location);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.updateTournament(tournamentId, dto);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(locationRepository, never()).findById(anyLong());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void updateTournament_LocationNotFound_ThrowsLocationNotFoundException() {
        // Arrange
        Long tournamentId = 1L;
        Long newLocationId = 103L;
        TournamentUpdateDTO dto = new TournamentUpdateDTO();
        dto.setName("Champions League Updated");
        dto.setStartDateTime(LocalDateTime.now().plusDays(3));
        dto.setEndDateTime(LocalDateTime.now().plusDays(4));

        // Initialize new Location in DTO
        Location newLocation = new Location();
        newLocation.setId(newLocationId);
        newLocation.setName("Non-existent Stadium");
        newLocation.setTournaments(new ArrayList<>()); // Initialize tournaments list
        dto.setLocation(newLocation);

        // Existing Tournament
        Location existingLocation = new Location();
        existingLocation.setId(101L);
        existingLocation.setName("Old Stadium");
        existingLocation.setTournaments(new ArrayList<>()); // Initialize tournaments list

        Tournament existingTournament = new Tournament();
        existingTournament.setId(tournamentId);
        existingTournament.setName("Champions League");
        existingTournament.setStartDateTime(LocalDateTime.now().plusDays(1));
        existingTournament.setEndDateTime(LocalDateTime.now().plusDays(2));
        existingTournament.setLocation(existingLocation);
        existingTournament.setMaxTeams(16);
        existingTournament.setTournamentFormat(TournamentFormat.FIVE_SIDE);
        existingTournament.setKnockoutFormat(KnockoutFormat.SINGLE_ELIM);
        existingTournament.setPrizePool(new ArrayList<>());
        existingTournament.setMinRank(1);
        existingTournament.setMaxRank(100);
        existingTournament.setHost(1L);
        existingTournament.setJoinedClubIds(new ArrayList<>());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(existingTournament));
        when(locationRepository.findById(newLocationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LocationNotFoundException.class, () -> {
            tournamentService.updateTournament(tournamentId, dto);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(locationRepository, times(1)).findById(newLocationId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= startTournament =================
    @Test
    void startTournament_ValidData_TournamentStartedSuccessfully() {
        // Arrange
        Long tournamentId = 3L;
        String jwtToken = "valid.jwt.token";
        Long locationId = 100L;
        String locationName = "Stadium";

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Existing Tournament with joined clubs and no bracket
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Spring Invitational");
        tournament.setJoinedClubIds(Arrays.asList(201L, 202L));
        tournament.setBracket(null);
        tournament.setLocation(location);
        tournament.setVenueBooked(true);
        tournament.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Mock Bracket creation
        Bracket bracket = new Bracket();
        bracket.setId(301L);
        bracket.setTournament(tournament);

        when(bracketService.createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken)).thenReturn(bracket);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act
        TournamentResponseDTO result = null;
        try {
            result = tournamentService.startTournament(tournamentId, jwtToken);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournament.getId(), result.getId());
        assertEquals(tournament.getName(), result.getName());
        assertEquals(tournament.getStartDateTime(), result.getStartDateTime());
        assertEquals(tournament.getEndDateTime(), result.getEndDateTime());
        assertNotNull(result.getLocation());
        assertEquals(locationId, result.getLocation().getId());
        assertEquals(locationName, result.getLocation().getName());
        assertEquals(tournament.getBracket(), result.getBracket());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, times(1)).createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void startTournament_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 4L;
        String jwtToken = "valid.jwt.token";

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.startTournament(tournamentId, jwtToken);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, never()).createBracket(anyLong(), anyList(), anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void startTournament_NoClubsJoined_ThrowsTournamentHasNoClubsException() {
        // Arrange
        Long tournamentId = 5L;
        String jwtToken = "valid.jwt.token";
        Long locationId = 101L;
        String locationName = "Empty Stadium";

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Existing Tournament with no joined clubs
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Autumn Cup");
        tournament.setJoinedClubIds(new ArrayList<>()); // No clubs
        tournament.setBracket(null);
        tournament.setLocation(location);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act & Assert
        assertThrows(TournamentHasNoClubsException.class, () -> {
            tournamentService.startTournament(tournamentId, jwtToken);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, never()).createBracket(anyLong(), anyList(), anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void startTournament_BracketAlreadyCreated_ThrowsBracketAlreadyCreatedException() {
        // Arrange
        Long tournamentId = 6L;
        String jwtToken = "valid.jwt.token";
        Long locationId = 102L;
        String locationName = "Existing Stadium";

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Existing Tournament with joined clubs and existing bracket
        Bracket existingBracket = new Bracket();
        existingBracket.setId(302L);
        // Link bracket to tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Winter Clash");
        tournament.setJoinedClubIds(Arrays.asList(203L, 204L));
        tournament.setBracket(existingBracket);
        tournament.setLocation(location);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act & Assert
        assertThrows(BracketAlreadyCreatedException.class, () -> {
            tournamentService.startTournament(tournamentId, jwtToken);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, never()).createBracket(anyLong(), anyList(), anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void startTournament_NoBracketAfterCreation_ThrowsRuntimeException() {
        // Arrange
        Long tournamentId = 7L;
        String jwtToken = "valid.jwt.token";
        Long locationId = 103L;
        String locationName = "Faulty Stadium";

        // Initialize Location
        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setTournaments(new ArrayList<>()); // Initialize tournaments list

        // Existing Tournament with joined clubs and no bracket
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Summer Showdown");
        tournament.setJoinedClubIds(Arrays.asList(205L, 206L));
        tournament.setBracket(null);
        tournament.setLocation(location);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(bracketService.createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken)).thenReturn(null);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            tournamentService.startTournament(tournamentId, jwtToken);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, times(1)).createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    // ================= updateMatchInTournament =================
    @Test
    void updateMatchInTournament_ValidData_MatchUpdatedSuccessfully() {
        // Arrange
        Long tournamentId = 10L;
        Long matchId = 1001L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1101L);
        dto.setClub2Id(1102L);
        dto.setWinningClubId(1101L);
        dto.setClub1Score(3);
        dto.setClub2Score(1);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Championship");
        

        Match match = new Match();
        match.setId(matchId);
        match.setClub1Id(1001L);
        match.setClub2Id(1002L);
        match.setWinningClubId(1001L);
        match.setClub1Score(2);
        match.setClub2Score(2);
        

        ClubProfile clubProfile1 = new ClubProfile();
        clubProfile1.setId(1101L);
        clubProfile1.setCaptainId(3001L);
        

        ClubProfile clubProfile2 = new ClubProfile();
        clubProfile2.setId(1102L);
        clubProfile2.setCaptainId(3002L);
        

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(clubServiceClient.getClubProfileById(1101L, jwtToken)).thenReturn(clubProfile1);
        when(clubServiceClient.getClubProfileById(1102L, jwtToken)).thenReturn(clubProfile2);
        when(bracketService.updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class)))
                .thenReturn(match);

        // Act
        Match result = null;
        try {
            result = tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        } catch (Exception e) {
            fail("Exception should not be thrown for valid data");
        }

        // Assert
        assertNotNull(result);
        assertEquals(1101L, result.getWinningClubId());
        assertEquals(3, result.getClub1Score());
        assertEquals(1, result.getClub2Score());
        assertEquals(1101L, result.getClub1Id());
        assertEquals(1102L, result.getClub2Id());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, times(1)).findById(matchId);
        verify(clubServiceClient, times(1)).getClubProfileById(1101L, jwtToken);
        verify(clubServiceClient, times(1)).getClubProfileById(1102L, jwtToken);
        verify(matchService, times(1)).updateElo(dto, jwtToken);
        verify(bracketService, times(1)).updateMatch(tournament, match, dto);
    }

    @Test
    void updateMatchInTournament_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 11L;
        Long matchId = 1002L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1201L);
        dto.setClub2Id(1202L);
        dto.setWinningClubId(1201L);
        dto.setClub1Score(1);
        dto.setClub2Score(0);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, never()).findById(anyLong());
        verify(clubServiceClient, never()).getClubProfileById(anyLong(), anyString());
        verify(matchService, never()).updateElo(any(MatchUpdateDTO.class), anyString());
        verify(bracketService, never()).updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void updateMatchInTournament_MatchNotFound_ThrowsMatchNotFoundException() {
        // Arrange
        Long tournamentId = 12L;
        Long matchId = 1003L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1301L);
        dto.setClub2Id(1302L);
        dto.setWinningClubId(1301L);
        dto.setClub1Score(2);
        dto.setClub2Score(2);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Semi-Final");
        

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MatchNotFoundException.class, () -> {
            tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, times(1)).findById(matchId);
        verify(clubServiceClient, never()).getClubProfileById(anyLong(), anyString());
        verify(matchService, never()).updateElo(any(MatchUpdateDTO.class), anyString());
        verify(bracketService, never()).updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void updateMatchInTournament_Club1ProfileNotFound_ThrowsClubProfileNotFoundException() {
        // Arrange
        Long tournamentId = 13L;
        Long matchId = 1004L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1401L);
        dto.setClub2Id(1402L);
        dto.setWinningClubId(1401L);
        dto.setClub1Score(4);
        dto.setClub2Score(3);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Final");
        

        Match match = new Match();
        match.setId(matchId);
        match.setClub1Id(1301L);
        match.setClub2Id(1302L);
        match.setWinningClubId(1301L);
        match.setClub1Score(3);
        match.setClub2Score(2);
        

        ClubProfile clubProfile1 = null; // Club1 profile not found
        ClubProfile clubProfile2 = new ClubProfile();
        clubProfile2.setId(1402L);
        clubProfile2.setCaptainId(4002L);
        

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(clubServiceClient.getClubProfileById(1401L, jwtToken)).thenReturn(clubProfile1);

        // Act & Assert
        assertThrows(ClubProfileNotFoundAtClientException.class, () -> {
            tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, times(1)).findById(matchId);
        verify(clubServiceClient, times(1)).getClubProfileById(1401L, jwtToken);
        verify(matchService, never()).updateElo(any(MatchUpdateDTO.class), anyString());
        verify(bracketService, never()).updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void updateMatchInTournament_Club2ProfileNotFound_ThrowsClubProfileNotFoundException() {
        // Arrange
        Long tournamentId = 14L;
        Long matchId = 1005L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1501L);
        dto.setClub2Id(1502L);
        dto.setWinningClubId(1501L);
        dto.setClub1Score(1);
        dto.setClub2Score(0);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Quarter-Final");
        

        Match match = new Match();
        match.setId(matchId);
        match.setClub1Id(1401L);
        match.setClub2Id(1402L);
        match.setWinningClubId(1401L);
        match.setClub1Score(2);
        match.setClub2Score(1);
        

        ClubProfile clubProfile1 = new ClubProfile();
        clubProfile1.setId(1501L);
        clubProfile1.setCaptainId(5001L);
        

        ClubProfile clubProfile2 = null; // Club2 profile not found

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(clubServiceClient.getClubProfileById(1501L, jwtToken)).thenReturn(clubProfile1);
        when(clubServiceClient.getClubProfileById(1502L, jwtToken)).thenReturn(clubProfile2);

        // Act & Assert
        assertThrows(ClubProfileNotFoundAtClientException.class, () -> {
            tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, times(1)).findById(matchId);
        verify(clubServiceClient, times(1)).getClubProfileById(1501L, jwtToken);
        verify(clubServiceClient, times(1)).getClubProfileById(1502L, jwtToken);
        verify(matchService, never()).updateElo(any(MatchUpdateDTO.class), anyString());
        verify(bracketService, never()).updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void updateMatchInTournament_InvalidWinningClubId_ThrowsInvalidWinningClubException() {
        // Arrange
        Long tournamentId = 15L;
        Long matchId = 1006L;
        String jwtToken = "valid.jwt.token";

        MatchUpdateDTO dto = new MatchUpdateDTO();
        dto.setClub1Id(1601L);
        dto.setClub2Id(1602L);
        dto.setWinningClubId(9999L); // Invalid winning club ID
        dto.setClub1Score(2);
        dto.setClub2Score(2);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Group Stage");
        

        Match match = new Match();
        match.setId(matchId);
        match.setClub1Id(1501L);
        match.setClub2Id(1502L);
        match.setWinningClubId(1501L);
        match.setClub1Score(1);
        match.setClub2Score(1);
        

        ClubProfile clubProfile1 = new ClubProfile();
        clubProfile1.setId(1601L);
        clubProfile1.setCaptainId(6001L);
        

        ClubProfile clubProfile2 = new ClubProfile();
        clubProfile2.setId(1602L);
        clubProfile2.setCaptainId(6002L);
        

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(clubServiceClient.getClubProfileById(1601L, jwtToken)).thenReturn(clubProfile1);
        when(clubServiceClient.getClubProfileById(1602L, jwtToken)).thenReturn(clubProfile2);

        // Act & Assert
        assertThrows(InvalidWinningClubException.class, () -> {
            tournamentService.updateMatchInTournament(tournamentId, matchId, dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(matchRepository, times(1)).findById(matchId);
        verify(clubServiceClient, times(1)).getClubProfileById(1601L, jwtToken);
        verify(clubServiceClient, times(1)).getClubProfileById(1602L, jwtToken);
        verify(matchService, never()).updateElo(any(MatchUpdateDTO.class), anyString());
        verify(bracketService, never()).updateMatch(any(Tournament.class), any(Match.class), any(MatchUpdateDTO.class));
        verify(matchRepository, never()).save(any(Match.class));
    }

    // ================= deleteTournament =================
    @Test
    void deleteTournament_ExistingId_TournamentDeletedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;

        // Mock repository behavior
        when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
        doNothing().when(tournamentRepository).deleteById(tournamentId);

        // Act
        try {
            tournamentService.deleteTournament(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown for existing tournament ID");
        }

        // Assert
        // No return value to assert, but we can verify interactions
        verify(tournamentRepository, times(1)).existsById(tournamentId);
        verify(tournamentRepository, times(1)).deleteById(tournamentId);
    }

    @Test
    void deleteTournament_NonExistingId_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;

        // Mock repository behavior
        when(tournamentRepository.existsById(tournamentId)).thenReturn(false);

        // Act & Assert
        TournamentNotFoundException exception = assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.deleteTournament(tournamentId);
        });

        assertEquals("Tournament not found with id: " + tournamentId, exception.getMessage());

        // Verify interactions
        verify(tournamentRepository, times(1)).existsById(tournamentId);
        verify(tournamentRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTournament_NullId_ThrowsException() {
        // Arrange
        Long tournamentId = null;
        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.deleteTournament(tournamentId);
        });

        // Verify
        verify(tournamentRepository).existsById(any()); // it is still called once with Null param
        verify(tournamentRepository, never()).deleteById(anyLong());
    }

    // ================= joinTournamentAsClub =================
    @Test
    void joinTournamentAsClub_ValidData_ClubJoinedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;
        Long clubId = 100L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Champions League");
        tournament.setMaxTeams(16);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(101L, 102L)));
        tournament.setMinRank(1);
        tournament.setMaxRank(100);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(10L, "Stadium A", new ArrayList<>()));
        tournament.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        ClubProfile clubProfile = new ClubProfile();
        clubProfile.setId(clubId);
        clubProfile.setCaptainId(1001L);
        clubProfile.setElo(50.0);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(clubProfile);
        when(jwtTokenProvider.getToken(jwtToken)).thenReturn("extracted.token");
        when(jwtUtil.extractUserId(anyString())).thenReturn(1001L);
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        TournamentResponseDTO result = null;
        try {
            result = tournamentService.joinTournamentAsClub(dto, jwtToken);
        } catch (Exception e) {
            fail("Exception should not be thrown for valid data");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournamentId, result.getId());
        assertEquals("Champions League", result.getName());
        assertTrue(result.getJoinedClubIds().contains(clubId));
        assertEquals(3, result.getJoinedClubIds().size());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, times(1)).verifyNoPenaltyStatus(clubId);
        verify(clubServiceClient, times(1)).getClubProfileById(clubId, jwtToken);
        verify(jwtTokenProvider, times(1)).getToken(jwtToken);
        verify(jwtUtil, times(1)).extractUserId("extracted.token");
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void joinTournamentAsClub_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;
        Long clubId = 200L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, never()).verifyNoPenaltyStatus(anyLong());
        verify(clubServiceClient, never()).getClubProfileById(anyLong(), anyString());
        verify(jwtTokenProvider, never()).getToken(anyString());
        verify(jwtUtil, never()).extractUserId(anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_ClubAlreadyJoined_ThrowsClubAlreadyJoinedException() {
        // Arrange
        Long tournamentId = 3L;
        Long clubId = 300L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Europa League");
        tournament.setMaxTeams(8);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(301L, 302L, 300L))); // clubId already joined
        tournament.setMinRank(10);
        tournament.setMaxRank(80);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(20L, "Stadium B", new ArrayList<>()));

        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(new ClubProfile());
        when(jwtTokenProvider.getToken(jwtToken)).thenReturn("valid.jwt.token");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300L);
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act & Assert
        assertThrows(ClubAlreadyJoinedException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient).verifyNoPenaltyStatus(anyLong());
        verify(clubServiceClient).getClubProfileById(anyLong(), anyString());
        verify(jwtTokenProvider).getToken(anyString());
        verify(jwtUtil).extractUserId(anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_TournamentFull_ThrowsTournamentFullException() {
        // Arrange
        Long tournamentId = 4L;
        Long clubId = 400L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Local Cup");
        tournament.setMaxTeams(4);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(401L, 402L, 403L, 404L))); // Already full
        tournament.setMinRank(5);
        tournament.setMaxRank(90);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(30L, "Stadium C", new ArrayList<>()));

        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(new ClubProfile());
        when(jwtTokenProvider.getToken(jwtToken)).thenReturn("valid.jwt.token");
        when(jwtUtil.extractUserId(anyString())).thenReturn(300L);
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act & Assert
        assertThrows(TournamentFullException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient).verifyNoPenaltyStatus(anyLong());
        verify(clubServiceClient).getClubProfileById(anyLong(), anyString());
        verify(jwtTokenProvider).getToken(anyString());
        verify(jwtUtil).extractUserId(anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_ClubBlacklisted_ThrowsBlacklistedFromTournamentException() {
        // Arrange
        Long tournamentId = 5L;
        Long clubId = 500L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("International Cup");
        tournament.setMaxTeams(16);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(501L, 502L)));
        tournament.setMinRank(1);
        tournament.setMaxRank(100);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(40L, "Stadium D", new ArrayList<>()));

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(false);

        // Act & Assert
        assertThrows(BlacklistedFromTournamentException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, times(1)).verifyNoPenaltyStatus(clubId);
        verify(clubServiceClient, never()).getClubProfileById(anyLong(), anyString());
        verify(jwtTokenProvider, never()).getToken(anyString());
        verify(jwtUtil, never()).extractUserId(anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_ClubProfileNotFound_ThrowsClubProfileNotFoundException() {
        // Arrange
        Long tournamentId = 6L;
        Long clubId = 600L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Regional Cup");
        tournament.setMaxTeams(8);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(601L, 602L)));
        tournament.setMinRank(10);
        tournament.setMaxRank(80);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(50L, "Stadium E", new ArrayList<>()));

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(null); // Club profile not found

        // Act & Assert
        assertThrows(ClubProfileNotFoundAtClientException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, times(1)).verifyNoPenaltyStatus(clubId);
        verify(clubServiceClient, times(1)).getClubProfileById(clubId, jwtToken);
        verify(jwtTokenProvider, never()).getToken(anyString());
        verify(jwtUtil, never()).extractUserId(anyString());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_ClubEloTooLow_ThrowsClubEloTooLowException() {
        // Arrange
        Long tournamentId = 8L;
        Long clubId = 800L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Elite Cup");
        tournament.setMaxTeams(16);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(801L, 802L)));
        tournament.setMinRank(50);
        tournament.setMaxRank(150);
        tournament.setVerificationStatus(Tournament.VerificationStatus.PENDING);
        tournament.setLocation(new Location(70L, "Stadium G", new ArrayList<>()));

        ClubProfile clubProfile = new ClubProfile();
        clubProfile.setId(clubId);
        clubProfile.setCaptainId(2001L);
        clubProfile.setElo(40.0); // Below minRank

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(clubProfile);
        when(jwtTokenProvider.getToken(jwtToken)).thenReturn("extracted.token");
        when(jwtUtil.extractUserId(anyString())).thenReturn(2001L);

        // Act & Assert
        assertThrows(ClubEloTooLowException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, times(1)).verifyNoPenaltyStatus(clubId);
        verify(clubServiceClient, times(1)).getClubProfileById(clubId, jwtToken);
        verify(jwtTokenProvider, times(1)).getToken(jwtToken);
        verify(jwtUtil, times(1)).extractUserId("extracted.token");
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void joinTournamentAsClub_ClubEloTooHigh_ThrowsClubEloTooHighException() {
        // Arrange
        Long tournamentId = 9L;
        Long clubId = 900L;
        String jwtToken = "valid.jwt.token";

        TournamentJoinDTO dto = new TournamentJoinDTO();
        dto.setTournamentId(tournamentId);
        dto.setClubId(clubId);

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Masters Cup");
        tournament.setMaxTeams(20);
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(901L, 902L)));
        tournament.setMinRank(10);
        tournament.setMaxRank(100);
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(80L, "Stadium H", new ArrayList<>()));

        ClubProfile clubProfile = new ClubProfile();
        clubProfile.setId(clubId);
        clubProfile.setCaptainId(2001L);
        clubProfile.setElo(120.0); // Above maxRank

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(clubServiceClient.verifyNoPenaltyStatus(clubId)).thenReturn(true);
        when(clubServiceClient.getClubProfileById(clubId, jwtToken)).thenReturn(clubProfile);
        when(jwtTokenProvider.getToken(jwtToken)).thenReturn("extracted.token");
        when(jwtUtil.extractUserId(anyString())).thenReturn(2001L);

        // Act & Assert
        assertThrows(ClubEloTooHighException.class, () -> {
            tournamentService.joinTournamentAsClub(dto, jwtToken);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(clubServiceClient, times(1)).verifyNoPenaltyStatus(clubId);
        verify(clubServiceClient, times(1)).getClubProfileById(clubId, jwtToken);
        verify(jwtTokenProvider, times(1)).getToken(jwtToken);
        verify(jwtUtil, times(1)).extractUserId("extracted.token");
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= removeClubFromTournament =================
    @Test
    void removeClubFromTournament_ValidData_ClubRemovedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;
        Long clubId = 101L;

        // Initialize Tournament with joined clubs
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Champions League");
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(101L, 102L, 103L)));
        tournament.setHost(1L);
        tournament.setLocation(new Location());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act
        try {
            tournamentService.removeClubFromTournament(tournamentId, clubId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertFalse(tournament.getJoinedClubIds().contains(clubId));
        assertEquals(Arrays.asList(102L, 103L), tournament.getJoinedClubIds());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void removeClubFromTournament_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;
        Long clubId = 201L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.removeClubFromTournament(tournamentId, clubId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void removeClubFromTournament_ClubNotJoined_ThrowsClubNotJoinedException() {
        // Arrange
        Long tournamentId = 3L;
        Long clubId = 301L;

        // Initialize Tournament without the specified club
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Europa League");
        tournament.setJoinedClubIds(new ArrayList<>(Arrays.asList(302L, 303L)));
        tournament.setHost(2L);
        tournament.setLocation(new Location());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act & Assert
        assertThrows(ClubNotJoinedException.class, () -> {
            tournamentService.removeClubFromTournament(tournamentId, clubId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= getAllClubsInTournament =================
    @Test
    void getAllClubsInTournament_ExistingTournament_ReturnsClubIds() {
        // Arrange
        Long tournamentId = 1L;
        List<Long> clubIds = Arrays.asList(101L, 102L, 103L);

        // Initialize Tournament with joined clubs
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Champions League");
        tournament.setJoinedClubIds(new ArrayList<>(clubIds));
        tournament.setHost(1L);
        tournament.setLocation(new Location());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        List<Long> result = null;
        try {
            result = tournamentService.getAllClubsInTournament(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(clubIds.size(), result.size());
        assertTrue(result.containsAll(clubIds));

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    @Test
    void getAllClubsInTournament_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.getAllClubsInTournament(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    // ================= isOwnerOfTournament =================
    @Test
    void isOwnerOfTournament_UserIsOwner_ReturnsTrue() {
        // Arrange
        Long tournamentId = 1L;
        Long profileId = 101L;

        // Initialize Tournament with host matching profileId
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setHost(profileId);
        tournament.setLocation(new Location());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        boolean result = tournamentService.isOwnerOfTournament(tournamentId, profileId);

        // Assert
        assertTrue(result);

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    @Test
    void isOwnerOfTournament_UserIsNotOwner_ReturnsFalse() {
        // Arrange
        Long tournamentId = 2L;
        Long profileId = 202L;
        Long actualHostId = 201L;

        // Initialize Tournament with host different from profileId
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setHost(actualHostId);
        tournament.setLocation(new Location());

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        boolean result = tournamentService.isOwnerOfTournament(tournamentId, profileId);

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    @Test
    void isOwnerOfTournament_TournamentNotFound_ReturnsFalse() {
        // Arrange
        Long tournamentId = 3L;
        Long profileId = 301L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act
        boolean result = tournamentService.isOwnerOfTournament(tournamentId, profileId);

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    // ================= updatePlayerAvailability =================
    
    @Test
    void updatePlayerAvailability_ValidData_PlayerAvailabilityUpdatedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;
        Long playerId = 101L;
        Long clubId = 201L;
        boolean availabilityStatus = true;

        PlayerAvailabilityDTO dto = new PlayerAvailabilityDTO();
        dto.setTournamentId(tournamentId);
        dto.setPlayerId(playerId);
        dto.setClubId(clubId);
        dto.setAvailable(availabilityStatus);

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setHost(1L);
        tournament.setLocation(new Location()); // Initialize location if required

        // Initialize existing PlayerAvailability
        PlayerAvailability existingAvailability = new PlayerAvailability();
        existingAvailability.setTournament(tournament);
        existingAvailability.setPlayerId(playerId);
        existingAvailability.setClubId(clubId);
        existingAvailability.setAvailable(false);

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(playerAvailabilityRepository.findByTournamentIdAndPlayerId(tournamentId, playerId))
                .thenReturn(Optional.of(existingAvailability));
        when(playerAvailabilityRepository.save(any(PlayerAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlayerAvailability result = null;
        try {
            result = tournamentService.updatePlayerAvailability(dto);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournament, result.getTournament());
        assertEquals(playerId, result.getPlayerId());
        assertEquals(clubId, result.getClubId());
        assertEquals(availabilityStatus, result.isAvailable());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(playerAvailabilityRepository, times(1))
                .findByTournamentIdAndPlayerId(tournamentId, playerId);
        verify(playerAvailabilityRepository, times(1)).save(existingAvailability);
    }

    @Test
    void updatePlayerAvailability_NewAvailability_PlayerAvailabilityCreatedSuccessfully() {
        // Arrange
        Long tournamentId = 2L;
        Long playerId = 102L;
        Long clubId = 202L;
        boolean availabilityStatus = false;

        PlayerAvailabilityDTO dto = new PlayerAvailabilityDTO();
        dto.setTournamentId(tournamentId);
        dto.setPlayerId(playerId);
        dto.setClubId(clubId);
        dto.setAvailable(availabilityStatus);

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setHost(2L);
        tournament.setLocation(new Location()); // Initialize location if required

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(playerAvailabilityRepository.findByTournamentIdAndPlayerId(tournamentId, playerId))
                .thenReturn(Optional.empty());

        // Mock save behavior
        when(playerAvailabilityRepository.save(any(PlayerAvailability.class)))
                .thenAnswer(invocation -> {
                    PlayerAvailability pa = invocation.getArgument(0);
                    pa.setId(1001L); // Assume ID is set after saving
                    return pa;
                });

        // Act
        PlayerAvailability result = null;
        try {
            result = tournamentService.updatePlayerAvailability(dto);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournament, result.getTournament());
        assertEquals(playerId, result.getPlayerId());
        assertEquals(clubId, result.getClubId());
        assertEquals(availabilityStatus, result.isAvailable());
        assertEquals(1001L, result.getId());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(playerAvailabilityRepository, times(1))
                .findByTournamentIdAndPlayerId(tournamentId, playerId);
        verify(playerAvailabilityRepository, times(1)).save(any(PlayerAvailability.class));
    }

    @Test
    void updatePlayerAvailability_ClubIdIsNull_ThrowsNoClubIndicateAvailabilityException() {
        // Arrange
        PlayerAvailabilityDTO dto = new PlayerAvailabilityDTO();
        dto.setTournamentId(3L);
        dto.setPlayerId(103L);
        dto.setClubId(null); // clubId is null
        dto.setAvailable(true);

        // Act & Assert
        assertThrows(NoClubIndicateAvailabilityException.class, () -> {
            tournamentService.updatePlayerAvailability(dto);
        });

        // Verify interactions
        verify(tournamentRepository, never()).findById(anyLong());
        verify(playerAvailabilityRepository, never()).findByTournamentIdAndPlayerId(anyLong(), anyLong());
        verify(playerAvailabilityRepository, never()).save(any(PlayerAvailability.class));
    }

    @Test
    void updatePlayerAvailability_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 4L;
        Long playerId = 104L;
        Long clubId = 204L;
        boolean availabilityStatus = true;

        PlayerAvailabilityDTO dto = new PlayerAvailabilityDTO();
        dto.setTournamentId(tournamentId);
        dto.setPlayerId(playerId);
        dto.setClubId(clubId);
        dto.setAvailable(availabilityStatus);

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.updatePlayerAvailability(dto);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(playerAvailabilityRepository, never()).findByTournamentIdAndPlayerId(anyLong(), anyLong());
        verify(playerAvailabilityRepository, never()).save(any(PlayerAvailability.class));
    }

    @Test
    void updatePlayerAvailability_SaveFails_ThrowsInvalidPlayerAvailabilityException() {
        // Arrange
        Long tournamentId = 5L;
        Long playerId = 105L;
        Long clubId = 205L;
        boolean availabilityStatus = false;

        PlayerAvailabilityDTO dto = new PlayerAvailabilityDTO();
        dto.setTournamentId(tournamentId);
        dto.setPlayerId(playerId);
        dto.setClubId(clubId);
        dto.setAvailable(availabilityStatus);

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setHost(3L);
        tournament.setLocation(new Location()); // Initialize location if required

        // Initialize existing PlayerAvailability
        PlayerAvailability existingAvailability = new PlayerAvailability();
        existingAvailability.setTournament(tournament);
        existingAvailability.setPlayerId(playerId);
        existingAvailability.setClubId(clubId);
        existingAvailability.setAvailable(true);

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(playerAvailabilityRepository.findByTournamentIdAndPlayerId(tournamentId, playerId))
                .thenReturn(Optional.of(existingAvailability));
        when(playerAvailabilityRepository.save(any(PlayerAvailability.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(InvalidPlayerAvailabilityException.class, () -> {
            tournamentService.updatePlayerAvailability(dto);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(playerAvailabilityRepository, times(1))
                .findByTournamentIdAndPlayerId(tournamentId, playerId);
        verify(playerAvailabilityRepository, times(1)).save(existingAvailability);
    }

    @Test
    void getPlayerAvailabilityForTournament_ExistingTournamentWithAvailabilities_ReturnsPlayerAvailabilityDTOs() {
        // Arrange
        Long tournamentId = 1L;

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Champions League");
        tournament.setHost(1L);
        tournament.setLocation(new Location());

        PlayerAvailability pa1 = new PlayerAvailability();
        pa1.setTournament(tournament);
        pa1.setPlayerId(101L);
        pa1.setClubId(201L);
        pa1.setAvailable(true);

        PlayerAvailability pa2 = new PlayerAvailability();
        pa2.setTournament(tournament);
        pa2.setPlayerId(102L);
        pa2.setClubId(202L);
        pa2.setAvailable(false);

        List<PlayerAvailability> availabilities = Arrays.asList(pa1, pa2);

        when(playerAvailabilityRepository.findByTournamentId(tournamentId)).thenReturn(availabilities);

        // Act
        List<PlayerAvailabilityDTO> result = tournamentService.getPlayerAvailabilityForTournament(tournamentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PlayerAvailabilityDTO dto1 = result.get(0);
        assertEquals(tournamentId, dto1.getTournamentId());
        assertEquals(pa1.getPlayerId(), dto1.getPlayerId());
        assertEquals(pa1.getClubId(), dto1.getClubId());
        assertEquals(pa1.isAvailable(), dto1.isAvailable());

        PlayerAvailabilityDTO dto2 = result.get(1);
        assertEquals(tournamentId, dto2.getTournamentId());
        assertEquals(pa2.getPlayerId(), dto2.getPlayerId());
        assertEquals(pa2.getClubId(), dto2.getClubId());
        assertEquals(pa2.isAvailable(), dto2.isAvailable());

        // Verify interactions
        verify(playerAvailabilityRepository, times(1)).findByTournamentId(tournamentId);
    }

    @Test
    void getPlayerAvailabilityForTournament_ExistingTournamentWithNoAvailabilities_ReturnsEmptyList() {
        // Arrange
        Long tournamentId = 2L;

        when(playerAvailabilityRepository.findByTournamentId(tournamentId))
                .thenReturn(Collections.emptyList());

        // Act
        List<PlayerAvailabilityDTO> result = tournamentService.getPlayerAvailabilityForTournament(tournamentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(playerAvailabilityRepository, times(1)).findByTournamentId(tournamentId);
    }

    @Test
    void getPlayerAvailabilityForTournament_TournamentNotFound_ReturnsEmptyList() {
        // Arrange
        Long tournamentId = 3L;

        when(playerAvailabilityRepository.findByTournamentId(tournamentId))
                .thenReturn(Collections.emptyList());

        // Act
        List<PlayerAvailabilityDTO> result = tournamentService.getPlayerAvailabilityForTournament(tournamentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(playerAvailabilityRepository, times(1)).findByTournamentId(tournamentId);
    }

    // ================= getHostedTournaments =================
    @Test
    void getHostedTournaments_HostWithTournaments_ReturnsTournaments() {
        // Arrange
        Long hostId = 1L;

        Tournament tournament1 = new Tournament();
        tournament1.setId(101L);
        tournament1.setName("Hosted Tournament 1");
        
        Tournament tournament2 = new Tournament();
        tournament2.setId(102L);
        tournament2.setName("Hosted Tournament 2");

        List<Tournament> hostedTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findByHost(hostId)).thenReturn(hostedTournaments);

        // Act
        List<Tournament> result = tournamentService.getHostedTournaments(hostId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tournament1));
        assertTrue(result.contains(tournament2));

        // Verify interactions
        verify(tournamentRepository, times(1)).findByHost(hostId);
    }

    @Test
    void getHostedTournaments_HostWithNoTournaments_ReturnsEmptyList() {
        // Arrange
        Long hostId = 2L;

        when(tournamentRepository.findByHost(hostId)).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getHostedTournaments(hostId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findByHost(hostId);
    }

    @Test
    void getHostedTournaments_HostIsNull_ReturnsEmptyList() {
        // Arrange
        Long hostId = null;

        when(tournamentRepository.findByHost(hostId)).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getHostedTournaments(hostId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findByHost(hostId);
    }

    // ================= getTournamentsForClub =================

    @Test
    void getTournamentsForClub_UpcomingFilter_ReturnsUpcomingTournaments() {
        // Arrange
        Long clubId = 1L;
        TournamentFilter filter = TournamentFilter.UPCOMING;

        Tournament tournament1 = new Tournament();
        tournament1.setId(101L);
        tournament1.setName("Upcoming Tournament 1");
        tournament1.setStartDateTime(LocalDateTime.now().plusDays(10));
        tournament1.setEndDateTime(LocalDateTime.now().plusDays(12));
        tournament1.setHost(1001L);
        tournament1.setLocation(new Location(100L, "Stadium A", new ArrayList<>()));
        tournament1.setJoinedClubIds(Arrays.asList(clubId, 2002L));
        tournament1.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        Tournament tournament2 = new Tournament();
        tournament2.setId(102L);
        tournament2.setName("Upcoming Tournament 2");
        tournament2.setStartDateTime(LocalDateTime.now().plusDays(15));
        tournament2.setEndDateTime(LocalDateTime.now().plusDays(17));
        tournament2.setHost(1002L);
        tournament2.setLocation(new Location(101L, "Stadium B", new ArrayList<>()));
        tournament2.setJoinedClubIds(Arrays.asList(clubId, 2003L));
        tournament2.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        List<Tournament> upcomingTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findUpcomingTournamentsForClub(clubId)).thenReturn(upcomingTournaments);

        // Act
        List<TournamentResponseDTO> result = tournamentService.getTournamentsForClub(clubId, filter);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        TournamentResponseDTO dto1 = result.get(0);
        assertEquals(tournament1.getId(), dto1.getId());
        assertEquals(tournament1.getName(), dto1.getName());
        assertEquals(tournament1.getStartDateTime(), dto1.getStartDateTime());
        assertEquals(tournament1.getEndDateTime(), dto1.getEndDateTime());
        assertEquals(tournament1.getHost(), dto1.getHost());
        assertEquals(tournament1.getLocation().getId(), dto1.getLocation().getId());
        assertEquals(tournament1.getLocation().getName(), dto1.getLocation().getName());

        TournamentResponseDTO dto2 = result.get(1);
        assertEquals(tournament2.getId(), dto2.getId());
        assertEquals(tournament2.getName(), dto2.getName());
        assertEquals(tournament2.getStartDateTime(), dto2.getStartDateTime());
        assertEquals(tournament2.getEndDateTime(), dto2.getEndDateTime());
        assertEquals(tournament2.getHost(), dto2.getHost());
        assertEquals(tournament2.getLocation().getId(), dto2.getLocation().getId());
        assertEquals(tournament2.getLocation().getName(), dto2.getLocation().getName());

        // Verify interactions
        verify(tournamentRepository, times(1)).findUpcomingTournamentsForClub(clubId);
    }

    @Test
    void getTournamentsForClub_CurrentFilter_ReturnsCurrentTournaments() {
        // Arrange
        Long clubId = 2L;
        TournamentFilter filter = TournamentFilter.CURRENT;

        Tournament tournament1 = new Tournament();
        tournament1.setId(201L);
        tournament1.setName("Current Tournament 1");
        tournament1.setStartDateTime(LocalDateTime.now().minusDays(1));
        tournament1.setEndDateTime(LocalDateTime.now().plusDays(1));
        tournament1.setHost(2001L);
        tournament1.setLocation(new Location(200L, "Stadium C", new ArrayList<>()));
        tournament1.setJoinedClubIds(Arrays.asList(clubId, 3002L));
        tournament1.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        List<Tournament> currentTournaments = Collections.singletonList(tournament1);

        when(tournamentRepository.findCurrentTournamentsForClub(clubId)).thenReturn(currentTournaments);

        // Act
        List<TournamentResponseDTO> result = tournamentService.getTournamentsForClub(clubId, filter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        TournamentResponseDTO dto1 = result.get(0);
        assertEquals(tournament1.getId(), dto1.getId());
        assertEquals(tournament1.getName(), dto1.getName());
        assertEquals(tournament1.getStartDateTime(), dto1.getStartDateTime());
        assertEquals(tournament1.getEndDateTime(), dto1.getEndDateTime());
        assertEquals(tournament1.getHost(), dto1.getHost());
        assertEquals(tournament1.getLocation().getId(), dto1.getLocation().getId());
        assertEquals(tournament1.getLocation().getName(), dto1.getLocation().getName());

        // Verify interactions
        verify(tournamentRepository, times(1)).findCurrentTournamentsForClub(clubId);
    }

    @Test
    void getTournamentsForClub_PastFilter_ReturnsPastTournaments() {
        // Arrange
        Long clubId = 3L;
        TournamentFilter filter = TournamentFilter.PAST;

        Tournament tournament1 = new Tournament();
        tournament1.setId(301L);
        tournament1.setName("Past Tournament 1");
        tournament1.setStartDateTime(LocalDateTime.now().minusDays(10));
        tournament1.setEndDateTime(LocalDateTime.now().minusDays(8));
        tournament1.setHost(3001L);
        tournament1.setLocation(new Location(300L, "Stadium D", new ArrayList<>()));
        tournament1.setJoinedClubIds(Arrays.asList(clubId, 4002L));
        tournament1.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        Tournament tournament2 = new Tournament();
        tournament2.setId(302L);
        tournament2.setName("Past Tournament 2");
        tournament2.setStartDateTime(LocalDateTime.now().minusDays(20));
        tournament2.setEndDateTime(LocalDateTime.now().minusDays(18));
        tournament2.setHost(3002L);
        tournament2.setLocation(new Location(301L, "Stadium E", new ArrayList<>()));
        tournament2.setJoinedClubIds(Arrays.asList(clubId, 4003L));
        tournament2.setTournamentFormat(TournamentFormat.FIVE_SIDE);

        List<Tournament> pastTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findPastTournamentsForClub(clubId)).thenReturn(pastTournaments);

        // Act
        List<TournamentResponseDTO> result = tournamentService.getTournamentsForClub(clubId, filter);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        TournamentResponseDTO dto1 = result.get(0);
        assertEquals(tournament1.getId(), dto1.getId());
        assertEquals(tournament1.getName(), dto1.getName());
        assertEquals(tournament1.getStartDateTime(), dto1.getStartDateTime());
        assertEquals(tournament1.getEndDateTime(), dto1.getEndDateTime());
        assertEquals(tournament1.getHost(), dto1.getHost());
        assertEquals(tournament1.getLocation().getId(), dto1.getLocation().getId());
        assertEquals(tournament1.getLocation().getName(), dto1.getLocation().getName());

        TournamentResponseDTO dto2 = result.get(1);
        assertEquals(tournament2.getId(), dto2.getId());
        assertEquals(tournament2.getName(), dto2.getName());
        assertEquals(tournament2.getStartDateTime(), dto2.getStartDateTime());
        assertEquals(tournament2.getEndDateTime(), dto2.getEndDateTime());
        assertEquals(tournament2.getHost(), dto2.getHost());
        assertEquals(tournament2.getLocation().getId(), dto2.getLocation().getId());
        assertEquals(tournament2.getLocation().getName(), dto2.getLocation().getName());

        // Verify interactions
        verify(tournamentRepository, times(1)).findPastTournamentsForClub(clubId);
    }

    @Test
    void getTournamentsForClub_NoTournaments_ReturnsEmptyList() {
        // Arrange
        Long clubId = 5L;
        TournamentFilter filter = TournamentFilter.UPCOMING;

        when(tournamentRepository.findUpcomingTournamentsForClub(clubId)).thenReturn(Collections.emptyList());

        // Act
        List<TournamentResponseDTO> result = tournamentService.getTournamentsForClub(clubId, filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findUpcomingTournamentsForClub(clubId);
    }

    // ================= submitVerification =================
    @Test
    void submitVerification_ValidData_TournamentUpdatedSuccessfully() {
        // Arrange
        Long tournamentId = 1L;
        String confirmationUrl = "http://example.com/verification.png";
        boolean venueBooked = true;

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Sample Tournament");
        tournament.setVerificationImageUrl(null);
        tournament.setVenueBooked(false);
        tournament.setVerificationStatus(Tournament.VerificationStatus.AWAITING_PAYMENT);
        tournament.setLocation(new Location(100L, "Stadium A", new ArrayList<>()));
        tournament.setHost(1001L);
        tournament.setJoinedClubIds(new ArrayList<>());

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tournament result = null;
        try {
            result = tournamentService.submitVerification(tournamentId, confirmationUrl, venueBooked);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(confirmationUrl, result.getVerificationImageUrl());
        assertEquals(venueBooked, result.getVenueBooked());
        assertEquals(Tournament.VerificationStatus.PENDING, result.getVerificationStatus());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void submitVerification_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 2L;
        String confirmationUrl = "http://example.com/verification2.png";
        boolean venueBooked = false;

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.submitVerification(tournamentId, confirmationUrl, venueBooked);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= approveVerification =================
    @Test
    void approveVerification_ValidId_TournamentApprovedSuccessfully() {
        // Arrange
        Long tournamentId = 3L;

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Approval Tournament");
        tournament.setVerificationStatus(Tournament.VerificationStatus.PENDING);
        tournament.setLocation(new Location(101L, "Stadium B", new ArrayList<>()));
        tournament.setHost(1002L);
        tournament.setJoinedClubIds(new ArrayList<>());

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tournament result = null;
        try {
            result = tournamentService.approveVerification(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(Tournament.VerificationStatus.APPROVED, result.getVerificationStatus());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void approveVerification_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 4L;

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.approveVerification(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= rejectVerification =================
    @Test
    void rejectVerification_ValidId_TournamentRejectedSuccessfully() {
        // Arrange
        Long tournamentId = 5L;

        // Initialize Tournament
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Rejection Tournament");
        tournament.setVerificationStatus(Tournament.VerificationStatus.PENDING);
        tournament.setLocation(new Location(102L, "Stadium C", new ArrayList<>()));
        tournament.setHost(1003L);
        tournament.setJoinedClubIds(new ArrayList<>());

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tournament result = null;
        try {
            result = tournamentService.rejectVerification(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(Tournament.VerificationStatus.REJECTED, result.getVerificationStatus());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void rejectVerification_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 6L;

        // Mock repository behavior
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.rejectVerification(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    // ================= getPendingVerifications =================
    @Test
    void getPendingVerifications_ReturnsPendingTournaments() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.PENDING;

        Tournament tournament1 = new Tournament();
        tournament1.setId(101L);
        tournament1.setName("Pending Tournament 1");
        tournament1.setVerificationStatus(status);
        tournament1.setLocation(new Location(100L, "Stadium A", new ArrayList<>()));
        tournament1.setHost(1001L);
        tournament1.setJoinedClubIds(Arrays.asList(2001L, 2002L));

        Tournament tournament2 = new Tournament();
        tournament2.setId(102L);
        tournament2.setName("Pending Tournament 2");
        tournament2.setVerificationStatus(status);
        tournament2.setLocation(new Location(101L, "Stadium B", new ArrayList<>()));
        tournament2.setHost(1002L);
        tournament2.setJoinedClubIds(Arrays.asList(2003L, 2004L));

        List<Tournament> pendingTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(pendingTournaments);

        // Act
        List<Tournament> result = tournamentService.getPendingVerifications();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tournament1));
        assertTrue(result.contains(tournament2));

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    @Test
    void getPendingVerifications_NoPendingTournaments_ReturnsEmptyList() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.PENDING;

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getPendingVerifications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    // ================= getApprovedVerifications =================
    @Test
    void getApprovedVerifications_ReturnsApprovedTournaments() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.APPROVED;

        Tournament tournament1 = new Tournament();
        tournament1.setId(201L);
        tournament1.setName("Approved Tournament 1");
        tournament1.setVerificationStatus(status);
        tournament1.setLocation(new Location(200L, "Stadium C", new ArrayList<>()));
        tournament1.setHost(2001L);
        tournament1.setJoinedClubIds(Arrays.asList(3001L, 3002L));

        Tournament tournament2 = new Tournament();
        tournament2.setId(202L);
        tournament2.setName("Approved Tournament 2");
        tournament2.setVerificationStatus(status);
        tournament2.setLocation(new Location(201L, "Stadium D", new ArrayList<>()));
        tournament2.setHost(2002L);
        tournament2.setJoinedClubIds(Arrays.asList(3003L, 3004L));

        List<Tournament> approvedTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(approvedTournaments);

        // Act
        List<Tournament> result = tournamentService.getApprovedVerifications();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tournament1));
        assertTrue(result.contains(tournament2));

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    @Test
    void getApprovedVerifications_NoApprovedTournaments_ReturnsEmptyList() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.APPROVED;

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getApprovedVerifications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    // ================= getRejectedVerifications =================
    @Test
    void getRejectedVerifications_ReturnsRejectedTournaments() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.REJECTED;

        Tournament tournament1 = new Tournament();
        tournament1.setId(301L);
        tournament1.setName("Rejected Tournament 1");
        tournament1.setVerificationStatus(status);
        tournament1.setLocation(new Location(300L, "Stadium E", new ArrayList<>()));
        tournament1.setHost(3001L);
        tournament1.setJoinedClubIds(Arrays.asList(4001L, 4002L));

        Tournament tournament2 = new Tournament();
        tournament2.setId(302L);
        tournament2.setName("Rejected Tournament 2");
        tournament2.setVerificationStatus(status);
        tournament2.setLocation(new Location(301L, "Stadium F", new ArrayList<>()));
        tournament2.setHost(3002L);
        tournament2.setJoinedClubIds(Arrays.asList(4003L, 4004L));

        List<Tournament> rejectedTournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(rejectedTournaments);

        // Act
        List<Tournament> result = tournamentService.getRejectedVerifications();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tournament1));
        assertTrue(result.contains(tournament2));

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    @Test
    void getRejectedVerifications_NoRejectedTournaments_ReturnsEmptyList() {
        // Arrange
        Tournament.VerificationStatus status = Tournament.VerificationStatus.REJECTED;

        when(tournamentRepository.findByVerificationStatus(status)).thenReturn(Collections.emptyList());

        // Act
        List<Tournament> result = tournamentService.getRejectedVerifications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify interactions
        verify(tournamentRepository, times(1)).findByVerificationStatus(status);
    }

    // ================= findById =================
    @Test
    void findById_ExistingId_ReturnsTournament() {
        // Arrange
        Long tournamentId = 401L;

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("FindById Tournament");
        tournament.setVerificationStatus(Tournament.VerificationStatus.APPROVED);
        tournament.setLocation(new Location(400L, "Stadium G", new ArrayList<>()));
        tournament.setHost(4001L);
        tournament.setJoinedClubIds(Arrays.asList(5001L, 5002L));

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        Tournament result = null;
        try {
            result = tournamentService.findById(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertNotNull(result);
        assertEquals(tournamentId, result.getId());
        assertEquals("FindById Tournament", result.getName());
        assertEquals(Tournament.VerificationStatus.APPROVED, result.getVerificationStatus());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    @Test
    void findById_NonExistingId_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 402L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.findById(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
    }

    // ================= updateTournamentPaymentStatus =================
    @Test
    void updateTournamentPaymentStatus_ExistingId_UpdatesPaymentStatusSuccessfully() {
        // Arrange
        Long tournamentId = 501L;

        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        tournament.setName("Payment Tournament");
        tournament.setVerificationPaid(false);
        tournament.setVerificationStatus(Tournament.VerificationStatus.AWAITING_PAYMENT);
        tournament.setLocation(new Location(500L, "Stadium H", new ArrayList<>()));
        tournament.setHost(5001L);
        tournament.setJoinedClubIds(Arrays.asList(6001L, 6002L));

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        try {
            tournamentService.updateTournamentPaymentStatus(tournamentId);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Assert
        assertTrue(tournament.isVerificationPaid());
        assertEquals(Tournament.VerificationStatus.PAYMENT_COMPLETED, tournament.getVerificationStatus());

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void updateTournamentPaymentStatus_TournamentNotFound_ThrowsTournamentNotFoundException() {
        // Arrange
        Long tournamentId = 502L;

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.updateTournamentPaymentStatus(tournamentId);
        });

        // Verify interactions
        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }
}