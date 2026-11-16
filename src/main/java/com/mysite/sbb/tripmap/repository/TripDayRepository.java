package com.mysite.sbb.tripmap.repository;

import com.mysite.sbb.tripmap.entity.TripDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    List<TripDay> findAllByOrderByDayNoAsc();

    Optional<TripDay> findByDayNo(Integer dayNo);
}
