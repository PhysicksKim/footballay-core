package com.footballay.core.infra.dispatcher.match

import org.quartz.JobKey

/**
 * Job 실행 컨텍스트
 *
 * Job에서 Dispatcher를 호출할 때 현재 Job의 정보를 전달합니다.
 * Dispatcher는 이 정보를 바탕으로 Job 전환을 결정할 수 있습니다.
 *
 * @property jobPhase 현재 Job의 단계 (PRE_MATCH, LIVE_MATCH, POST_MATCH)
 * @property jobKey 현재 실행 중인 Job의 Key (Job 삭제에 사용)
 */
data class JobContext(
    val jobPhase: JobPhase,
    val jobKey: JobKey,
) {
    /**
     * Job 단계
     */
    enum class JobPhase {
        PRE_MATCH,
        LIVE_MATCH,
        POST_MATCH,
    }

    companion object {
        /**
         * PreMatchJob 컨텍스트 생성
         */
        fun preMatch(jobKey: JobKey): JobContext = JobContext(JobPhase.PRE_MATCH, jobKey)

        /**
         * LiveMatchJob 컨텍스트 생성
         */
        fun liveMatch(jobKey: JobKey): JobContext = JobContext(JobPhase.LIVE_MATCH, jobKey)

        /**
         * PostMatchJob 컨텍스트 생성
         */
        fun postMatch(jobKey: JobKey): JobContext = JobContext(JobPhase.POST_MATCH, jobKey)
    }
}

