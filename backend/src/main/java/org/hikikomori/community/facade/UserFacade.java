package org.hikikomori.community.facade;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hikikomori.community.config.ScoreWeights;
import org.hikikomori.community.domain.User;
import org.hikikomori.community.dto.UserDto;
import org.hikikomori.community.repository.ReportRepositoryImpl;
import org.hikikomori.community.repository.UserJpaRepository;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.hikikomori.community.repository.VoteRepositoryImpl;
import org.hikikomori.community.service.ScoreService;
import org.hikikomori.community.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final ScoreService scoreService;
    private final UserRepositoryImpl userRepository;
    private final VoteRepositoryImpl voteRepository;
    private final ReportRepositoryImpl reportRepository;
    private final ScoreWeights weights;

    @Transactional
    public void touch(Long userId, String nickName) {
        Optional<User> existing = userRepository.findByUserId(userId);
        if (existing.isPresent() && nickName.equals(existing.get().getNickName())) {
            return; // 닉네임 변경 없음 → 불필요한 UPDATE 스킵
        }
        userRepository.save(userService.upsert(existing, userId, nickName));
    }

    @Transactional
    public void markBanned(Long userId) {
        User user = userService.markBanned(userRepository.findByUserId(userId), userId);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "profile", key = "#userId")
    public UserDto.ProfileResponse getProfile(Long userId) {
        User user = userRepository.getByUserId(userId);
        long voteNet = voteRepository.netByTargetUser(userId);
        long reports = reportRepository.countByTargetUser(userId);
        long power = scoreService.compute(voteNet, reports, weights);
        long rank = userRepository.countHigherPower(weights.vote(), weights.report(), power) + 1;
        return new UserDto.ProfileResponse(
                userId, user.getNickName(), power, voteNet, reports, rank, user.isBanned());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "ranking", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto.RankingResponse> getRanking(Pageable pageable) {
        Page<UserJpaRepository.RankingRow> page =
                userRepository.findRanking(weights.vote(), weights.report(), pageable);
        return page.map(r -> new UserDto.RankingResponse(
                r.getUserId(), r.getNickName(), r.getPower(), r.getBanned()));
    }
}
