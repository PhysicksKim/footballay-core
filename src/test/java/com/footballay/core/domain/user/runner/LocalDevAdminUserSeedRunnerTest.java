package com.footballay.core.domain.user.runner;

import com.footballay.core.domain.user.entity.Authority;
import com.footballay.core.domain.user.entity.Role;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDevAdminUserSeedRunnerTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsLocalDevAdminIfMissing() {
        when(userRepository.findByUsername(LocalDevAdminUserSeedRunner.ADMIN_USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(LocalDevAdminUserSeedRunner.ADMIN_PASSWORD)).thenReturn("{bcrypt}encoded");

        runner().run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("qwer");
        assertThat(saved.getNickname()).isEqualTo("로컬개발ADMIN");
        assertThat(saved.getPassword()).isEqualTo("{bcrypt}encoded");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getAuthorities()).hasSize(1);
        Authority authority = saved.getAuthorities().iterator().next();
        assertThat(authority.getUser()).isSameAs(saved);
        assertThat(authority.getAuthority()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updatesExistingUserWithoutDuplicatingAdminRole() {
        User user = User.builder()
                .username(LocalDevAdminUserSeedRunner.ADMIN_USERNAME)
                .nickname("old")
                .password("{bcrypt}old")
                .enabled(false)
                .build();
        Authority authority = new Authority();
        authority.setUser(user);
        authority.setAuthority(Role.ROLE_ADMIN);
        user.getAuthorities().add(authority);
        when(userRepository.findByUsername(LocalDevAdminUserSeedRunner.ADMIN_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(LocalDevAdminUserSeedRunner.ADMIN_PASSWORD, "{bcrypt}old")).thenReturn(false);
        when(passwordEncoder.encode(LocalDevAdminUserSeedRunner.ADMIN_PASSWORD)).thenReturn("{bcrypt}new");

        runner().run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getNickname()).isEqualTo("로컬개발ADMIN");
        assertThat(saved.getPassword()).isEqualTo("{bcrypt}new");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getAuthorities())
                .extracting(Authority::getAuthority)
                .containsExactly(Role.ROLE_ADMIN);
    }

    private LocalDevAdminUserSeedRunner runner() {
        return new LocalDevAdminUserSeedRunner(userRepository, passwordEncoder);
    }
}
