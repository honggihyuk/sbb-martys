package com.mysite.sbb.common.geo;

// 위도·경도 계산
public final class GeoUtils {

    private GeoUtils() {}

    private static final double EARTH_RADIUS_KM = 6371.0;

    // 두 지점 사이의 직선(대권) 거리 km : 하버사인(haversine) 공식
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    // 화면 표시용 (예: 850m, 12.3km, 1,240km)
    public static String format(double km) {
        if (km < 1) return Math.round(km * 1000) + "m";
        if (km < 100) return String.format("%.1fkm", km);
        return String.format("%,dkm", Math.round(km));
    }
}
