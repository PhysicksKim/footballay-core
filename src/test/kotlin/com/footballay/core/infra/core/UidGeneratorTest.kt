package com.footballay.core.infra.core

import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("dev", "devrealapi")
@Disabled("실제 API를 호출하므로 기본 테스트에서는 제외합니다")
class UidGeneratorTest {
    val log = logger()

    @Autowired
    lateinit var uidGenerator: UidGenerator

    @Test
    fun `generateUid는 유효한 UID를 반환해야 한다`() {
        val uid = uidGenerator.generateUid()
        log.info("Generated UID: $uid")
        assert(uidGenerator.isValidUid(uid)) { "Generated UID is not valid" }
    }

}
