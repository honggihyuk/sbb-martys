package com.mysite.sbb.tripmap.dto;

import com.mysite.sbb.tripmap.entity.TripDay;
import com.mysite.sbb.tripmap.entity.TripStop;

import java.time.LocalDate;
import java.util.List;

// GET /map/data 응답 (JSON) : static/js/trip-map.js 가 지도에 그린다
public record TripMapData(List<Day> days) {

    public record Day(Long id, int dayNo, LocalDate date, String title, List<Stop> stops) {
    }

    public record Stop(Long id, int order, String name, double lat, double lng, String time,
                       String address, String memo, boolean visited, String googleUrl) {
    }

    public static TripMapData of(List<TripDay> days) {
        return new TripMapData(days.stream().map(d -> new Day(d.getId(), d.getDayNo(), d.getDate(), d.getTitle(),
                d.getStops().stream().map(TripMapData::stop).toList())).toList());
    }

    private static Stop stop(TripStop s) {
        return new Stop(s.getId(), s.getSortOrder() + 1, s.getName(), s.getLat(), s.getLng(), s.getTime(),
                s.getAddress(), s.getMemo(), Boolean.TRUE.equals(s.getVisited()), s.getGoogleUrl());
    }
}
