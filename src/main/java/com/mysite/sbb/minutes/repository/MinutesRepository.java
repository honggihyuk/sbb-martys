package com.mysite.sbb.minutes.repository;

import com.mysite.sbb.minutes.entity.Minutes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinutesRepository extends JpaRepository<Minutes, Long> {

    List<Minutes> findAllByOrderByMeetingDateDescIdDesc();
}
