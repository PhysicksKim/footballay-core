package com.footballay.core.domain.user.repository;

import com.footballay.core.domain.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
