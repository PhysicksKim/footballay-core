package com.gyechunsik.scoreboard.domain.football.comparator;

import com.gyechunsik.scoreboard.domain.football.persistence.live.StartPlayer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StartLineupComparator implements Comparator<StartPlayer> {
    private static final Map<String, Integer> POSITION_PRIORITY;

    static {
        POSITION_PRIORITY = new HashMap<>();
        POSITION_PRIORITY.put("G", 1);
        POSITION_PRIORITY.put("D", 2);
        POSITION_PRIORITY.put("M", 3);
        POSITION_PRIORITY.put("F", 4);
    }

    @Override
    public int compare(StartPlayer target, StartPlayer reference) {
        boolean targetIsSub = target.getSubstitute();
        boolean referenceIsSub = reference.getSubstitute();

        if (!targetIsSub && referenceIsSub) return -1;
        if (targetIsSub && !referenceIsSub) return 1;

        try {

            if (targetIsSub) {
                // 후보의 경우 포지션 G D M F 순으로 정렬
                int positionCompare = Integer.compare(
                        POSITION_PRIORITY.get(target.getPosition().toUpperCase()),
                        POSITION_PRIORITY.get(reference.getPosition().toUpperCase())
                );
                if (positionCompare != 0) {
                    return positionCompare;
                }
                // 포지션 동일 시 등번호(number) 순으로 정렬
                int numberComp = Integer.compare(target.getPlayer().getNumber(), reference.getPlayer().getNumber());
                if (numberComp != 0) {
                    return numberComp;
                }
                // 모두 동일 시 player id 순으로 정렬.
                return Long.compare(target.getId(), reference.getId());
            } else {
                // 선발의 경우 포메이션 그리드 순으로 정렬 (ex. 1:1, 1:2, 2:1, 2:2)
                if(target.getGrid() == null || reference.getGrid() == null) {
                    log.error("grid is null. target: {}, reference: {}", target, reference);
                    return 0;
                }
                String[] grid1 = target.getGrid().split(":");
                String[] grid2 = reference.getGrid().split(":");
                int xCompare = Integer.compare(Integer.parseInt(grid1[0]), Integer.parseInt(grid2[0]));
                return xCompare != 0 ? xCompare : Integer.compare(Integer.parseInt(grid1[1]), Integer.parseInt(grid2[1]));
            }
        } catch (Exception e) {
            log.error("error while StartLineup comparing. target: {}, reference: {}", target, reference);
            return 0;
        }
    }

}