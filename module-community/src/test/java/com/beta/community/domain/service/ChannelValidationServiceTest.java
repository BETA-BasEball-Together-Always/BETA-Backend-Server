package com.beta.community.domain.service;

import com.beta.community.domain.entity.Post.Channel;
import com.beta.core.exception.community.InvalidChannelAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChannelValidationServiceTest {

    private ChannelValidationService channelValidationService;

    @BeforeEach
    void setUp() {
        channelValidationService = new ChannelValidationService();
    }

    @Nested
    @DisplayName("validateAndResolveChannel 성공 케이스")
    class SuccessCase {

        @Test
        @DisplayName("ALL 채널 요청 시 ALL 반환")
        void returnAll_whenRequestChannelIsAll() {
            String result = channelValidationService.validateAndResolveChannel("ALL", "DOOSAN");

            assertThat(result).isEqualTo(Channel.ALL.name());
        }

        @Test
        @DisplayName("TEAM 채널 요청 시 사용자 팀코드 반환")
        void returnTeamCode_whenRequestChannelIsTeam() {
            String result = channelValidationService.validateAndResolveChannel("TEAM", "DOOSAN");

            assertThat(result).isEqualTo("DOOSAN");
        }
    }

    @Nested
    @DisplayName("validateAndResolveChannel 실패 케이스")
    class FailureCase {

        @Test
        @DisplayName("유효하지 않은 채널 구분 시 예외 발생")
        void throwException_whenInvalidChannel() {
            assertThatThrownBy(() ->
                    channelValidationService.validateAndResolveChannel("INVALID", "DOOSAN"))
                    .isInstanceOf(InvalidChannelAccessException.class)
                    .hasMessageContaining("유효하지 않은 채널 구분입니다");
        }

        @Test
        @DisplayName("TEAM 요청 시 팀코드가 null이면 예외 발생")
        void throwException_whenTeamCodeIsNull() {
            assertThatThrownBy(() ->
                    channelValidationService.validateAndResolveChannel("TEAM", null))
                    .isInstanceOf(InvalidChannelAccessException.class)
                    .hasMessageContaining("팀 코드가 존재하지 않습니다");
        }

        @Test
        @DisplayName("TEAM 요청 시 팀코드가 빈 문자열이면 예외 발생")
        void throwException_whenTeamCodeIsBlank() {
            assertThatThrownBy(() ->
                    channelValidationService.validateAndResolveChannel("TEAM", "  "))
                    .isInstanceOf(InvalidChannelAccessException.class)
                    .hasMessageContaining("팀 코드가 존재하지 않습니다");
        }

        @Test
        @DisplayName("TEAM 요청 시 유효하지 않은 팀코드면 예외 발생")
        void throwException_whenInvalidTeamCode() {
            assertThatThrownBy(() ->
                    channelValidationService.validateAndResolveChannel("TEAM", "INVALID_TEAM"))
                    .isInstanceOf(InvalidChannelAccessException.class)
                    .hasMessageContaining("유효하지 않은 팀 코드입니다");
        }

        @Test
        @DisplayName("TEAM 요청 시 팀코드가 ALL이면 예외 발생")
        void throwException_whenTeamCodeIsAll() {
            assertThatThrownBy(() ->
                    channelValidationService.validateAndResolveChannel("TEAM", "ALL"))
                    .isInstanceOf(InvalidChannelAccessException.class)
                    .hasMessageContaining("ALL은 팀 채널로 사용할 수 없습니다");
        }
    }
}
