package com.footballay.core.web.common.dto;

public record ApiResponse<T>(MetaData metaData, T[] response) {
}
