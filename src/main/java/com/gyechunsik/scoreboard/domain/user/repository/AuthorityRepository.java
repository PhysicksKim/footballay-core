package com.gyechunsik.scoreboard.domain.user.repository;

import com.gyechunsik.scoreboard.domain.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
