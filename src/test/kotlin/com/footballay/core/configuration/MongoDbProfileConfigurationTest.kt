package com.footballay.core.configuration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.util.PropertyPlaceholderHelper
import java.util.Properties

/** MongoDB 공용 인프라 URI를 프로필별로 검증한다. */
class MongoDbProfileConfigurationTest {

    @Test
    fun `live profile requires the shared MongoDB URI environment variable`() {
        assertThat(properties("application-live.yml").getProperty("spring.data.mongodb.uri"))
            .isEqualTo("\${FOOTBALLAY_MONGODB_URI}")
    }

    @Test
    fun `live profile fails clearly when the shared MongoDB URI environment variable is missing`() {
        val uri = properties("application-live.yml").getProperty("spring.data.mongodb.uri")

        assertThatThrownBy {
            PropertyPlaceholderHelper("\${", "}", null, false)
                .replacePlaceholders(uri) { null }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("FOOTBALLAY_MONGODB_URI")
    }

    @Test
    fun `local profile selects the footballay database`() {
        assertThat(properties("application-local.yml").getProperty("spring.data.mongodb.uri"))
            .isEqualTo("mongodb://localhost:27017/footballay")
    }

    @Test
    fun `dev profile selects the footballay database`() {
        assertThat(properties("application-dev.yml").getProperty("spring.data.mongodb.uri"))
            .isEqualTo("mongodb://localhost:27017/footballay")
    }

    private fun properties(resourceName: String): Properties {
        val yamlProperties = YamlPropertiesFactoryBean().apply {
            setResources(ClassPathResource(resourceName))
        }

        return requireNotNull(yamlProperties.`object`)
    }
}
