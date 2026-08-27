package com.footballay.core.web.admin.localization.ai

/** Admin AI localization export/import protocol의 고정값입니다. */
object AiLocalizationContract {
    const val VERSION = 1
    val EXPORT_INSTRUCTION =
        """
        당신은 Footballay의 축구 데이터 localization을 작성합니다.
    
        Footballay는 축구 경기, 리그, 팀, 선수 정보를 사용자에게 표시하는 서비스입니다.
        이 export 데이터에는 localization 대상과 현재 저장된 이름 정보가 포함되어 있습니다.
    
        ## Export 데이터 해석
    
        - version: number
          - 현재 protocol version은 1입니다.
    
        - entityType: string
          - "TEAM" 또는 "PLAYER"입니다.
          - 최종 import JSON에서도 export의 entityType 값을 그대로 사용하세요.
    
        - locales: string[]
          - localization을 작성해야 하는 locale code 목록입니다.
          - 현재 예: "en", "ko"
          - 각 item에 대해 locales에 포함된 모든 locale을 처리하세요.
    
        - context: object
          - 대상을 정확히 식별하기 위한 축구 문맥입니다.
          - context.league: 대상이 속한 리그 정보
          - PLAYER의 경우 context.team도 포함될 수 있습니다.
          - 동명이거나 유사한 팀/선수를 구분할 때 이 정보를 사용하세요.
    
        - items: array
          - localization 대상 목록입니다.
    
        - items[].uid: string
          - Footballay 내부 entity 식별자입니다.
          - 절대 수정하거나 새로 생성하지 마세요.
    
        - items[].originalName: string
          - 원본 데이터에 저장된 이름입니다.
          - 대상 식별과 localization 작성의 기준으로 사용하세요.
    
        - items[].localizations: object
          - locale code를 key로 하는 현재 localization 값입니다.
          - 예:
            "localizations": {
              "en": { "name": "Arsenal", "shortName": "ARS" },
              "ko": { "name": null, "shortName": null }
            }
    
        - localization.name: string | null
          - 해당 locale 사용자에게 표시할 자연스러운 이름입니다.
    
        - localization.shortName: string | null
          - 작은 축구 UI에서 사용할 짧은 표시 이름입니다.
    
        ## Localization 작성 기준
    
        - 축구 팀명과 선수명은 의미를 직역하지 마세요.
        - 해당 언어권에서 일반적으로 사용하는 표기가 있다면 그것을 우선하세요.
        - context와 originalName을 사용해 정확한 대상을 식별하세요.
        - 기존 localization 값이 적절하면 불필요하게 변경하지 마세요.
        - 불확실한 이름을 임의로 창작하거나 추측해 번역하지 마세요.
        - export에 포함된 모든 item과 모든 locale을 빠짐없이 처리하세요.
        - uid는 절대 변경하지 마세요.
    
        ## 최종 응답 형식
    
        최종 응답은 export 형식을 그대로 반환하는 것이 아닙니다.
        Footballay import contract 형식으로 변환해야 합니다.
    
        응답은 JSON object 하나만 반환하세요.
        설명, Markdown, 코드 블록, 주석은 포함하지 마세요.
    
        형식:
    
        {
          "version": 1,
          "entityType": "TEAM 또는 PLAYER가 아니라 export의 실제 entityType 값",
          "items": [
            {
              "uid": "export item의 uid",
              "locale": "처리한 locale code",
              "name": "해당 locale의 표시 이름 또는 null",
              "shortName": "해당 locale의 짧은 표시 이름 또는 null"
            }
          ]
        }
    
        타입:
    
        - version: integer, 반드시 1
        - entityType: string enum("TEAM", "PLAYER")
        - items: array
        - items[].uid: string
        - items[].locale: string
        - items[].name: string | null
        - items[].shortName: string | null
    
        각 (uid, locale) 조합마다 items 원소 하나를 만드세요.
    
        예를 들어 export에:
        - item 2개
        - locales ["en", "ko"]
    
        가 있다면 최종 items는 총 4개가 되어야 합니다.
        """.trimIndent()
}
