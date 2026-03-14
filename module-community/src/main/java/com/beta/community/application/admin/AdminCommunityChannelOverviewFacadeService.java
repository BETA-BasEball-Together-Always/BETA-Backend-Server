package com.beta.community.application.admin;

import com.beta.community.application.admin.dto.AdminCommunityChannelOverviewMetricsResult;
import com.beta.community.infra.repository.ChannelOverviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminCommunityChannelOverviewFacadeService {

    private final ChannelOverviewQueryRepository channelOverviewQueryRepository;

    @Transactional(readOnly = true)
    public AdminCommunityChannelOverviewMetricsResult getChannelOverviewMetrics(
            List<String> channelCodes,
            int days
    ) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);

        List<LocalDate> dates = IntStream.range(0, days)
                .mapToObj(startDate::plusDays)
                .toList();

        List<ChannelOverviewQueryRepository.ChannelDailyCountSnapshot> postSnapshots =
                channelOverviewQueryRepository.findDailyPostCounts(today, days);
        List<ChannelOverviewQueryRepository.ChannelDailyCountSnapshot> commentSnapshots =
                channelOverviewQueryRepository.findDailyCommentCounts(today, days);

        return AdminCommunityChannelOverviewMetricsResult.from(
                channelCodes,
                dates,
                postSnapshots,
                commentSnapshots
        );
    }
}
