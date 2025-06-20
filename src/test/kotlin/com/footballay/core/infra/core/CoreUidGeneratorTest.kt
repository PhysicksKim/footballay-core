package com.footballay.core.infra.core

import com.footballay.core.infra.core.util.CoreUidGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

internal class CoreUidGeneratorTest {

    private val UidLength = 16
    private val generator = CoreUidGenerator()
    private val allowedChars = CoreUidGenerator.ALLOWED_CHARS.toSet()

    @DisplayName("UID 가 허용문자조합에 따라서 생성되었는지 검사합니다")
    @Test
    fun `허용 문자 조합에 따라 UID가 생성되는지 확인한다`() {
        val uid = generator.generateUid()
        // 길이 검증
        assertThat(uid.length).isEqualTo(uid.length).withFailMessage("UID length must be $UidLength")
        // 허용 문자 검증
        uid.all {
            it in allowedChars
        }.also { isValid ->
            assertThat(isValid).isTrue.withFailMessage("UID must only contain characters: ${CoreUidGenerator.ALLOWED_CHARS}")
        }
    }

    @DisplayName("주어진 String 이 유효한 UID 형식인지 검사합니다")
    @Test
    fun `유효하지 않은 UID는 false를 반환한다`() {
        assertThat(generator.isValidUid("a")).isFalse()
            .withFailMessage("Too short UID should be invalid")
        assertThat(generator.isValidUid("x".repeat(CoreUidGenerator.UID_LENGTH + 1))).isFalse()
            .withFailMessage("Too long UID should be invalid")
        assertThat(generator.isValidUid("")).isFalse()
            .withFailMessage("Empty string should be invalid")
    }

}