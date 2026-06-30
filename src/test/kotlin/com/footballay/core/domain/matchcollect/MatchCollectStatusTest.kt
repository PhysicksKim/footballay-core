package com.footballay.core.domain.matchcollect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MatchCollectStatusTest {
    @Test
    fun `incomplete statuses는 운영자 확인 필요 상태만 포함한다`() {
        assertThat(MatchCollectStatus.INCOMPLETE_STATUSES)
            .containsExactly(
                MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
                MatchCollectStatus.FAIL_END,
            )
    }

    @Test
    fun `isIncomplete은 운영자 확인 필요 상태에만 true를 반환한다`() {
        val incompleteStatuses = MatchCollectStatus.entries.filter { it.isIncomplete() }

        assertThat(incompleteStatuses)
            .containsExactly(
                MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
                MatchCollectStatus.FAIL_END,
            )
    }
}
