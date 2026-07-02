package com.footballay.core.infra.dataquality.raw

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

interface RawResponseCanonicalHasher {
    fun hash(rawJson: String): String
}

@Component
class DefaultRawResponseCanonicalHasher(
    objectMapper: ObjectMapper,
) : RawResponseCanonicalHasher {
    private val objectMapper: ObjectMapper =
        objectMapper
            .copy()
            .apply {
                configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                configure(SerializationFeature.INDENT_OUTPUT, false)
            }

    override fun hash(rawJson: String): String {
        val canonicalBytes = canonicalizeAsBytes(rawJson)
        val digest = MessageDigest.getInstance(SHA_256).digest(canonicalBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun canonicalizeAsBytes(rawJson: String): ByteArray {
        val root =
            try {
                objectMapper.readTree(rawJson)
            } catch (ex: JsonProcessingException) {
                throw IllegalArgumentException("Raw response body is not valid JSON", ex)
            }

        return objectMapper.writeValueAsBytes(canonicalize(root))
    }

    private fun canonicalize(node: JsonNode): JsonNode =
        when {
            node.isObject -> canonicalizeObject(node as ObjectNode)
            node.isArray -> canonicalizeArray(node as ArrayNode)
            else -> node.deepCopy()
        }

    private fun canonicalizeObject(node: ObjectNode): ObjectNode {
        val canonical = objectMapper.createObjectNode()
        node
            .fields()
            .asSequence()
            .sortedBy { it.key }
            .forEach { (fieldName, value) ->
                canonical.set<JsonNode>(fieldName, canonicalize(value))
            }
        return canonical
    }

    private fun canonicalizeArray(node: ArrayNode): ArrayNode {
        val canonical = objectMapper.createArrayNode()
        node.forEach { item ->
            canonical.add(canonicalize(item))
        }
        return canonical
    }

    private companion object {
        const val SHA_256 = "SHA-256"
    }
}
