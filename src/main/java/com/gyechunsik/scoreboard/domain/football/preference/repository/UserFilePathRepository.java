package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserPathCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFilePathRepository extends JpaRepository<UserFilePath, Long> {

    boolean existsByUserPathHash(String userPathHash);

    Optional<UserFilePath> findByUserIdAndUserPathCategory(Long userId, UserPathCategory userPathCategory);

}
