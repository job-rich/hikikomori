package org.hikikomori.community.service;

import java.util.ArrayList;
import java.util.List;
import org.hikikomori.community.domain.Vote;
import org.hikikomori.community.domain.VoteValue;
import org.hikikomori.community.dto.VoteDto;
import org.hikikomori.community.exception.SelfReportException;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    /** append-only 로그에 삽입할 한 행: 어떤 value를 +1(행사)/-1(취소) */
    public record VoteAction(VoteValue value, int delta) {}

    public void checkTargetAuthor(Long actualAuthor, Long claimedUserId) {
        if (!actualAuthor.equals(claimedUserId)) {
            throw new IllegalArgumentException("추천 대상 작성자가 일치하지 않습니다");
        }
    }

    public void checkNotSelfVote(Long voterId, Long targetUserId) {
        if (voterId.equals(targetUserId)) {
            throw new SelfReportException("본인의 콘텐츠는 추천할 수 없습니다");
        }
    }

    /** 현재 보유 상태 + 요청 value → 삽입할 델타 행 목록(1~2개) */
    public List<VoteAction> resolveActions(boolean currentUp, boolean currentDown, VoteValue requested) {
        List<VoteAction> actions = new ArrayList<>();
        if (requested == VoteValue.UP) {
            if (currentUp) {
                actions.add(new VoteAction(VoteValue.UP, -1));   // 추천 취소
            } else {
                actions.add(new VoteAction(VoteValue.UP, +1));   // 추천
                if (currentDown) {
                    actions.add(new VoteAction(VoteValue.DOWN, -1)); // 비추천 거두기(스위치)
                }
            }
        } else {
            if (currentDown) {
                actions.add(new VoteAction(VoteValue.DOWN, -1));
            } else {
                actions.add(new VoteAction(VoteValue.DOWN, +1));
                if (currentUp) {
                    actions.add(new VoteAction(VoteValue.UP, -1));
                }
            }
        }
        return actions;
    }

    /** 토글 후 내 표 상태 */
    public VoteValue resultVote(boolean currentUp, boolean currentDown, VoteValue requested) {
        if (requested == VoteValue.UP) {
            return currentUp ? null : VoteValue.UP;
        }
        return currentDown ? null : VoteValue.DOWN;
    }

    public Vote buildVote(Long targetUserId, VoteDto.CreateRequest request, VoteAction action) {
        return Vote.builder()
                .voterId(request.voterId())
                .targetUserId(targetUserId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .value(action.value())
                .delta(action.delta())
                .build();
    }
}
