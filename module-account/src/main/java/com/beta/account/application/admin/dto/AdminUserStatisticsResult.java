package com.beta.account.application.admin.dto;

import com.beta.account.infra.repository.AdminUserStatisticsQueryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AdminUserStatisticsResult(
        long totalUserCount,
        List<GenderStat> genderStats,
        List<AgeStat> ageStats
) {
    public static AdminUserStatisticsResult from(
            AdminUserStatisticsQueryRepository.UserStatisticsSnapshot snapshot
    ) {
        Map<GenderCategory, Long> genderCountMap = snapshot.genderStats().stream()
                .collect(Collectors.toMap(
                        item -> toGenderCategory(item.gender()),
                        AdminUserStatisticsQueryRepository.GenderCountSnapshot::count
                ));

        Map<AgeGroup, Long> ageCountMap = snapshot.ageStats().stream()
                .collect(Collectors.toMap(
                        item -> toAgeGroup(item.ageGroup()),
                        AdminUserStatisticsQueryRepository.AgeGroupCountSnapshot::count
                ));

        return new AdminUserStatisticsResult(
                snapshot.totalUserCount(),
                Arrays.stream(GenderCategory.values())
                        .map(category -> new GenderStat(category, genderCountMap.getOrDefault(category, 0L)))
                        .toList(),
                Arrays.stream(AgeGroup.values())
                        .map(ageGroup -> new AgeStat(ageGroup, ageCountMap.getOrDefault(ageGroup, 0L)))
                        .toList()
        );
    }

    private static GenderCategory toGenderCategory(AdminUserStatisticsQueryRepository.GenderStatType genderStatType) {
        return switch (genderStatType) {
            case FEMALE -> GenderCategory.FEMALE;
            case MALE -> GenderCategory.MALE;
            case UNSPECIFIED -> GenderCategory.UNSPECIFIED;
        };
    }

    private static AgeGroup toAgeGroup(AdminUserStatisticsQueryRepository.AgeGroupStatType ageGroupStatType) {
        return switch (ageGroupStatType) {
            case TEENS -> AgeGroup.TEENS;
            case TWENTIES -> AgeGroup.TWENTIES;
            case THIRTIES -> AgeGroup.THIRTIES;
            case FORTIES -> AgeGroup.FORTIES;
            case FIFTIES -> AgeGroup.FIFTIES;
            case OTHERS -> AgeGroup.OTHERS;
            case UNSPECIFIED -> AgeGroup.UNSPECIFIED;
        };
    }

    public record GenderStat(
            GenderCategory gender,
            long count
    ) {
    }

    public record AgeStat(
            AgeGroup ageGroup,
            long count
    ) {
    }

    public enum GenderCategory {
        FEMALE,
        MALE,
        UNSPECIFIED
    }

    public enum AgeGroup {
        TEENS,
        TWENTIES,
        THIRTIES,
        FORTIES,
        FIFTIES,
        OTHERS,
        UNSPECIFIED
    }
}
