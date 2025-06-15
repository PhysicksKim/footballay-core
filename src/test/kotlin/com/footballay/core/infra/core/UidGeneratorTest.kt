package com.footballay.core.infra.core

import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("dev","devrealapi")
class UidGeneratorTest {

    val log = logger()

    @Autowired
    lateinit var uidGenerator: UidGenerator

    @Test
    fun `generateUid should return a valid UID`() {
        val uid = uidGenerator.generateUid()
        log.info("Generated UID: $uid")
        assert(uidGenerator.isValidUid(uid)) { "Generated UID is not valid" }
    }

    // log.info("generator class : ${uidGenerator::class.java.simpleName}, generated UID: $uid")

}