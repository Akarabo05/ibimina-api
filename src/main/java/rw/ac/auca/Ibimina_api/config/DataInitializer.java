package rw.ac.auca.Ibimina_api.config;

import rw.ac.auca.Ibimina_api.entity.AppUser;
import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.entity.Role;
import rw.ac.auca.Ibimina_api.repository.AppUserRepository;
import rw.ac.auca.Ibimina_api.repository.GroupRepository;
import rw.ac.auca.Ibimina_api.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository users;
    private final GroupRepository groups;
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final String adminUsername;
    private final String adminPassword;
    private final boolean seedDemo;

    public DataInitializer(AppUserRepository users, GroupRepository groups, MemberRepository members,
                           PasswordEncoder encoder,
                           @Value("${app.admin.username}") String adminUsername,
                           @Value("${app.admin.password}") String adminPassword,
                           @Value("${app.seed-demo}") boolean seedDemo) {
        this.users = users;
        this.groups = groups;
        this.members = members;
        this.encoder = encoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.seedDemo = seedDemo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;

        users.save(account(adminUsername, adminPassword, "admin@ibimina.local", Role.ADMIN, null));
        log.warn("Created first ADMIN account '{}'. Change its password in production.", adminUsername);

        if (seedDemo) {
            Group group = groups.save(new Group("Twisungane Cooperative", "MONTHLY", new BigDecimal("5000.00")));
            Member alice = members.save(new Member("Uwase Alice", "0788000001", "alice@example.com", group));
            members.save(new Member("Mugisha Eric", "0788000002", null, group));
            members.save(new Member("Ingabire Chantal", "0788000003", null, group));
            users.save(account("treasurer", "Treasurer@123", "treasurer@ibimina.local", Role.TREASURER, null));
            users.save(account("alice", "Member@12345", "alice@example.com", Role.MEMBER, alice));
            log.warn("Demo data created (users: treasurer, alice). Set SEED_DEMO=false to disable.");
        }
    }

    private AppUser account(String username, String password, String email, Role role, Member member) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setEmail(email);
        u.setRole(role);
        u.setMember(member);
        return u;
    }
}