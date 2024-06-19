package com.gyechunsik.scoreboard.web.common.dto;

public record ApiResponse<T>(MetaData metaData, T[] response) {
}
