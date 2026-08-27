package com.footballay.core.web.admin.localization.ai

/** Admin AI localization export/import protocol의 고정값입니다. */
object AiLocalizationContract {
    const val VERSION = 1

    val EXPORT_INSTRUCTION =
        """
        당신은 Footballay의 축구 데이터 localization을 작성합니다.

        Footballay는 축구 경기, 리그, 팀, 선수 정보를 사용자에게 표시하는 서비스입니다.
        아래 export 데이터에는 localization 대상과 현재 저장된 localization 정보가 포함되어 있습니다.

        ## 1. Export 데이터 구조

        - version: integer
          - export protocol version입니다.
          - 현재 값은 1입니다.

        - entityType: string
          - localization 대상 종류입니다.
          - 가능한 값은 "TEAM", "PLAYER"입니다.
          - 최종 import JSON에서도 export의 entityType 값을 그대로 사용하세요.

        - locales: string[]
          - 이번 작업에서 작성해야 하는 locale code 목록입니다.
          - 예: ["en", "ko"]
          - 모든 item에 대해 이 목록의 모든 locale을 처리하세요.

        - context: object
          - 대상 entity를 정확히 식별하기 위한 축구 문맥입니다.
          - context.league는 대상이 속한 League 정보입니다.
          - PLAYER export에서는 context.team이 포함됩니다.
          - 동명이거나 이름이 유사한 팀/선수를 구분할 때 반드시 참고하세요.

        - items: array
          - localization 대상 entity 목록입니다.

        - items[].uid: string
          - Footballay 내부 entity 식별자입니다.
          - 절대 수정하거나 새로 만들지 마세요.

        - items[].originalName: string
          - 원본 데이터의 이름입니다.
          - 대상 식별과 localization 작성의 기준으로 사용하세요.

        - items[].localizations: object
          - locale code를 key로 하는 현재 localization 값입니다.
          - 각 값의 형식은 다음과 같습니다.

            {
              "name": string | null,
              "shortName": string | null
            }

          - null은 현재 해당 값이 없다는 뜻입니다.

        ## 2. Localization 작성 기준

        - name은 해당 locale 사용자에게 자연스럽게 표시할 이름입니다.
        - 팀명과 선수명은 단어의 의미를 직역하지 마세요.
        - 해당 언어권에서 일반적으로 사용하는 축구 표기가 있다면 그것을 우선하세요.
        - context와 originalName을 이용해 정확한 대상을 식별하세요.
        - 이미 존재하는 localization 값이 적절하다면 불필요하게 변경하지 마세요.
        - 확실하지 않은 정보를 임의로 창작하거나 이름의 의미를 추측해 번역하지 마세요.
        - uid는 절대 변경하지 마세요.
        - export에 포함된 모든 item과 요청된 모든 locale을 빠짐없이 처리하세요.
        - name과 shortName은 각각 최대 255자입니다.
        - 요청된 locale에 대해 가능한 한 name과 shortName을 모두 작성하세요.
        - 적절한 값을 합리적으로 결정할 수 없는 경우에만 null을 사용하세요.

        ## 3. shortName 작성 기준

        shortName의 의미는 entityType에 따라 다릅니다.

        ### PLAYER

        - 선수의 shortName은 작은 축구 UI에서 빠르게 식별할 수 있는 짧고 자연스러운 선수 이름입니다.
        - 가능하면 한 단어 정도의 이름을 사용하세요.
        - 반드시 first name이나 last name을 사용해야 하는 것은 아닙니다.
        - 해당 선수를 축구 팬들이 가장 자연스럽게 식별할 수 있는 이름 부분을 우선하세요.
        - 문자 수를 기계적으로 맞추지 마세요. 알파벳은 글자별 표시 폭이 다르므로 고정 글자 수 제한을 기준으로 삼지 않습니다.
        - 너무 긴 전체 이름을 그대로 shortName으로 사용하지 마세요.
        - 이름을 임의의 이니셜이나 축약 코드로 만들지 마세요.

        예:
        - Cristiano Ronaldo → Ronaldo
        - Erling Haaland → Haaland
        - Mohamed Salah → Salah

        ### TEAM

        - 팀의 shortName은 약어 코드가 아니라, 해당 팀을 일반적으로 짧게 부르는 자연스러운 이름입니다.
        - 가능하면 하나의 대표적인 이름 단위로 줄이세요.
        - "TOT", "ARS", "MCI" 같은 3글자 팀 코드는 shortName으로 사용하지 마세요.
        - 이런 고정 약어 코드는 별도의 abbreviation/code 필드에서 다룰 대상이며 현재 shortName의 의미가 아닙니다.
        - 자연스럽게 줄일 수 없는 팀 이름은 억지로 축약하지 말고 일반적으로 사용되는 가장 짧은 이름을 사용하세요.
        - 이미 일반적으로 짧은 이름이면 억지로 더 줄이지 마세요.
        
        예:
        - Arsenal → Arsenal
        - Chelsea → Chelsea
        - Tottenham Hotspur → Tottenham
        - Newcastle United → Newcastle
        

        ## 4. 최종 응답 형식

        최종 응답은 export 구조를 그대로 반환하는 것이 아닙니다.
        Footballay AI localization import contract 형식으로 변환해야 합니다.

        최종 응답은 유효한 JSON object 하나만 반환하세요.
        설명, Markdown, 코드 블록, 주석, 앞뒤 문장은 포함하지 마세요.

        최종 JSON 구조:

        {
          "version": 1,
          "entityType": "TEAM",
          "items": [
            {
              "uid": "export item의 uid",
              "locale": "처리한 locale code",
              "name": "해당 locale의 표시 이름",
              "shortName": "해당 locale의 짧은 표시 이름"
            }
          ]
        }

        위 JSON의 "TEAM"은 형식 예시입니다.
        실제 응답에서는 export 데이터의 entityType 값을 그대로 사용하세요.

        Import field 규칙:

        - version: integer
          - 반드시 1

        - entityType: string
          - "TEAM" 또는 "PLAYER"
          - 반드시 export의 entityType과 동일한 값

        - items: array
          - 각 (uid, locale) 조합을 하나의 item으로 표현

        - items[].uid: string
          - export의 uid를 그대로 사용

        - items[].locale: string
          - export의 locales에 포함된 실제 locale code 사용

        - items[].name: string | null
          - 해당 locale의 표시 이름

        - items[].shortName: string | null
          - 해당 locale의 shortName 작성 기준에 맞는 짧은 표시 이름

        export의 items[].localizations처럼 locale을 object key로 묶지 마세요.
        import에서는 각 (uid, locale) 조합을 별도의 items 원소로 평탄화해야 합니다.

        예를 들어 export에 item이 2개이고 locales가 ["en", "ko"]라면,
        최종 import JSON의 items는 4개여야 합니다.

        최종 import JSON에는 다음 export 전용 field를 넣지 마세요.

        - locales
        - context
        - originalName
        - localizations
        - instruction

        아래는 이번 작업의 실제 export 데이터입니다.
        """.trimIndent()
}