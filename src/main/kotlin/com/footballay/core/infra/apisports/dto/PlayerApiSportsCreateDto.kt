package com.footballay.core.infra.apisports.dto

data class PlayerApiSportsCreateDto(
    val apiId: Long?, // API 응답의 player.id 불완전한 경우 null 존재 가능
    val name: String? = null, // API 응답의 player.name
    val firstname: String? = null, // API 응답의 player.firstname
    val lastname: String? = null, // API 응답의 player.lastname
    val age: Int? = null, // API 응답의 player.age
    val birthDate: String? = null, // API 응답의 player.birth.date
    val birthPlace: String? = null, // API 응답의 player.birth.place
    val birthCountry: String? = null,
    val nationality: String? = null,
    val height: String? = null, // API 응답의 player.height
    val weight: String? = null, // API 응답의 player.weight
    val number: Int? = null, // API 응답의 player.number
    val position: String? = null, // API 응답의 player.position
    val photo: String? = null // API 응답의 player.photo
)