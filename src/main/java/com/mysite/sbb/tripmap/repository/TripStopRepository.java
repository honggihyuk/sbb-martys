package com.mysite.sbb.tripmap.repository;

import com.mysite.sbb.tripmap.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {
}
