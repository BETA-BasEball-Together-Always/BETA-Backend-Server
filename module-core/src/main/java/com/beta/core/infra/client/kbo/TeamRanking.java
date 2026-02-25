package com.beta.core.infra.client.kbo;

public record TeamRanking(
        int rank,
        String teamName,
        int games,
        int wins,
        int losses,
        int draws,
        String winRate,
        String gamesBehind,
        String recentTen,
        String streak
) {
}
