package com.footballay.core

import com.fasterxml.jackson.databind.ObjectMapper

fun prettyPrintJson(objectMapper: ObjectMapper, response: Any): String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response)

