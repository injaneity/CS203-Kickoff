package com.crashcourse.kickoff.tms.tournamenttest;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.*;

import com.crashcourse.kickoff.tms.tournament.dto.TournamentResponseDTO;
import com.crashcourse.kickoff.tms.tournament.model.*;
import com.crashcourse.kickoff.tms.tournament.repository.TournamentRepository;

import com.crashcourse.kickoff.tms.location.model.Location;
import com.crashcourse.kickoff.tms.location.repository.LocationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TournamentTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Teardown method to clean up after each test
     */
    @AfterEach
    void tearDown() {
        tournamentRepository.deleteAll();
        locationRepository.deleteAll();
    }

    /**
     * Helper method to create and save a tournament
     */
    private Tournament createTournament(String name, String locationName, int maxTeams, 
                                        TournamentFormat tournamentFormat, KnockoutFormat knockoutFormat,
                                        int minRank, int maxRank, Long hostId) {
        Location location = new Location();
        location.setName(locationName);
        location.setTournaments(new ArrayList<>());

        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setStartDateTime(LocalDateTime.now().plusDays(1));
        tournament.setEndDateTime(LocalDateTime.now().plusDays(2));
        tournament.setMaxTeams(maxTeams);
        tournament.setTournamentFormat(tournamentFormat);
        tournament.setKnockoutFormat(knockoutFormat);
        tournament.setPrizePool(Arrays.asList(1000.0f, 500.0f));
        tournament.setMinRank(minRank);
        tournament.setMaxRank(maxRank);
        tournament.setHost(hostId);

        tournament.setLocation(location);
        location.getTournaments().add(tournament);
        locationRepository.save(location);

        // Save Tournament
        tournament = tournamentRepository.save(tournament);

        return tournament;
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    void getTournamentById_Success() throws Exception {

        Tournament createdTournament = createTournament(
                "Autumn Cup",
                "Secondary Stadium",
                8,
                TournamentFormat.FIVE_SIDE,
                KnockoutFormat.SINGLE_ELIM,
                2,
                15,
                1L
        );

        Long tournamentId = createdTournament.getId();
        URI uri = new URI(baseUrl + port + "/tournaments/" + tournamentId);
        ResponseEntity<TournamentResponseDTO> response = restTemplate.getForEntity(uri, TournamentResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        TournamentResponseDTO tournament = response.getBody();
        System.out.println("Tournament Response: " + tournament);
        assertNotNull(tournament, "TournamentResponseDTO should not be null");
        
        assertEquals(tournamentId, tournament.getId(), "Tournament ID should match");
        assertEquals("Autumn Cup", tournament.getName(), "Tournament name should match");
        assertEquals(8, tournament.getMaxTeams(), "Max teams should match");
        assertEquals(TournamentFormat.FIVE_SIDE.toString(), tournament.getTournamentFormat(), "Tournament format should match");
        assertEquals(KnockoutFormat.SINGLE_ELIM.toString(), tournament.getKnockoutFormat(), "Knockout format should match");
        assertEquals(2, tournament.getMinRank(), "Minimum rank should match");
        assertEquals(15, tournament.getMaxRank(), "Maximum rank should match");
        assertEquals(1L, tournament.getHost(), "Host ID should match");

        // Verify the location details
        assertNotNull(tournament.getLocation(), "Location should not be null for the tournament");
        assertEquals("Secondary Stadium", tournament.getLocation().getName(), "Location name should match for the tournament");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    void getTournamentById_NotFound() throws Exception {
        /**
         * Let's assume that 999 does not exist
         */
        Long nonExistingId = 999L;
        URI uri = new URI(baseUrl + port + "/tournaments/" + nonExistingId);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        /**
         * Assert: Verify that the response status is 500 Internal Server Error
         */
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Expected HTTP status 500 Internal Server Error");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    void getAllTournaments_Success() throws Exception {
        // Arrange: Create two tournaments
        Tournament tournament1 = createTournament(
                "Autumn Cup",
                "Secondary Stadium",
                8,
                TournamentFormat.FIVE_SIDE,
                KnockoutFormat.SINGLE_ELIM,
                2,
                15,
                1L
        );

        Tournament tournament2 = createTournament(
                "Winter Championship",
                "Tertiary Stadium",
                12,
                TournamentFormat.FIVE_SIDE,
                KnockoutFormat.SINGLE_ELIM,
                3,
                20,
                2L
        );

        URI uri = new URI(baseUrl + port + "/tournaments");

        /**
         * Act: Make a GET request to fetch all tournaments
         */
        ResponseEntity<TournamentResponseDTO[]> response = restTemplate.getForEntity(uri, TournamentResponseDTO[].class);

        /**
         * Assert: Verify the response status and body
         */
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        TournamentResponseDTO[] tournaments = response.getBody();
        assertEquals(2, tournaments.length, "Expected two tournaments in the response");

        /**
         * Verify the first tournament
         */
        TournamentResponseDTO retrievedTournament1 = Arrays.stream(tournaments)
                .filter(t -> t.getName().equals("Autumn Cup"))
                .findFirst()
                .orElse(null);

        assertNotNull(retrievedTournament1, "Autumn Cup should be present in the response");
        assertEquals(8, retrievedTournament1.getMaxTeams(), "Autumn Cup should have 8 max teams");
        assertEquals(TournamentFormat.FIVE_SIDE.toString(), retrievedTournament1.getTournamentFormat(), "Tournament format should match");
        assertEquals(KnockoutFormat.SINGLE_ELIM.toString(), retrievedTournament1.getKnockoutFormat(), "Knockout format should match");
        assertEquals(2, retrievedTournament1.getMinRank(), "Minimum rank should match");
        assertEquals(15, retrievedTournament1.getMaxRank(), "Maximum rank should match");
        assertEquals(1L, retrievedTournament1.getHost(), "Host ID should match");

        assertNotNull(retrievedTournament1.getLocation(), "Location should not be null for Autumn Cup");
        assertEquals("Secondary Stadium", retrievedTournament1.getLocation().getName(), "Location name should match for Autumn Cup");

        /**
         * Verify the second tournament
         */
        TournamentResponseDTO retrievedTournament2 = Arrays.stream(tournaments)
                .filter(t -> t.getName().equals("Winter Championship"))
                .findFirst()
                .orElse(null);

        assertNotNull(retrievedTournament2, "Winter Championship should be present in the response");
        assertEquals(12, retrievedTournament2.getMaxTeams(), "Winter Championship should have 12 max teams");
        assertEquals(TournamentFormat.FIVE_SIDE.toString(), retrievedTournament2.getTournamentFormat(), "Tournament format should match");
        assertEquals(KnockoutFormat.SINGLE_ELIM.toString(), retrievedTournament2.getKnockoutFormat(), "Knockout format should match");
        assertEquals(3, retrievedTournament2.getMinRank(), "Minimum rank should match");
        assertEquals(20, retrievedTournament2.getMaxRank(), "Maximum rank should match");
        assertEquals(2L, retrievedTournament2.getHost(), "Host ID should match");

        assertNotNull(retrievedTournament2.getLocation(), "Location should not be null for Winter Championship");
        assertEquals("Tertiary Stadium", retrievedTournament2.getLocation().getName(), "Location name should match for Winter Championship");
    }
}
