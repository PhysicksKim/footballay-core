package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.Embeddable

/**
 * ApiSports 의 lineup 에서 제공되는 유니폼 색상이 샵(#)이 생략된 헥사코드로 제공됩니다.
 *
 * ```
 * "colors": {
 *     "player": {
 *         "primary": "ea0000",
 *         "number": "ffffff",
 *         "border": "ea0000"
 *     },
 *     "goalkeeper": {
 *         "primary": "b356f6",
 *         "number": "ffffff",
 *         "border": "b356f6"
 *     }
 * }
 * ```
 */
@Embeddable
data class UniformColor(
    val primary: String? = null,
    val number: String? = null,
    val border: String? = null,
)
