package com.crashcourse.kickoff.tms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.crashcourse.kickoff.tms.location.model.Location;
import com.crashcourse.kickoff.tms.location.service.LocationService;
import com.crashcourse.kickoff.tms.tournament.dto.PlayerAvailabilityDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentCreateDTO;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentJoinDTO;
import com.crashcourse.kickoff.tms.tournament.model.KnockoutFormat;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFormat;
import com.crashcourse.kickoff.tms.tournament.service.TournamentService;
import com.crashcourse.kickoff.tms.security.JwtUtil;

@SpringBootApplication
public class KickoffTournamentManagementApplication {

    private JwtUtil jwtUtil;

    public KickoffTournamentManagementApplication(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    @Profile("!prod")
    public UserDetailsService userDetailsService() {
        UserDetails mockUser = User.builder()
            .username("mockCaptain")
            .password("{noop}password")
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CAPTAIN")))
            .build();

        return new InMemoryUserDetailsManager(mockUser);
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(KickoffTournamentManagementApplication.class, args);

        Environment env = ctx.getEnvironment();
        if (!env.acceptsProfiles(Profiles.of("prod"))) {
            initialiseMockData(ctx);
        }
    }

    private static void initialiseMockData(ApplicationContext ctx) {
        // Location
        LocationService locationService = ctx.getBean(LocationService.class);
        Location location1 = new Location(null, "MBS", null);
        locationService.createLocation(location1);
        Location location2 = new Location(null, "Botanic Gardens", null);
        locationService.createLocation(location2);
        Location location3 = new Location(null, "East Coast Park", null);
        locationService.createLocation(location3);
        Location location4 = new Location(null, "Jurong East Sports Hall", null);
        locationService.createLocation(location4);
        Location location5 = new Location(null, "Bras Basah (SMU)", null);
        locationService.createLocation(location5);
        Location location6 = new Location(null, "National Stadium Courts", null);
        locationService.createLocation(location6);
        Location location7 = new Location(null, "Our Tampines Hub", null);
        locationService.createLocation(location7);
        Location location8 = new Location(null, "Woodlands Football Courts", null);
        locationService.createLocation(location8);

        // Tournament
        TournamentService tournamentService = ctx.getBean(TournamentService.class);
        TournamentCreateDTO tournament1DTO = new TournamentCreateDTO("Saturday East-side Tournament", 
            LocalDateTime.of(2024, 10, 19, 10, 0, 0), 
            LocalDateTime.of(2024, 10, 19, 18, 0, 0), 
            location3, 8, TournamentFormat.FIVE_SIDE, KnockoutFormat.SINGLE_ELIM, 
            new ArrayList<Float>(), 500, 2000);
        tournamentService.createTournament(tournament1DTO, 1L);
System.out.println("[Added tournament 1]");

        TournamentCreateDTO tournament2DTO = new TournamentCreateDTO("Sunday Night Mini-Tournament", 
            LocalDateTime.of(2024, 10, 20, 19, 0, 0), 
            LocalDateTime.of(2024, 10, 20, 23, 0, 0), 
            location2, 4, TournamentFormat.FIVE_SIDE, KnockoutFormat.SINGLE_ELIM, 
            new ArrayList<Float>(), 500, 2000);
        tournamentService.createTournament(tournament2DTO, 2L);
System.out.println("[Added tournament 2]");

        TournamentCreateDTO tournament3DTO = new TournamentCreateDTO("Casual Tournament @ Central Singapore", 
            LocalDateTime.of(2024, 10, 26, 8, 0, 0), 
            LocalDateTime.of(2024, 10, 26, 13, 0, 0), 
            location1, 16, TournamentFormat.FIVE_SIDE, KnockoutFormat.DOUBLE_ELIM, 
            new ArrayList<Float>(), 500, 2000);
        tournamentService.createTournament(tournament3DTO, 3L);
System.out.println("[Added tournament 3]");

        // Generate JWT token for mock captain
        JwtUtil jwtUtil = ctx.getBean(JwtUtil.class);
        String jwtToken = "Bearer " + jwtUtil.generateToken("mockCaptain");

        // Join tournaments
        TournamentJoinDTO[] joinDTOs = {
            new TournamentJoinDTO(1L, 1L), new TournamentJoinDTO(3L, 1L), new TournamentJoinDTO(6L, 1L),
            new TournamentJoinDTO(1L, 2L), new TournamentJoinDTO(7L, 2L), new TournamentJoinDTO(4L, 2L), new TournamentJoinDTO(5L, 2L),
            new TournamentJoinDTO(1L, 3L), new TournamentJoinDTO(3L, 3L), new TournamentJoinDTO(4L, 3L),
            new TournamentJoinDTO(5L, 3L), new TournamentJoinDTO(6L, 3L), new TournamentJoinDTO(7L, 3L)
        };

        for (TournamentJoinDTO joinDTO : joinDTOs) {
            tournamentService.joinTournamentAsClub(joinDTO, jwtToken);
        }

        // Set player availability
        for (long i = 1; i <= 22; i += 7) {
            PlayerAvailabilityDTO playerAvailabilityDTO = new PlayerAvailabilityDTO(1L, i + 1, 2L, true);
            tournamentService.updatePlayerAvailability(playerAvailabilityDTO);
        }

        for (long i = 29; i <= 43; i += 7) {
            PlayerAvailabilityDTO playerAvailabilityDTO = new PlayerAvailabilityDTO(1L, i + 1, 2L, false);
            tournamentService.updatePlayerAvailability(playerAvailabilityDTO);
        }
    }
}