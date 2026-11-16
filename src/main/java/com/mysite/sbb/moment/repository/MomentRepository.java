package com.mysite.sbb.moment.repository;

import com.mysite.sbb.moment.entity.Moment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MomentRepository extends JpaRepository<Moment, Long> {

    List<Moment> findAllByOrderByMeetDateDescIdDesc();
}
