package com.mysite.sbb.group.repository;

import com.mysite.sbb.group.entity.TeamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInfoRepository extends JpaRepository<TeamInfo, Integer> {
}
