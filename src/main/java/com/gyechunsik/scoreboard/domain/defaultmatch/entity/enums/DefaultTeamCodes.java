package com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DefaultTeamCodes {

    // EPL2324
    ntg(CONST.EPL2324, "Nottingham_forest", "노팅엄"),
    nwc(CONST.EPL2324, "Newcastle_united", "뉴캐슬"),
    lut(CONST.EPL2324, "Luton_town", "루턴타운"),
    liv(CONST.EPL2324, "Liverpool", "리버풀"),
    mci(CONST.EPL2324, "Manchester_city", "맨시티"),
    mun(CONST.EPL2324, "Manchester_united", "맨유"),
    bur(CONST.EPL2324, "Burnley", "번리"),
    bou(CONST.EPL2324, "Bournemouth", "본머스"),
    brh(CONST.EPL2324, "Brighton", "브라이튼"),
    bre(CONST.EPL2324, "Brentford", "브렌트포드"),
    shu(CONST.EPL2324, "Sheffield_united", "셰필드"),
    ars(CONST.EPL2324, "Arsenal", "아스널"),
    ava(CONST.EPL2324, "Aston_villa", "아스톤빌라"),
    eve(CONST.EPL2324, "Everton", "에버튼"),
    wlv(CONST.EPL2324, "Wolverhampton", "울버햄튼"),
    whu(CONST.EPL2324, "West_ham_united", "웨스트햄"),
    che(CONST.EPL2324, "Chelsea", "첼시"),
    cry(CONST.EPL2324, "Crystal_palace", "C.팰리스"),
    tot(CONST.EPL2324, "Tottenham_hotspur", "토트넘"),
    ful(CONST.EPL2324, "Fulham", "풀럼"),

    // Nation
    kr(CONST.NATION, "South_korea", "대한민국"),
    bh(CONST.NATION, "Bahrain", "바레인"),
    jo(CONST.NATION, "Jordan", "요르단"),
    my(CONST.NATION, "Malaysia", "말레이시아"),
    sa(CONST.NATION, "Saudi_arabia", "사우디아라비아"),
    au(CONST.NATION, "Australia", "호주"),
    jp(CONST.NATION, "Japan", "일본"),
    ir(CONST.NATION, "Iran", "이란"),

    // ETC (cup) teams
    cup(CONST.ETC, "Cup_team", "기타 컵팀 대비용"),
    ety(CONST.ETC, "Empty_team", "비어있는 팀로고");

    private final String category;
    private final String name;
    private final String koreaName;

    DefaultTeamCodes(String category, String name, String koreaName) {
        this.category = category;
        this.name = name;
        this.koreaName = koreaName;
    }

    @Override
    public String toString() {
        return name;
    }

    private static class CONST {
        private static final String EPL2324 = LeagueCategory.epl2324.getValue();
        private static final String NATION = LeagueCategory.nation.getValue();
        private static final String ETC = LeagueCategory.etc.getValue();
    }

    public static DefaultTeamCodes of(String code) {
        return Arrays.stream(DefaultTeamCodes.values())
                .filter(teamCode -> teamCode.getName().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DefaultTeamCodes code: " + code));
    }
}
