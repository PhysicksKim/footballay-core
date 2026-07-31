package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import com.footballay.core.logger

interface RawResponseStorage {
    fun upload(command: RawResponseUploadCommand): RawResponseStoredObject
}

class NoopRawResponseStorage : RawResponseStorage {
    private val log = logger()

    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject {
        log.info("No-op Raw Response upload")
        return RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)
    }

}
