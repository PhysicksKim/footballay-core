package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectionCommand

interface ApiSportsRawResponseCollector {
    fun collect(command: RawResponseCollectionCommand)
}

class NoopApiSportsRawResponseCollector : ApiSportsRawResponseCollector {
    override fun collect(command: RawResponseCollectionCommand) = Unit
}
