package com.footballay.core.domain.admin.seasoncore

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeasonCoreMigrationReadinessServiceTest {
    @Autowired
    private lateinit var readinessService: SeasonCoreMigrationReadinessService

    @Test
    fun `empty database readiness check executes all validation queries`() {
        val report = readinessService.check()

        assertThat(report.ready).isTrue()
        assertThat(report.errorCount).isZero()
        assertThat(report.issues).isNotEmpty
        assertThat(report.issues).allSatisfy { issue ->
            assertThat(issue.count).isZero()
            assertThat(issue.affectedIds).isEmpty()
        }
    }
}
