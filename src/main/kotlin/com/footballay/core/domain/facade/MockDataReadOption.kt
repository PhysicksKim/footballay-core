package com.footballay.core.domain.facade

data class MockDataReadOption(
    val includeMockData: Boolean = false,
) {
    companion object {
        val DEFAULT = MockDataReadOption()
    }
}
