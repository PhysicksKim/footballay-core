package com.footballay.core.domain.sync.service

import org.springframework.stereotype.Service

@Service
class ApiSportSyncService (
    private val apiSportsRequestService: ApiSportsRequestService,
){

    fun syncCurrentLeagues() {
        // (1) API 에 현재 리그들 요청 보냄
        // (2) 파싱해서 DB 에 저장 로직으로 보냄
        // (3) 후처리 로직 및 로깅 로직으로 보냄
        // (4) 결과를 반환함
    }
}