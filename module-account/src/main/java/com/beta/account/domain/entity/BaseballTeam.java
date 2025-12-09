package com.beta.account.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "baseball_teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseballTeam {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "team_name_en", nullable = false, length = 50)
    private String teamNameEn;

    @Column(name = "team_name_kr", length = 50)
    private String teamNameKr;

    @Column(name = "home_stadium", nullable = false, length = 100)
    private String homeStadium;

    @Column(name = "stadium_address", nullable = false, length = 200)
    private String stadiumAddress;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude; // 경도

    @Column(name = "founded_year")
    private Integer foundedYear; // 창단 연도

    @Builder
    public BaseballTeam(String code, String teamNameEn, String teamNameKr,
                        String homeStadium, String stadiumAddress,
                        BigDecimal latitude, BigDecimal longitude, Integer foundedYear) {
        this.code = code;
        this.teamNameEn = teamNameEn;
        this.teamNameKr = teamNameKr;
        this.homeStadium = homeStadium;
        this.stadiumAddress = stadiumAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foundedYear = foundedYear;
    }
}
