package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class DataQualityNoopConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(DataQualityNoopConfig::class.java)

    @Test
    fun `registers noop beans without external infrastructure`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(ApiSportsRawResponseCollector::class.java)
            assertThat(context).hasSingleBean(RawResponseDuplicateGate::class.java)
            assertThat(context).hasSingleBean(RawResponseStorage::class.java)
            assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
        }
    }
}
