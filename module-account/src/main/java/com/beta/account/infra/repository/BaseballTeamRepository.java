package com.beta.account.infra.repository;

import com.beta.account.domain.entity.BaseballTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseballTeamRepository extends JpaRepository<BaseballTeam, String> {

}
