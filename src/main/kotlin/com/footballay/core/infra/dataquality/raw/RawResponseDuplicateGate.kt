package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult

interface RawResponseDuplicateGate {
    fun checkAndStore(command: RawResponseDuplicateCheckCommand): RawResponseDuplicateCheckResult
}

class NoopRawResponseDuplicateGate : RawResponseDuplicateGate {
    override fun checkAndStore(command: RawResponseDuplicateCheckCommand): RawResponseDuplicateCheckResult =
        RawResponseDuplicateCheckResult.New
}
