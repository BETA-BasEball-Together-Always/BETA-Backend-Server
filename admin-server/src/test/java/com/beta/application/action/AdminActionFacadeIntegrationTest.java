package com.beta.application.action;

import com.beta.account.domain.entity.User;
import com.beta.account.infra.client.apple.AppleLoginClient;
import com.beta.account.infra.repository.UserJpaRepository;
import com.beta.community.domain.entity.Comment;
import com.beta.community.domain.entity.Post;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.CommentJpaRepository;
import com.beta.community.infra.repository.PostJpaRepository;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.exception.admin.InvalidAdminActionException;
import com.beta.domain.entity.AdminLog;
import com.beta.domain.entity.AdminLogAction;
import com.beta.domain.entity.AdminLogTargetType;
import com.beta.docker.MysqlRedisTestContainer;
import com.beta.infra.repository.AdminLogJpaRepository;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        properties = {
                "spring.data.redis.username=",
                "spring.data.redis.password=",
                "management.health.mail.enabled=false"
        }
)
@Sql(scripts = {"/sql/admin-action-cleanup.sql", "/sql/admin-action-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/admin-action-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminActionFacadeIntegrationTest extends MysqlRedisTestContainer {

    @Autowired
    private AdminActionFacadeService adminActionFacadeService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private CommentJpaRepository commentJpaRepository;

    @Autowired
    private AdminLogJpaRepository adminLogJpaRepository;

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
    void 회원_정지시_상태가_변경되고_관리자_로그가_저장된다() {
        // given
        Long actorAdminId = 1L;
        Long targetUserId = 2L;
        String reason = "운영 정책 위반";

        // when
        adminActionFacadeService.suspendMember(actorAdminId, targetUserId, reason);

        // then
        User user = userJpaRepository.findById(targetUserId).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.SUSPENDED);

        List<AdminLog> adminLogs = adminLogJpaRepository.findAll();
        assertThat(adminLogs).hasSize(1);

        AdminLog adminLog = adminLogs.get(0);
        assertThat(adminLog.getActorAdminId()).isEqualTo(actorAdminId);
        assertThat(adminLog.getTargetType()).isEqualTo(AdminLogTargetType.MEMBER);
        assertThat(adminLog.getTargetId()).isEqualTo(targetUserId);
        assertThat(adminLog.getAction()).isEqualTo(AdminLogAction.MEMBER_SUSPEND);
        assertThat(adminLog.getBeforeStatus()).isEqualTo("ACTIVE");
        assertThat(adminLog.getAfterStatus()).isEqualTo("SUSPENDED");
        assertThat(adminLog.getReason()).isEqualTo(reason);
    }

    @Test
    void 게시글_다시노출시_상태가_변경되고_관리자_로그가_저장된다() {
        // given
        Long actorAdminId = 1L;
        Long targetPostId = 102L;
        String reason = "숨김 해제";

        // when
        adminActionFacadeService.unhidePost(actorAdminId, targetPostId, reason);

        // then
        Post post = postJpaRepository.findById(targetPostId).orElseThrow();
        assertThat(post.getStatus()).isEqualTo(Status.ACTIVE);

        List<AdminLog> adminLogs = adminLogJpaRepository.findAll();
        assertThat(adminLogs).hasSize(1);

        AdminLog adminLog = adminLogs.get(0);
        assertThat(adminLog.getActorAdminId()).isEqualTo(actorAdminId);
        assertThat(adminLog.getTargetType()).isEqualTo(AdminLogTargetType.POST);
        assertThat(adminLog.getTargetId()).isEqualTo(targetPostId);
        assertThat(adminLog.getAction()).isEqualTo(AdminLogAction.POST_UNHIDE);
        assertThat(adminLog.getBeforeStatus()).isEqualTo("HIDDEN");
        assertThat(adminLog.getAfterStatus()).isEqualTo("ACTIVE");
        assertThat(adminLog.getReason()).isEqualTo(reason);
    }

    @Test
    void 댓글_숨김시_상태가_변경되고_관리자_로그가_저장된다() {
        // given
        Long actorAdminId = 1L;
        Long targetCommentId = 201L;
        String reason = "운영자 숨김";

        // when
        adminActionFacadeService.hideComment(actorAdminId, targetCommentId, reason);

        // then
        Comment comment = commentJpaRepository.findById(targetCommentId).orElseThrow();
        assertThat(comment.getStatus()).isEqualTo(Status.HIDDEN);

        List<AdminLog> adminLogs = adminLogJpaRepository.findAll();
        assertThat(adminLogs).hasSize(1);

        AdminLog adminLog = adminLogs.get(0);
        assertThat(adminLog.getActorAdminId()).isEqualTo(actorAdminId);
        assertThat(adminLog.getTargetType()).isEqualTo(AdminLogTargetType.COMMENT);
        assertThat(adminLog.getTargetId()).isEqualTo(targetCommentId);
        assertThat(adminLog.getAction()).isEqualTo(AdminLogAction.COMMENT_HIDE);
        assertThat(adminLog.getBeforeStatus()).isEqualTo("ACTIVE");
        assertThat(adminLog.getAfterStatus()).isEqualTo("HIDDEN");
        assertThat(adminLog.getReason()).isEqualTo(reason);
    }

    @Test
    void 삭제된_게시글_숨김시_예외가_발생하고_관리자_로그는_저장되지_않는다() {
        // given
        Long actorAdminId = 1L;
        Long targetPostId = 103L;

        // when // then
        assertThatThrownBy(() -> adminActionFacadeService.hidePost(actorAdminId, targetPostId, "삭제 게시글 숨김 시도"))
                .isInstanceOf(InvalidAdminActionException.class)
                .hasMessage("삭제된 게시글은 숨김 처리할 수 없습니다.");

        Post post = postJpaRepository.findById(targetPostId).orElseThrow();
        assertThat(post.getStatus()).isEqualTo(Status.DELETED);
        assertThat(adminLogJpaRepository.findAll()).isEmpty();
    }
}
