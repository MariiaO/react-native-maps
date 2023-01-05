package com.airbnb.android.react.maps;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

/**
 * Created by alex on 6/13/17.
 */

public class TileUtils {

    public static final int TILE_SIZE = 256;
    public static final float FACTOR = 10000f;
    public static final int SCALE = 2;
    public static final int DIMENSION = SCALE * TILE_SIZE;

    public static Point toPoint(LatLng latLng) {
        double x = latLng.longitude / 360.0D + 0.5D;
        double siny = Math.sin(Math.toRadians(latLng.latitude));
        double y = 0.5D * Math.log((1.0D + siny) / (1.0D - siny)) / -6.283185307179586D + 0.5D;
        return new Point(x * TILE_SIZE * FACTOR, y * TILE_SIZE * FACTOR);
    }

    public static LatLng toLatLng(Point point) {
        double x = point.x / TILE_SIZE / FACTOR - 0.5D;
        double lng = x * 360.0D;
        double y = 0.5D - point.y / TILE_SIZE / FACTOR;
        double lat = 90.0D - Math.toDegrees(Math.atan(Math.exp(-y * 2.0D * 3.141592653589793D)) * 2.0D);
        return new LatLng(lat , lng);
    }
}
