package com.footballay.core.infra.apisports.match.plan.context

import com.footballay.core.logger

/**
 * MatchPlayer에서 사용하는 키 생성 로직을 관리합니다.
 *
 * 키 생성 전략:
 * - apiId가 존재하는 경우: "mp_id_" + apiId
 * - apiId가 없는 경우: "mp_name_" + name
 *
 * 이 클래스는 MatchPlayer의 고유 식별자를 생성하는 책임만 담당합니다.
 */
object MatchPlayerKeyGenerator {
    const val ID_PREFIX = "mp_id_"
    const val NAME_PREFIX = "mp_name_"

    val log = logger()

    /**
     * 선수 정보를 기반으로 MatchPlayer 키를 생성합니다.
     *
     * @param apiId 선수의 ApiSports ID (nullable)
     * @param name 선수 이름 (필수)
     * @return MatchPlayer 키 문자열
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     */
    fun generateMatchPlayerKey(
        apiId: Long?,
        name: String,
    ): String {
        require(!name.isBlank()) { "선수 이름은 필수입니다. name: $name" }

        return if (apiId != null) {
            generateKeyById(apiId)
        } else {
            generateKeyByName(name)
        }
    }

    /**
     * apiId를 기반으로 키를 생성합니다.
     *
     * @param apiId 선수의 ApiSports ID
     * @return "mp_id_" + apiId 형태의 키
     */
    fun generateKeyById(apiId: Long): String = "$ID_PREFIX$apiId"

    /**
     * 이름을 기반으로 키를 생성합니다.
     *
     * @param name 선수 이름
     * @return "mp_name_" + name 형태의 키
     */
    fun generateKeyByName(name: String): String {
        require(!name.isBlank()) { "선수 이름은 필수입니다. name: $name" }
        return "$NAME_PREFIX${name.trim()}"
    }

    /**
     * 키가 ID 기반인지 확인합니다.
     *
     * @param key MatchPlayer 키
     * @return ID 기반 키인 경우 true, 이름 기반 키인 경우 false
     */
    fun isIdBasedKey(key: String): Boolean = key.startsWith(ID_PREFIX)

    /**
     * 키가 이름 기반인지 확인합니다.
     *
     * @param key MatchPlayer 키
     * @return 이름 기반 키인 경우 true, ID 기반 키인 경우 false
     */
    fun isNameBasedKey(key: String): Boolean = key.startsWith(NAME_PREFIX)

    /**
     * ID 기반 키에서 apiId를 추출합니다.
     *
     * @param key MatchPlayer 키
     * @return apiId (키가 ID 기반이 아닌 경우 null)
     */
    fun extractApiIdFromKey(key: String): Long? =
        if (isIdBasedKey(key)) {
            key.substring(ID_PREFIX.length).toLongOrNull()
        } else {
            null
        }

    /**
     * 이름 기반 키에서 선수 이름을 추출합니다.
     *
     * @param key MatchPlayer 키
     * @return 선수 이름 (키가 이름 기반이 아닌 경우 null)
     */
    fun extractNameFromKey(key: String): String? =
        if (isNameBasedKey(key)) {
            key.substring(NAME_PREFIX.length)
        } else {
            null
        }
}
