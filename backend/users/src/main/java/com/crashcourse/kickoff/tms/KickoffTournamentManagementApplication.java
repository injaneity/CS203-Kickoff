package com.crashcourse.kickoff.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import com.crashcourse.kickoff.tms.player.model.PlayerPosition;
import com.crashcourse.kickoff.tms.security.SecurityConfig;
import com.crashcourse.kickoff.tms.user.dto.NewUserDTO;
import com.crashcourse.kickoff.tms.user.model.User;
import com.crashcourse.kickoff.tms.user.service.UserService;

@SpringBootApplication
public class KickoffTournamentManagementApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(KickoffTournamentManagementApplication.class, args);

		Environment env = ctx.getEnvironment();
		if (!env.acceptsProfiles(Profiles.of("prod"))) {
			// initialiseMockData(ctx);
			initialiseProdData(ctx);
		} else {
			initialiseProdData(ctx);
		}
	}

	private static void initialiseMockData(ApplicationContext ctx) {
		// User
		UserService userService = ctx.getBean(UserService.class);

		// Creating dummyUsers, each one name will be User0, User1, User2, ... and pw
		// will be password0, password1, password2, ...
		final int NUM_DUMMY_USERS = 50;
		final String[] dummyNames = {
				"Zane", "Liam", "Emma", "Noah", "AvaFootball", "Elijah", "Charlotte", "William",
				"Sophia", "James", "Amelia", "Benjamin", "Isabella", "Lucas", "MiaKickoff", "Henry",
				"Evelyn", "Alexander", "Harper", "Michael", "Camila", "Ethan", "Gianna", "Daniel",
				"Abigail", "Matthew", "Luna", "Joseph", "Ella", "Sebastian", "Elizabeth", "David",
				"Sofia", "Carter", "Emily", "Wyatt", "Avery", "John", "Mila", "Owen",
				"Scarlett", "Luke", "Eleanor", "Gabriel", "Madison", "Anthony", "Aria", "Isaac",
				"Grace", "Yekai"
		};

		// create users 1 to 50 (user 1 is admin) with id 2 to 51
		PlayerPosition[] POSITIONS = PlayerPosition.values();

		for (int i = 1; i <= NUM_DUMMY_USERS; i++) {
			// round-robin positions so that our demo is still deterministic
			String primaryPosition = POSITIONS[(i - 1) % POSITIONS.length].name();
			String[] positionArr = { primaryPosition };

			NewUserDTO dummyUserDTO = new NewUserDTO(dummyNames[i - 1] + i, "user" + i + "@email.com",
					"password" + i, positionArr, "player");

			try {
				User dummy = userService.addUser(dummyUserDTO);
				dummy = userService.addHostProfileToUser(dummy);
				System.out.println("[Added dummy user]: " + dummy.getUsername());
			} catch (IllegalArgumentException e) {
				System.out.println(dummyUserDTO.getUsername() + " has been created!");
			}

		}
		
		NewUserDTO adminDTO = new NewUserDTO("admin", "admin@email.com", "password",
				new String[] { "POSITION_Goalkeeper", "POSITION_Midfielder" }, "player");
		try {
			User admin = userService.addUser(adminDTO);
			admin = userService.addRolesToUser(admin, SecurityConfig.getAllRolesAsSet());
			admin = userService.addHostProfileToUser(admin);
			System.out.println("[Added admin]: " + admin.getUsername());
		} catch (IllegalArgumentException e) {
			System.out.println("admin has been created!");
		}
	}

	private static void initialiseProdData(ApplicationContext ctx) {
		// User
		UserService userService = ctx.getBean(UserService.class);
		
		NewUserDTO adminDTO = new NewUserDTO("admin", "admin@email.com", "password",
				null, "admin");
		try {
			User admin = userService.addUser(adminDTO);
			admin = userService.addRolesToUser(admin, SecurityConfig.getAllRolesAsSet());
			System.out.println("[Added admin]: " + admin.getUsername());
		} catch (IllegalArgumentException e) {
			System.out.println("admin has been created!");
		}
	}
}