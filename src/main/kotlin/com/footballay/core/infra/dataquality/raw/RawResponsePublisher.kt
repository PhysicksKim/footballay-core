package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent

interface RawResponsePublisher {
    fun publish(event: RawResponseCollectedEvent)
}

class NoopRawResponsePublisher : RawResponsePublisher {
    override fun publish(event: RawResponseCollectedEvent) = Unit
}
