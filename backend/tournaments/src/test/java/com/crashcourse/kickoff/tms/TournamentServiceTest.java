package com.crashcourse.kickoff.tms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import com.crashcourse.kickoff.tms.tournament.dto.*;
import com.crashcourse.kickoff.tms.tournament.model.*;
import com.crashcourse.kickoff.tms.tournament.repository.*;
import com.crashcourse.kickoff.tms.tournament.service.TournamentServiceImpl;
import com.crashcourse.kickoff.tms.bracket.model.Bracket;
import com.crashcourse.kickoff.tms.bracket.service.BracketService;
import com.crashcourse.kickoff.tms.location.model.Location;
import com.crashcourse.kickoff.tms.location.repository.LocationRepository;
import com.crashcourse.kickoff.tms.location.service.LocationService;
import org.springframework.web.client.RestTemplate;
import com.crashcourse.kickoff.tms.tournament.exception.*;

class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private PlayerAvailabilityRepository playerAvailabilityRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BracketService bracketService;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

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
    // @Test
    // void startTournament_ValidData_TournamentStartedSuccessfully() {
    //     // Arrange
    //     Long tournamentId = 3L;
    //     String jwtToken = "valid.jwt.token";
    //     Long locationId = 100L;
    //     String locationName = "Stadium";

    //     // Initialize Location
    //     Location location = new Location();
    //     location.setId(locationId);
    //     location.setName(locationName);
    //     location.setTournaments(new ArrayList<>()); // Initialize tournaments list

    //     // Existing Tournament with joined clubs and no bracket
    //     Tournament tournament = new Tournament();
    //     tournament.setId(tournamentId);
    //     tournament.setName("Spring Invitational");
    //     tournament.setJoinedClubIds(Arrays.asList(201L, 202L));
    //     tournament.setBracket(null);
    //     tournament.setLocation(location);
    //     // Ensure location's tournaments list includes this tournament if needed
    //     // location.getTournaments().add(tournament); // Not strictly necessary here

    //     when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

    //     // Mock Bracket creation
    //     Bracket bracket = new Bracket();
    //     bracket.setId(301L);
    //     bracket.setTournament(tournament);

    //     when(bracketService.createBracket(eq(tournamentId), eq(tournament.getJoinedClubIds()), eq(jwtToken))).thenReturn(bracket);
    //     when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

    //     // Act
    //     TournamentResponseDTO result = null;
    //     try {
    //         result = tournamentService.startTournament(tournamentId, jwtToken);
    //     } catch (Exception e) {
    //         fail("Exception should not be thrown");
    //     }

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(tournament.getId(), result.getId());
    //     assertEquals(tournament.getName(), result.getName());
    //     assertEquals(tournament.getStartDateTime(), result.getStartDateTime());
    //     assertEquals(tournament.getEndDateTime(), result.getEndDateTime());
    //     assertNotNull(result.getLocation());
    //     assertEquals(locationId, result.getLocation().getId());
    //     assertEquals(locationName, result.getLocation().getName());
    //     assertEquals(tournament.getBracket(), result.getBracket());

    //     // Verify interactions
    //     verify(tournamentRepository, times(1)).findById(tournamentId);
    //     verify(bracketService, times(1)).createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken);
    //     verify(tournamentRepository, times(1)).save(tournament);
    // }

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
        when(bracketService.createBracket(eq(tournamentId), eq(tournament.getJoinedClubIds()), eq(jwtToken))).thenReturn(null);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> { // Depending on implementation, it might throw a different exception
            tournamentService.startTournament(tournamentId, jwtToken);
        });

        verify(tournamentRepository, times(1)).findById(tournamentId);
        verify(bracketService, times(1)).createBracket(tournamentId, tournament.getJoinedClubIds(), jwtToken);
        verify(tournamentRepository, times(1)).save(tournament);
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
}