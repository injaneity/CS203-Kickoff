package com.crashcourse.kickoff.tms;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.crashcourse.kickoff.tms.host.HostProfile;
import com.crashcourse.kickoff.tms.host.HostProfileService;
import com.crashcourse.kickoff.tms.player.service.PlayerProfileService;
import com.crashcourse.kickoff.tms.security.SecurityConfig;
import com.crashcourse.kickoff.tms.tournament.dto.TournamentCreateDTO;
import com.crashcourse.kickoff.tms.tournament.model.KnockoutFormat;
import com.crashcourse.kickoff.tms.tournament.model.TournamentFormat;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.service.UserService;

@SpringBootApplication
public class KickoffTournamentManagementApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(KickoffTournamentManagementApplication.class, args);

		initialiseMockData(ctx);
	}

	private static void initialiseMockData(ApplicationContext ctx) {
		// User
		UserService userService = ctx.getBean(UserService.class);
		PlayerProfileService playerProfileService = ctx.getBean(PlayerProfileService.class);
		HostProfileService hostProfileService = ctx.getBean(HostProfileService.class);
		BCryptPasswordEncoder encoder = ctx.getBean(BCryptPasswordEncoder.class);

		// Creating admin user id 1
		NewUserDTO adminDTO = new NewUserDTO("admin", "admin@email.com", "password",
				new String[] { "POSITION_Goalkeeper", "POSITION_Midfielder" }, "player");
		User admin = userService.addUser(adminDTO);
		admin.setRoles(SecurityConfig.getAllRolesAsSet());
		admin = userService.save(admin);
		HostProfile adminHostProfile = hostProfileService.addHostProfile(admin);
		System.out.println("[Added admin]: " + admin.getUsername());

		// Creating dummyUsers, each one name will be User0, User1, User2, ... and pw will be password0, password1, password2, ...
		final int NUM_DUMMY_USERS = 50;

		// create users 1 to 60 (user 0 is admin)
		for (int i = 1; i <= NUM_DUMMY_USERS; i++) {
			NewUserDTO dummyUserDTO = new NewUserDTO("User" + i, "user" + i + "@email.com",
					"password" + i, new String[] { "POSITION_Goalkeeper", "POSITION_Midfielder" }, "player"); // now all players are goalkeeper and midfielders, can rand later
			User dummy = userService.addUser(dummyUserDTO);
			// playerProfileService.addPlayerProfile(dummy, dummyUserDTO);
			System.out.println("[Added dummy user]: " + dummy.getUsername());
		}
	}
}
