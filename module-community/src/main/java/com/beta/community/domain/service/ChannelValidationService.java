package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post.Channel;
import com.beta.core.exception.community.InvalidChannelAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelValidationService {

    public String validateAndResolveChannel(String requestChannel, String userTeamCode) {
        if ("ALL".equals(requestChannel)) {
            return Channel.ALL.name();
        }

        if ("TEAM".equals(requestChannel)) {
            validateTeamCode(userTeamCode);
            return userTeamCode;
        }

        throw new InvalidChannelAccessException("유효하지 않은 채널 구분입니다: " + requestChannel);
    }

    private void validateTeamCode(String teamCode) {
        if (teamCode == null || teamCode.isBlank()) {
            throw new InvalidChannelAccessException("팀 코드가 존재하지 않습니다");
        }

        try {
            Channel.valueOf(teamCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidChannelAccessException("유효하지 않은 팀 코드입니다: " + teamCode);
        }

        if (Channel.ALL.name().equals(teamCode)) {
            throw new InvalidChannelAccessException("ALL은 팀 채널로 사용할 수 없습니다");
        }
    }
}
