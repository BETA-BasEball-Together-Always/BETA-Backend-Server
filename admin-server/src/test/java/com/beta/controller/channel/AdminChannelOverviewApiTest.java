package com.beta.controller.channel;

import com.beta.account.infra.client.apple.AppleLoginClient;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.response.ErrorResponse;
import com.beta.core.security.AdminAuthConstants;
import com.beta.core.security.JwtTokenProvider;
import com.beta.docker.MysqlRedisTestContainer;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.data.redis.username=",
                "spring.data.redis.password=",
                "management.health.mail.enabled=false"
        }
)
@Sql(scripts = {"/sql/admin-channel-overview-cleanup.sql", "/sql/admin-channel-overview-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-channel-overview-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminChannelOverviewApiTest extends MysqlRedisTestContainer {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AppleLoginClient appleLoginClient;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private AbstractAuthenticationDetailsProvider authProvider;

    @MockitoBean
    private ObjectStorage objectStorage;

    @MockitoBean
    private OracleCloudStorageClient oracleCloudStorageClient;

    @Test
    void 관리자_팀별_현황_조회시_200_응답과_팀별_집계_데이터를_반환한다() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(
                1L,
                "LG",
                "ADMIN",
                AdminAuthConstants.ADMIN_CLIENT
        );
        HttpHeaders headers = bearerHeaders(accessToken);

        // when
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/admin/channels/overview",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(body).doesNotContainKeys("totalTodayPostCount", "totalTodayCommentCount");

        Map<String, Object> todayPeakTeam = toMap(body.get("todayPeakTeam"));
        assertThat(todayPeakTeam.get("teamCode")).isEqualTo("KIA");
        assertThat(todayPeakTeam.get("teamName")).isEqualTo("KIA 타이거즈");
        assertThat(toLong(todayPeakTeam.get("activityCount"))).isEqualTo(6L);
        assertThat(toLong(todayPeakTeam.get("postCount"))).isEqualTo(1L);
        assertThat(toLong(todayPeakTeam.get("commentCount"))).isEqualTo(5L);

        Map<String, Object> weeklyPeakTeam = toMap(body.get("weeklyPeakTeam"));
        assertThat(weeklyPeakTeam.get("teamCode")).isEqualTo("LG");
        assertThat(weeklyPeakTeam.get("teamName")).isEqualTo("LG 트윈스");
        assertThat(toLong(weeklyPeakTeam.get("activityCount"))).isEqualTo(16L);
        assertThat(toLong(weeklyPeakTeam.get("postCount"))).isEqualTo(7L);
        assertThat(toLong(weeklyPeakTeam.get("commentCount"))).isEqualTo(9L);

        List<Map<String, Object>> teams = toListOfMap(body.get("teams"));
        assertThat(teams).hasSize(10);

        Map<String, Object> lg = findTeamByCode(teams, "LG");
        assertThat(lg.get("teamName")).isEqualTo("LG 트윈스");
        assertThat(toLong(lg.get("userCount"))).isEqualTo(2L);
        assertThat(toLong(lg.get("todayPostCount"))).isEqualTo(2L);
        assertThat(toLong(lg.get("todayCommentCount"))).isEqualTo(3L);
        assertThat(toLong(lg.get("todayActivityCount"))).isEqualTo(5L);
        assertThat(toLong(lg.get("weeklyPostCount"))).isEqualTo(7L);
        assertThat(toLong(lg.get("weeklyCommentCount"))).isEqualTo(9L);
        assertThat(toLong(lg.get("weeklyActivityCount"))).isEqualTo(16L);
        assertThat(lg).doesNotContainKeys(
                "todayPostShareRate",
                "todayCommentShareRate",
                "todayCommentToPostRatio",
                "weeklyCommentToPostRatio"
        );

        List<Map<String, Object>> lgDailyActivities = toListOfMap(lg.get("dailyActivities"));
        assertThat(lgDailyActivities).hasSize(7);
        assertThat(toLong(lgDailyActivities.get(0).get("postCount"))).isEqualTo(1L);
        assertThat(toLong(lgDailyActivities.get(0).get("commentCount"))).isEqualTo(1L);
        assertThat(toLong(lgDailyActivities.get(6).get("postCount"))).isEqualTo(2L);
        assertThat(toLong(lgDailyActivities.get(6).get("commentCount"))).isEqualTo(3L);
        assertThat(toLong(lgDailyActivities.get(6).get("totalActivityCount"))).isEqualTo(5L);

        Map<String, Object> kiwoom = findTeamByCode(teams, "KIWOOM");
        assertThat(kiwoom.get("teamName")).isEqualTo("키움 히어로즈");
        assertThat(toLong(kiwoom.get("userCount"))).isZero();
        assertThat(toLong(kiwoom.get("todayPostCount"))).isZero();
        assertThat(toLong(kiwoom.get("todayCommentCount"))).isZero();
        assertThat(toLong(kiwoom.get("weeklyActivityCount"))).isZero();

        Map<String, Object> kia = findTeamByCode(teams, "KIA");
        assertThat(toLong(kia.get("userCount"))).isEqualTo(1L);

        Map<String, Object> doosan = findTeamByCode(teams, "DOOSAN");
        assertThat(toLong(doosan.get("userCount"))).isEqualTo(1L);

        Map<String, Object> hanwha = findTeamByCode(teams, "HANWHA");
        assertThat(toLong(hanwha.get("userCount"))).isEqualTo(1L);
    }

    @Test
    void CLIENT는_ADMIN이고_ROLE이_USER인_토큰으로_팀별_현황을_호출하면_403_ADMIN001_예외를_반환한다() {
        // given
        String userRoleToken = jwtTokenProvider.generateAccessToken(
                2L,
                "LG",
                "USER",
                AdminAuthConstants.ADMIN_CLIENT
        );
        HttpHeaders headers = bearerHeaders(userRoleToken);

        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/api/v1/admin/channels/overview",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADMIN001");
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toListOfMap(Object value) {
        return ((List<Object>) value).stream()
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> findTeamByCode(List<Map<String, Object>> teams, String teamCode) {
        return teams.stream()
                .filter(team -> teamCode.equals(team.get("teamCode")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("team not found teamCode=" + teamCode));
    }

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }

}
