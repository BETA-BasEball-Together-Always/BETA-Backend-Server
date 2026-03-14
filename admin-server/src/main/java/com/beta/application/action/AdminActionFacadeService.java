package com.beta.application.action;

import com.beta.account.application.admin.AdminUserActionAppService;
import com.beta.community.application.admin.AdminCommentActionAppService;
import com.beta.community.application.admin.AdminPostActionAppService;
import com.beta.domain.entity.AdminLog;
import com.beta.domain.service.AdminLogWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminActionFacadeService {

    private final AdminUserActionAppService adminUserActionAppService;
    private final AdminPostActionAppService adminPostActionAppService;
    private final AdminCommentActionAppService adminCommentActionAppService;
    private final AdminLogWriteService adminLogWriteService;

    @Transactional
    public void suspendMember(Long actorAdminId, Long targetUserId, String reason) {
        adminUserActionAppService.suspendUser(targetUserId);
        AdminLog adminLog = AdminLog.suspendMember(actorAdminId, targetUserId, reason);
        adminLogWriteService.save(adminLog);
    }

    @Transactional
    public void unsuspendMember(Long actorAdminId, Long targetUserId, String reason) {
        adminUserActionAppService.unsuspendUser(targetUserId);
        AdminLog adminLog = AdminLog.unsuspendMember(actorAdminId, targetUserId, reason);
        adminLogWriteService.save(adminLog);
    }

    @Transactional
    public void hidePost(Long actorAdminId, Long targetPostId, String reason) {
        adminPostActionAppService.hidePost(targetPostId);
        AdminLog adminLog = AdminLog.hidePost(actorAdminId, targetPostId, reason);
        adminLogWriteService.save(adminLog);
    }

    @Transactional
    public void unhidePost(Long actorAdminId, Long targetPostId, String reason) {
        adminPostActionAppService.unhidePost(targetPostId);
        AdminLog adminLog = AdminLog.unhidePost(actorAdminId, targetPostId, reason);
        adminLogWriteService.save(adminLog);
    }

    @Transactional
    public void hideComment(Long actorAdminId, Long targetCommentId, String reason) {
        adminCommentActionAppService.hideComment(targetCommentId);
        AdminLog adminLog = AdminLog.hideComment(actorAdminId, targetCommentId, reason);
        adminLogWriteService.save(adminLog);
    }

    @Transactional
    public void unhideComment(Long actorAdminId, Long targetCommentId, String reason) {
        adminCommentActionAppService.unhideComment(targetCommentId);
        AdminLog adminLog = AdminLog.unhideComment(actorAdminId, targetCommentId, reason);
        adminLogWriteService.save(adminLog);
    }
}
