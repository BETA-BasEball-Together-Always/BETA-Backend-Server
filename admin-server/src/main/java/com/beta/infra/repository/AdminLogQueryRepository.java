package com.beta.infra.repository;

import com.beta.domain.entity.AdminLog;
import com.beta.domain.entity.AdminLogAction;
import com.beta.domain.entity.QAdminLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<AdminLog> findActionLogs(
            int page,
            int size,
            AdminLogAction action,
            LocalDate from,
            LocalDate to
    ) {
        QAdminLog adminLog = QAdminLog.adminLog;
        Pageable pageable = PageRequest.of(page, size);
        BooleanBuilder condition = new BooleanBuilder();

        if (action != null) {
            condition.and(adminLog.action.eq(action));
        }

        if (from != null) {
            condition.and(adminLog.createdAt.goe(from.atStartOfDay()));
        }

        if (to != null) {
            condition.and(adminLog.createdAt.lt(to.plusDays(1).atStartOfDay()));
        }

        List<AdminLog> content = queryFactory
                .selectFrom(adminLog)
                .where(condition)
                .orderBy(adminLog.createdAt.desc(), adminLog.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(adminLog.count())
                .from(adminLog)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount == null ? 0L : totalCount);
    }
}
