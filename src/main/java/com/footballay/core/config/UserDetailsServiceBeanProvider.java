package com.footballay.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
public class UserDetailsServiceBeanProvider {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        
        // 사용자 조회 쿼리에 스키마 명시
        userDetailsManager.setUsersByUsernameQuery(
                "select username, password, enabled " +
                        "from footballay_core.users " +
                        "where username = ?"
        );
        
        // 권한 조회 쿼리에 스키마 명시
        userDetailsManager.setAuthoritiesByUsernameQuery(
                "select u.username, a.authority " +
                        "from footballay_core.users u inner join footballay_core.authorities a on u.id = a.user_id " +
                        "where u.username = ?"
        );
        
        return userDetailsManager;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        FootballayCoreSchemeTokenRepository tokenRepository = new FootballayCoreSchemeTokenRepository();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

}
