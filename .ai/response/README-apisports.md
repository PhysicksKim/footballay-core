# Fixture 요청 예시

```
https://v3.football.api-sports.io/fixtures?id=1208397&timezone=Asia%2FSeoul
```

위와 같은 형태로 요청합니다. `fixtures` 는 endpoint 세부 위치에 해당하면 그 뒤에 쿼리 파라미터로 여러 요청 사항을 제공합니다.

요청에는 Header 로 api key 를 담아야 합니다.

```
x-rapidapi-key : 49__CENSORED__9c
```

이렇게 담으며 api key 는 spring profile 로 devrealapi 활성화 시 `rapidapi.football.key` 로 key 를 얻을 수 있습니다.
응답 json 예시는 `./apisports_fixture.json` 를 참고하면 됩니다.
