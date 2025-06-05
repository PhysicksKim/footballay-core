package com.footballay.core.infra.provider.dto

import java.time.LocalDate

/**
 * ApiSports로부터 넘어오는 선수 정보 중 최소한으로 필요한 필드
 */
data class ApiPlayerDto(
    val apiId: Long,
    val name: String,
    val firstname: String?,
    val lastname: String?,
    val age: Int?,
    val birthDate: LocalDate?,
    val birthPlace: String?,
    val birthCountry: String?,
    val nationality: String?,
    val height: String?,
    val weight: String?,
    val injured: Boolean = false,
    val photo: String?,
    val position: String?,
    val number: Int?
) 