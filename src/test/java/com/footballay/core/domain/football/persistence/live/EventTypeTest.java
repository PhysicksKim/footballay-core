package com.footballay.core.domain.football.persistence.live;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTypeTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTypeTest.class);

    @DisplayName("String Naming Convention To EventType")
    @Test
    void StringToEventType() {
        // given
        String goal_UPPERCASE = "GOAL";
        String goal_LOWERCASE = "goal";
        String goal_camelCase = "Goal";
        // when
        EventType goal1 = EventType.valueOf(goal_UPPERCASE);
        log.info("goal1 : {}", goal1);
        // then
        assertThat(goal1).isEqualTo(EventType.GOAL);
        assertThatThrownBy(() -> {
            EventType goal2 = EventType.valueOf(goal_LOWERCASE);
            log.error("Unexpected success: The lowercase string \'{}\' was converted to EventType \'{}\'.", goal_LOWERCASE, goal2);
        }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> {
            EventType goal3 = EventType.valueOf(goal_camelCase);
            log.error("Unexpected success: The camelCase string \'{}\' was converted to EventType \'{}\'.", goal_camelCase, goal3);
        }).isInstanceOf(IllegalArgumentException.class);
        log.info("lowercase And camelCase occur IllegalArgumentException while converting to EventType enum :: input={}", goal_LOWERCASE + ", " + goal_camelCase);
    }

    @DisplayName("4가지 EventType 변환 성공")
    @Test
    void FourEventTypeConverting() {
        // given
        String goal = "Goal";
        String card = "Card";
        String subst = "Subst";
        String var = "Var";
        // when
        EventType goalType = EventType.valueOf(goal.toUpperCase());
        EventType cardType = EventType.valueOf(card.toUpperCase());
        EventType substType = EventType.valueOf(subst.toUpperCase());
        EventType varType = EventType.valueOf(var.toUpperCase());
        // then
        assertThat(goalType).isEqualTo(EventType.GOAL);
        assertThat(cardType).isEqualTo(EventType.CARD);
        assertThat(substType).isEqualTo(EventType.SUBST);
        assertThat(varType).isEqualTo(EventType.VAR);
    }

    @DisplayName("EventType To String")
    @Test
    void EventTypeToString() {
        // given
        EventType goal = EventType.GOAL;
        EventType card = EventType.CARD;
        EventType subst = EventType.SUBST;
        EventType var = EventType.VAR;
        // when
        String goalString = goal.toString();
        String cardString = card.toString();
        String substString = subst.toString();
        String varString = var.toString();
        // then
        log.info("goalString : {}", goalString);
        log.info("cardString : {}", cardString);
        log.info("substString : {}", substString);
        log.info("varString : {}", varString);
        assertThat(goalString).isEqualTo("GOAL");
        assertThat(cardString).isEqualTo("CARD");
        assertThat(substString).isEqualTo("SUBST");
        assertThat(varString).isEqualTo("VAR");
    }

    @DisplayName("잘못된 String 으로 변환을 요청시 예외 발생")
    @Test
    void FailToParsing() {
        // given
        String WRONG_TYPE = "wrongType";
        // when & then
        assertThatThrownBy(() -> {
            EventType type = EventType.valueOf(WRONG_TYPE);
            log.error("Unexpected success: The invalid string \'{}\' was converted to EventType \'{}\'.", WRONG_TYPE, type);
        }).isInstanceOf(IllegalArgumentException.class);
        log.info("Expected exception occurred while converting the invalid string to EventType. :: input={}", WRONG_TYPE);
    }
}
