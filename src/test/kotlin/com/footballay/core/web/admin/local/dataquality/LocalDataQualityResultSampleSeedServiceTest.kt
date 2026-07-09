package com.footballay.core.web.admin.local.dataquality

import com.footballay.core.infra.dataquality.result.model.DataQualityCheckStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityMaxSeverity
import com.footballay.core.infra.dataquality.result.model.DataQualityMongoCollections
import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.data.mongodb.core.MongoTemplate

@ExtendWith(MockitoExtension::class)
class LocalDataQualityResultSampleSeedServiceTest {
    @Mock
    private lateinit var mongoTemplate: MongoTemplate

    @Captor
    private lateinit var documentCaptor: ArgumentCaptor<QualityResultDocument>

    @Test
    fun `seed upserts fixed sample quality result documents`() {
        val result = LocalDataQualityResultSampleSeedService(mongoTemplate).seed()

        verify(mongoTemplate, times(2)).save(documentCaptor.capture(), eq(DataQualityMongoCollections.QUALITY_RESULTS))

        val documents = documentCaptor.allValues
        assertThat(result.collection).isEqualTo(DataQualityMongoCollections.QUALITY_RESULTS)
        assertThat(result.seededCount).isEqualTo(2)
        assertThat(result.resultIds).containsExactly(
            "01JZLOCALDQ0000000000000001",
            "01JZLOCALDQ0000000000000002",
        )
        assertThat(documents.map { it.id }).containsExactlyElementsOf(result.resultIds)

        val needCheck = documents[0]
        assertThat(needCheck.checkStatus).isEqualTo(DataQualityCheckStatus.NEED_CHECK)
        assertThat(needCheck.hasIssue).isTrue()
        assertThat(needCheck.issueCount).isEqualTo(1)
        assertThat(needCheck.maxSeverity).isEqualTo(DataQualityMaxSeverity.WARN)
        assertThat(needCheck.parameters.single().name).isEqualTo("fixtureId")
        assertThat(needCheck.parameters.single().value).isEqualTo("1208397")

        val noIssue = documents[1]
        assertThat(noIssue.checkStatus).isEqualTo(DataQualityCheckStatus.NO_ISSUE)
        assertThat(noIssue.hasIssue).isFalse()
        assertThat(noIssue.issueCount).isZero()
        assertThat(noIssue.maxSeverity).isEqualTo(DataQualityMaxSeverity.NONE)
        assertThat(noIssue.issues).isEmpty()
    }
}
