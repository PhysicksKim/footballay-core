package com.gyechunsik.scoreboard.domain.football.comparator;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.sun.jna.platform.unix.solaris.Kstat2;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class StartLineupComparator implements Comparator<MatchPlayer> {
    private static final Map<String, Integer> POSITION_PRIORITY;

    static {
        POSITION_PRIORITY = new HashMap<>();
        POSITION_PRIORITY.put("G", 1);
        POSITION_PRIORITY.put("D", 2);
        POSITION_PRIORITY.put("M", 3);
        POSITION_PRIORITY.put("F", 4);
    }

    /**
     * 1) 포지션 순 정렬 (G -> D -> M -> F) <br>
     * 2) 등번호 순으로 정렬 <br>
     *
     * @param target    the first object to be compared.
     * @param reference the second object to be compared.
     * @return
     */
    @Override
    public int compare(MatchPlayer target, MatchPlayer reference) {
        boolean targetIsSub = target.getSubstitute();
        boolean referenceIsSub = reference.getSubstitute();

        if (!targetIsSub && referenceIsSub) return -1;
        if (targetIsSub && !referenceIsSub) return 1;

        try {
            if (targetIsSub) {
                // 후보의 경우 포지션 G D M F 순으로 정렬
                int positionCompare = Integer.compare(
                        POSITION_PRIORITY.get(getSafePositionToUpperCase(target)),
                        POSITION_PRIORITY.get(getSafePositionToUpperCase(reference))
                );
                if (positionCompare != 0) {
                    return positionCompare;
                }

                // 포지션 동일 시 등번호(number) 순으로 정렬
                Integer targetNumber = getSafeNumber(target);
                Integer referenceNumber = getSafeNumber(reference);

                if (targetNumber == null && referenceNumber != null) {
                    return 1;
                }
                if (targetNumber != null && referenceNumber == null) {
                    return -1;
                }

                int numberComp = safeNumberCompare(targetNumber, referenceNumber);
                if (numberComp != 0) {
                    return numberComp;
                }

                // 모두 동일 시 player id 순으로 정렬.
                return compareSafePlayerId(target, reference);
            } else {
                // 선발의 경우 포메이션 그리드 순으로 정렬 (ex. 1:1, 1:2, 2:1, 2:2)
                if (target.getGrid() == null || reference.getGrid() == null) {
                    log.error("grid is null. target: {}, reference: {}", target, reference);
                    return 0;
                }

                String[] grid1 = target.getGrid().split(":");
                String[] grid2 = reference.getGrid().split(":");
                int xCompare = Integer.compare(Integer.parseInt(grid1[0]), Integer.parseInt(grid2[0]));
                return xCompare != 0 ? xCompare : Integer.compare(Integer.parseInt(grid1[1]), Integer.parseInt(grid2[1]));
            }
        } catch (Exception e) {
            log.error("error while MatchLineup comparing. target: {}, reference: {}", target, reference);
            return 0;
        }
    }

    private static int compareSafePlayerId(MatchPlayer target, MatchPlayer reference) {
        Player targetPlayer = target.getPlayer();
        Player referencePlayer = reference.getPlayer();

        if (targetPlayer != null && referencePlayer != null) {
            return Long.compare(targetPlayer.getId(), referencePlayer.getId());
        }

        if(targetPlayer == null && referencePlayer == null) {
            UUID targetUuid = target.getTemporaryId();
            UUID referenceUuid = reference.getTemporaryId();
            if(targetUuid != null && referenceUuid != null) {
                return targetUuid.compareTo(referenceUuid);
            }
        }

        if (targetPlayer == null && referencePlayer != null) {
            return 1;
        } else if(targetPlayer != null && referencePlayer == null){ // targetPlayer != null && referencePlayer == null
            return -1;
        }

        return 0;
    }

    /**
     * 선수의 등번호를 반환합니다. <br>
     * 선수가 null 인 경우 unregisteredPlayerNumber 를 사용합니다.
     *
     * @param matchPlayer 경기 선수
     * @return 선수의 등번호
     */
    private static @Nullable Integer getSafeNumber(MatchPlayer matchPlayer) {
        if (matchPlayer.getPlayer() != null) {
            return matchPlayer.getPlayer().getNumber();
        } else {
            return matchPlayer.getUnregisteredPlayerNumber();
        }
    }

    private static int safeNumberCompare(Integer targetNumber, Integer referenceNumber) {
        if(targetNumber != null && referenceNumber != null) {
            return Integer.compare(targetNumber, referenceNumber);
        }
        return 0;
    }

    private static String getSafePositionToUpperCase(MatchPlayer matchPlayer) {
        if (matchPlayer.getPosition() == null) {
            return "F";
        } else {
            return matchPlayer.getPosition().toUpperCase();
        }
    }

}