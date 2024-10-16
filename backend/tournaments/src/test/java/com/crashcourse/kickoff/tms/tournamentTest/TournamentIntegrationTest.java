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
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        /*
         * In case tear down not implemented
         */
        tournamentRepository.deleteAll();
        locationRepository.deleteAll();


        Location location1 = new Location();
        location1.setName("Secondary Stadium");
        location1.setTournaments(new ArrayList<>());

        Tournament tournament1 = new Tournament();
        tournament1.setName("Autumn Cup");
        tournament1.setStartDateTime(LocalDateTime.now().plusDays(3));
        tournament1.setEndDateTime(LocalDateTime.now().plusDays(4));

        tournament1.setMaxTeams(8);
        tournament1.setTournamentFormat(TournamentFormat.FIVE_SIDE);
        tournament1.setKnockoutFormat(KnockoutFormat.SINGLE_ELIM);
        tournament1.setPrizePool(Arrays.asList(2000.0f, 1000.0f));

        tournament1.setMinRank(2);
        tournament1.setMaxRank(15);
        tournament1.setHost(1L);

        /**
         * Necessary to prevent persistence error
         */
        tournament1.setLocation(location1);
        location1.getTournaments().add(tournament1);
        location1 = locationRepository.save(location1);

        tournamentRepository.save(tournament1);

        
        Location location2 = new Location();
        location2.setName("Tertiary Stadium");
        location2.setTournaments(new ArrayList<>());

        Tournament tournament2 = new Tournament();
        tournament2.setName("Winter Championship");
        tournament2.setStartDateTime(LocalDateTime.now().plusDays(5));
        tournament2.setEndDateTime(LocalDateTime.now().plusDays(6));

        tournament2.setMaxTeams(12);
        tournament2.setTournamentFormat(TournamentFormat.FIVE_SIDE);
        tournament2.setKnockoutFormat(KnockoutFormat.SINGLE_ELIM);
        tournament2.setPrizePool(Arrays.asList(1500.0f, 750.0f));

        tournament2.setMinRank(3);
        tournament2.setMaxRank(20);
        tournament2.setHost(2L);

        /**
         * Necessary to prevent persistence error
         */
        tournament2.setLocation(location2);
        location2.getTournaments().add(tournament2);
        location2 = locationRepository.save(location2);
        
        tournamentRepository.save(tournament2);

    }

    /**
     * Teardown method to clean up after each test
     */
    @AfterEach
    void tearDown() {
        tournamentRepository.deleteAll();
        locationRepository.deleteAll();
    }

    /**
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void getAllTournaments_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/tournaments");

        /**
         * Expects a list of TournamentRepsonseDTOs
         */
        ResponseEntity<TournamentResponseDTO[]> response = restTemplate.getForEntity(uri, TournamentResponseDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        TournamentResponseDTO[] tournaments = response.getBody();
        assertEquals(2, tournaments.length, "Expected two tournaments in the response");

        /**
         * Verify the first tournament
         */

        TournamentResponseDTO tournament1 = Arrays.stream(tournaments)
                .filter(t -> t.getName().equals("Autumn Cup"))
                .findFirst()
                .orElse(null);

        assertNotNull(tournament1, "Autumn Cup should be present in the response");
        assertEquals(8, tournament1.getMaxTeams(), "Autumn Cup should have 8 max teams");
        assertEquals(TournamentFormat.FIVE_SIDE.toString(), tournament1.getTournamentFormat(), "Tournament format should match");
        assertEquals(KnockoutFormat.SINGLE_ELIM.toString(), tournament1.getKnockoutFormat(), "Knockout format should match");
        assertEquals(2, tournament1.getMinRank(), "Minimum rank should match");
        assertEquals(15, tournament1.getMaxRank(), "Maximum rank should match");
        assertEquals(1L, tournament1.getHost(), "Host ID should match");

        assertNotNull(tournament1.getLocation(), "Location should not be null for Autumn Cup");
        assertEquals("Secondary Stadium", tournament1.getLocation().getName(), "Location name should match for Autumn Cup");

        /**
         * Verify the second tournament
         */
        TournamentResponseDTO tournament2 = Arrays.stream(tournaments)
                .filter(t -> t.getName().equals("Winter Championship"))
                .findFirst()
                .orElse(null);

        assertNotNull(tournament2, "Winter Championship should be present in the response");
        assertEquals(12, tournament2.getMaxTeams(), "Winter Championship should have 12 max teams");
        assertEquals(TournamentFormat.FIVE_SIDE.toString(), tournament2.getTournamentFormat(), "Tournament format should match");
        assertEquals(KnockoutFormat.SINGLE_ELIM.toString(), tournament2.getKnockoutFormat(), "Knockout format should match");
        assertEquals(3, tournament2.getMinRank(), "Minimum rank should match");
        assertEquals(20, tournament2.getMaxRank(), "Maximum rank should match");
        assertEquals(2L, tournament2.getHost(), "Host ID should match");

        assertNotNull(tournament2.getLocation(), "Location should not be null for Winter Championship");
        assertEquals("Tertiary Stadium", tournament2.getLocation().getName(), "Location name should match for Winter Championship");
    }
}
