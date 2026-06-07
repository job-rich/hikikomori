package org.hikikomori.community.facade;

import lombok.RequiredArgsConstructor;
import org.hikikomori.community.domain.User;
import org.hikikomori.community.repository.UserRepositoryImpl;
import org.hikikomori.community.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final UserRepositoryImpl userRepository;

    @Transactional
    public void touch(Long userId, String nickName) {
        User user = userService.upsert(userRepository.findByUserId(userId), userId, nickName);
        userRepository.save(user);
    }

    @Transactional
    public void markBanned(Long userId) {
        User user = userService.markBanned(userRepository.findByUserId(userId), userId);
        userRepository.save(user);
    }
}
