package com.beta.account.domain.service;

import com.beta.account.domain.entity.BaseballTeam;
import com.beta.account.infra.repository.BaseballTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaseballTeamReadService {

    private final BaseballTeamRepository baseballTeamRepository;

    public List<BaseballTeam> getAllBaseballTeams() {
        return baseballTeamRepository.findAll();
    }
}
