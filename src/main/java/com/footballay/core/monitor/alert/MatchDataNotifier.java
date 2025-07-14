package com.footballay.core.monitor.alert;

public interface MatchDataNotifier {

    /**
     * 정상적으로 처리된 경우
     * @param fixtureId 대상 경기 ID
     * @param message 처리 완료 메시지 (예: "라인업 저장 완료", "이벤트 10개 저장")
     */
    void notifySuccess(String fixtureId, String message) throws NotificationException;

    /**
     * 처리 실패(비정상 종료)한 경우
     * @param fixtureId 대상 경기 ID
     * @param message 실패 사유 메시지
     */
    void notifyFailure(String fixtureId, String message) throws NotificationException;

    /**
     * 예외 발생 시
     * @param fixtureId 대상 경기 ID
     * @param errorMessage 예외 메시지(스택트레이스 요약 등)
     */
    void notifyException(String fixtureId, String errorMessage) throws NotificationException;

}