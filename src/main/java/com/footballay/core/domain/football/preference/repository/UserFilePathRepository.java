package com.footballay.core.domain.football.preference.repository;

import com.footballay.core.domain.football.preference.persistence.UserFilePath;
import com.footballay.core.domain.football.preference.persistence.UserPathCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFilePathRepository extends JpaRepository<UserFilePath, Long> {

    boolean existsByUserPathHash(String userPathHash);

    Optional<UserFilePath> findByUserIdAndUserPathCategory(Long userId, UserPathCategory userPathCategory);

}
