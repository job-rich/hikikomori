package org.hikikomori.community.dto;

public class UserDto {

    public record ProfileResponse(
            Long userId, String nickName, long power, long voteNet, long reports, long rank, boolean banned
    ) {}

    public record RankingResponse(Long userId, String nickName, long power, boolean banned) {}
}
