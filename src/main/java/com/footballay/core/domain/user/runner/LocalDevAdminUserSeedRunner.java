package com.footballay.core.domain.user.runner;

import com.footballay.core.domain.user.entity.Authority;
import com.footballay.core.domain.user.entity.Role;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@Profile({"local", "dev"})
public class LocalDevAdminUserSeedRunner implements ApplicationRunner {
    static final String ADMIN_USERNAME = "qwer";
    static final String ADMIN_PASSWORD = "qwer";
    static final String ADMIN_NICKNAME = "로컬개발ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalDevAdminUserSeedRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository
                .findByUsername(ADMIN_USERNAME)
                .orElseGet(this::createAdminUser);

        ensureLocalAdminUser(user);
        userRepository.save(user);
    }

    private User createAdminUser() {
        return User.builder()
                .username(ADMIN_USERNAME)
                .nickname(ADMIN_NICKNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .enabled(true)
                .build();
    }

    private void ensureLocalAdminUser(User user) {
        if (!Objects.equals(user.getNickname(), ADMIN_NICKNAME)) {
            user.setNickname(ADMIN_NICKNAME);
        }
        if (!user.isEnabled()) {
            user.setEnabled(true);
        }
        if (user.getPassword() == null || !passwordEncoder.matches(ADMIN_PASSWORD, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        }
        if (user.getAuthorities().stream().noneMatch(authority -> authority.getAuthority() == Role.ROLE_ADMIN)) {
            Authority authority = new Authority();
            authority.setUser(user);
            authority.setAuthority(Role.ROLE_ADMIN);
            user.getAuthorities().add(authority);
        }
    }
}
