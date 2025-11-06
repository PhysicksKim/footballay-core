package com.footballay.core

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestClient

/**
 * 제네릭 타입 정보를 담은 ParameterizedTypeReference 를 생성
 */
inline fun <reified T> parameterizedTypeReference(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

/**
 * WebClient.retrieve() 이후에 바로 쓰기 편한 확장 (body)
 */
inline fun <reified T> RestClient.ResponseSpec.bodyObject(): T? = this.body(parameterizedTypeReference<T>())
