package com.footballay.core.infra.backbone.mock.resource

import com.footballay.core.common.result.DomainFail

internal fun validationFail(
    code: String,
    message: String,
    field: String,
): DomainFail.Validation =
    DomainFail.Validation.single(
        code = code,
        message = message,
        field = field,
    )
