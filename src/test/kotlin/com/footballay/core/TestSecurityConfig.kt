package com.footballay.core

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@EnableWebSecurity
class TestSecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val admin = User.withUsername("admin")
            .password("{noop}password")
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            }
            .httpBasic { }
            .build()
    }
}
