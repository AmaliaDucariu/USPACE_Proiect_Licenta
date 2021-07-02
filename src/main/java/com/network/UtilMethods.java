package com.network;

public class UtilMethods {

    /*
     * Function to compute distance on great circle on altitude H
     *
     * H - altitude above ground level
     * H = 0 NM by default
     * R - radius of Earth
     * R = 3443.89 NM
     *
     * */
    /*public static double greatCircleDistance(final markerDeparture, markerArrival, H = 0) {

        var longDep = markerDeparture.getLatLng().lng * DEG_TO_RAD_FACTOR
        var latDep = markerDeparture.getLatLng().lat * DEG_TO_RAD_FACTOR
        var longArr = markerArrival.getLatLng().lng * DEG_TO_RAD_FACTOR
        var latArr = markerArrival.getLatLng().lat * DEG_TO_RAD_FACTOR
        v = Math.abs(longArr - longDep);
        n = Math.acos(Math.sin(latDep) * Math.sin(latArr) + Math.cos(latDep) * Math.cos(latArr) * Math.cos(v));

        return n * (R + H);

    }*/
}
