package com.gyechunsik.scoreboard.domain.user.repository;

import com.gyechunsik.scoreboard.domain.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
