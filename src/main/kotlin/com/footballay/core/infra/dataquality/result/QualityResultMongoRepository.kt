package com.footballay.core.infra.dataquality.result

import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface QualityResultMongoRepository :
    MongoRepository<QualityResultDocument, String>,
    QualityResultMongoRepositoryCustom
