package org.hikikomori.community.domain;

import java.util.List;
import java.util.Random;

public class DeleteTaunt {

    private static final List<String> MESSAGES = List.of(
            "쫄아서 도망간 댓글입니다.",
            "팩폭 맞고 자진 삭제된 댓글입니다.",
            "현피 무서워서 튄 댓글입니다.",
            "논리 앞에 무너진 댓글입니다.",
            "자존심 상해 지운 댓글입니다.",
            "반박 못 하고 도망간 댓글입니다.",
            "멘탈이 산산조각난 댓글입니다.",
            "키보드 워리어의 흑역사가 된 댓글입니다."
    );
    private static final Random RANDOM = new Random();

    public static String pick() {
        return MESSAGES.get(RANDOM.nextInt(MESSAGES.size()));
    }

    private DeleteTaunt() {
    }
}
