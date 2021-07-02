package com.constants;

public class ConversionFactors {

    private final double R =3443.89;
    /*
     * Conversion from Degrees to Radians
     * */
    private final double DEG_TO_RAD_FACTOR = 3.14 / 180;
    /*
     * Conversion from Angles to Distance on Earth
     *
     * Distance expressed in NM
     *
     * Angle expressed in degrees
     * */
    private final double ANGLE_TO_NM_FACTOR = 60;
    /*
     * Conversion from feet to NM
     * */
    private final double FEET_TO_NM_FACTOR =0.00016457883;
    /*
     * Conversion from feet to degrees
     * */
    private final double FEET_TO_DEG_FACTOR = FEET_TO_NM_FACTOR / 60;

    public double getANGLE_TO_NM_FACTOR() {
        return ANGLE_TO_NM_FACTOR;
    }

    public double getDEG_TO_RAD_FACTOR() {
        return DEG_TO_RAD_FACTOR;
    }

    public double getFEET_TO_DEG_FACTOR() {
        return FEET_TO_DEG_FACTOR;
    }

    public double getFEET_TO_NM_FACTOR() {
        return FEET_TO_NM_FACTOR;
    }

    public double getR() {
        return R;
    }
}