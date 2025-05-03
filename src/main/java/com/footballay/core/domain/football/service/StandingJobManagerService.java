package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.persistence.League;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StandingJobManagerService {

    /*
    job 은 매 정각 + 5분 (ex. 01:05, 02:05, ...) 에 실행된다.
    job 은 스프링이 닫혔다가 뜨게 되면 등록되도록 한다.

    job task 는 league 에서
    league.getIsStandingAvailable()
    가 true 인 리그를 가져오고 standing 저장 가능한 리그들을 대상으로만 request 를 날린다.

    request 는 queue 형태로 동작한다. 왜냐하면 api request limit per minute 이 존재하기 때문.
    queue 는 request 를 보내고 성공하면 이어서 다음 league standing request 를 날린다.
    만약 api 측에서 request limit 이 걸리면 가까운 새로운 minute 의 3초에 다시 request 를 날리도록 한다.
    예를 들어 13:01:23 에 request 를 날렸으나 limit 으로 인해 실패했다면, 해당 queue 가 13:02:03 에 다시 request 를 날리도록 한다.
     */

}
