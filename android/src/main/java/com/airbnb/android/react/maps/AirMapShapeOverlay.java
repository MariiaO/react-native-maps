package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;
import android.os.AsyncTask;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class AirMapShapeOverlay extends AirMapFeature {

    private static final String TAG = "AirMapShapeOverlay";
    private static final float LIMIT_RESOLUTION = 4096;

    private static float ANCHOR_X = 0.5f;
    private static float ANCHOR_Y = 0.5f;

    private GroundOverlayOptions overlayOptions;
    private GroundOverlay overlay;

    private LatLngBounds overlayBounds;
    private float transparency = 0.0f;
    private float rotation = 0.0f;
    private float zIndex = 0;
    private String name;

    private BitmapDescriptor imageBitmapDescriptor;
    private Bitmap imageBitmap;

    private GoogleMap map;
    private float scale = 0;
    private AsyncTask<Void, Void, Bitmap> drawOverlayBitmap;
    private ReadableArray rawShapes;

    public AirMapShapeOverlay(Context context) {
        super(context);
    }

    public void setOverlayName(String name) {
        this.name = name;
    }

    public String getOverlayName() {
        return this.name;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        if (overlay != null) {
            overlay.setBearing(rotation);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (overlay != null) {
            overlay.setZIndex(zIndex);
        }
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
        if (overlay != null) {
            overlay.setTransparency(transparency);
        }
    }

    public GroundOverlayOptions getOverlayOptions() {
        if (overlayOptions == null) {
            overlayOptions = createOverlayOptions();
        }
        return overlayOptions;
    }

    @Override
    public Object getFeature() {
        return overlay;
    }

    @Override
    public void addToMap(GoogleMap map) {
        if (map != null) {
            this.map = map;
            overlay = map.addGroundOverlay(getOverlayOptions());
        }
    }

    @Override
    public void removeFromMap(GoogleMap map) {
        this.map = null;
        if(overlay != null) {
            overlay.remove();
            overlay = null;
        }
        if (imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
            imageBitmapDescriptor = null;
        }

        this.overlayOptions = null;
        this.overlayBounds = null;
        if (drawOverlayBitmap != null) {
            drawOverlayBitmap.cancel(false);
            drawOverlayBitmap = null;
        }
        rawShapes = null;
    }

    private void updateImage(Bitmap bitmap) {
        if(overlay != null) {
            overlay.remove();
            overlay = null;
        }
        if (imageBitmap != null) {
            imageBitmap.recycle();
            imageBitmap = null;
            imageBitmapDescriptor = null;
        }
        this.overlayOptions = null;
        imageBitmap = bitmap;
        imageBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(imageBitmap);
        if (map != null) {
            overlay = map.addGroundOverlay(getOverlayOptions());
        }
    }


    private GroundOverlayOptions createOverlayOptions() {
        if (imageBitmapDescriptor == null) {
            imageBitmap = createDrawable();
            imageBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(imageBitmap);
        }
        if (overlayBounds == null) {
            overlayBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(0, 0));
        }

        return new GroundOverlayOptions()
                .positionFromBounds(overlayBounds)
                .anchor(ANCHOR_X, ANCHOR_Y)
                .bearing(rotation)
                .zIndex(zIndex)
                .transparency(transparency)
                .clickable(true)
                .image(imageBitmapDescriptor);
    }

    private void updateBitmap() {
        if (this.drawOverlayBitmap != null) {
            this.drawOverlayBitmap.cancel(false);
        }
        this.drawOverlayBitmap = new DrawOverlayBitmap(rawShapes, scale).execute();
    }

    private Bitmap createDrawable() {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    public void setScale(float scale) {
        if (Math.abs(this.scale - scale) > 0.0001)  {
            this.scale = scale;
        }
        if (this.rawShapes != null) {
            updateBitmap();
        }
    }

    public void setShapes(ReadableArray shapes) {
        this.rawShapes = shapes;
        if (Math.abs(this.scale) > 0.0001) {
            updateBitmap();
        }
    }

    private LatLngBounds addMarginForLatLngBoundsBuilder(LatLngBounds overlayBounds, int width, float scale) {
        double margin = width * scale * 1.6;
        LatLng southwest = SphericalUtil.computeOffset(overlayBounds.southwest, margin, 225);
        LatLng northeast = SphericalUtil.computeOffset(overlayBounds.northeast, margin, 45);
        return new LatLngBounds(southwest, northeast);
    }

    private class DrawOverlayBitmap extends AsyncTask<Void, Void, Bitmap> {

        private final Path path;

        private volatile float scale;
        private Shape[] shapes;
        private LatLngBounds overlayBounds;
        private ReadableArray rawShapes;

        public DrawOverlayBitmap(ReadableArray rawShapes, float scale) {
            this.rawShapes = rawShapes;
            this.scale = scale;
            this.path = new Path();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null && ((ThemedReactContext)getContext()).hasCurrentActivity()) {
                // there's issue related to the size of an image. That's because of bitmap with size 1,1
                // i've tried different ways to resolve it but seems like it's only one which is working
                AirMapShapeOverlay.this.overlayBounds = this.overlayBounds;
                AirMapShapeOverlay.this.overlayOptions = null;
                updateImage(bitmap);
            }
        }

        private void parseShape(ReadableArray rawShapes) {
            try {
                LatLngBounds.Builder overlayBoundsBuilder = new LatLngBounds.Builder();
                Shape[] shapesList = new Shape[rawShapes.size()];
                for(int i = 0; i < rawShapes.size(); i++) {
                    Shape shape = new Shape();
                    ReadableMap shapeRaw = rawShapes.getMap(i);
                    if (shapeRaw.hasKey("strokeColor")) {
                        shape.lineColor = Color.parseColor(shapeRaw.getString("strokeColor"));
                    }
                    if (shapeRaw.hasKey("strokeWidth")) {
                        shape.width = shapeRaw.getInt("strokeWidth");
                    }
                    if (shapeRaw.hasKey("closePath")) {
                        shape.closePath = shapeRaw.getBoolean("closePath");
                    }
                    if (shapeRaw.hasKey("fillColor")) {
                        shape.fillColor = Color.parseColor(shapeRaw.getString("fillColor"));
                        shape.closePath = true;
                    }

                    int maxMarginWidth = 0;
                    LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
                    if (shapeRaw.hasKey("circle")) {
                        ReadableMap circleRaw = shapeRaw.getMap("circle");
                        double lng = circleRaw.getDouble("longitude");
                        double lat = circleRaw.getDouble("latitude");
                        shape.circleRadius = circleRaw.getInt("radius");
                        shape.circleCenter = new LatLng(lat, lng);
                        latLngBoundsBuilder.include(shape.circleCenter);
                        shapesList[i] = shape;
                        shape.pathColors = new int[0];
                        latLngBoundsBuilder.include(shape.circleCenter);
                        LatLngBounds bounds = addMarginForLatLngBoundsBuilder(latLngBoundsBuilder.build(), shape.circleRadius, this.scale);
                        latLngBoundsBuilder.include(bounds.northeast);
                        latLngBoundsBuilder.include(bounds.southwest);
                    } else if (shapeRaw.hasKey("path")) {
                        ReadableArray pathRaw = shapeRaw.getArray("path");
                        shape.pathColors = new int[pathRaw.size()];
                        shape.path = new LatLng[pathRaw.size()];
                        boolean hasColoredLine = false;
                        for(int j = 0; j < pathRaw.size(); j++) {
                            ReadableMap point = pathRaw.getMap(j);
                            double lng = point.getDouble("longitude");
                            double lat = point.getDouble("latitude");
                            if (point.hasKey("color")) {
                                hasColoredLine = true;
                                shape.pathColors[j] = Color.parseColor(point.getString("color"));
                            }
                            shape.path[j] = new LatLng(lat, lng);
                            latLngBoundsBuilder.include(shape.path[j]);
                        }
                        if (!hasColoredLine) {
                            shape.pathColors = new int[0];
                        }
                    } else {
                        return;
                    }
                    shapesList[i] = shape;
                    shape.bounds = addMarginForLatLngBoundsBuilder(latLngBoundsBuilder.build(), shape.width, this.scale);
                    overlayBoundsBuilder.include(shape.bounds.northeast);
                    overlayBoundsBuilder.include(shape.bounds.southwest);
                }
                this.overlayBounds = overlayBoundsBuilder.build();
                this.shapes = shapesList;
            } catch (Exception e) {
                this.shapes = null;
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            parseShape(this.rawShapes);
            if (this.shapes == null) {
                return null;
            }
            int[] size = projection(overlayBounds.southwest, overlayBounds.northeast, scale);
            int width = size[0];
            int height = size[1];
            int maxPx = Math.max(width, height);
            float scale = this.scale;
            if (maxPx > LIMIT_RESOLUTION) {
                float factor = maxPx / LIMIT_RESOLUTION;
                width /= factor;
                height /= factor;
                scale /= factor;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            for (Shape shape: shapes) {
                Paint paint = new Paint();
                paint.setStrokeWidth(shape.width * scale);
                if (shape.fillColor != 0) {
                    paint.setColor(shape.fillColor);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                } else if (shape.lineColor != 0) {
                    paint.setColor(shape.lineColor);
                    paint.setStyle(Paint.Style.STROKE);
                } else if (shape.pathColors.length > 0) {
                    paint.setColor(shape.pathColors[0]);
                    paint.setStyle(Paint.Style.STROKE);
                }  else {
                    paint.setColor(Color.TRANSPARENT);
                    paint.setStyle(Paint.Style.STROKE);
                }
                paint.setAntiAlias(true);
                if (shape.circleCenter != null) {
                    drawCircle(canvas, paint, shape, scale);
                } else if (shape.fillColor != 0 || shape.lineColor != 0) {
                    drawPolygon(canvas, paint, shape, scale);
                } else if (shape.pathColors.length > 1) {
                    drawColoredPath(canvas, paint, shape, scale);
                }
            }
            return bitmap;
        }

        private void drawCircle(Canvas canvas, Paint paint, Shape shape, float scale) {
            LatLng startPoint = overlayBounds.southwest;
            int[] point = projection(startPoint, shape.circleCenter, scale);
            canvas.drawCircle(point[0], canvas.getHeight() - point[1], shape.circleRadius * scale, paint);
        }

        private void drawColoredPath(Canvas canvas, Paint paint, Shape shape, float scale) {
            LatLng startPoint = overlayBounds.southwest;
            int[] pointA = projection(startPoint, shape.path[0], scale);
            path.reset();
            path.moveTo(pointA[0], canvas.getHeight() - pointA[1]);
            for (int i = 1; i < shape.path.length; i++) {
                int[] pointB = projection(startPoint, shape.path[i], scale);
                path.lineTo(pointB[0], canvas.getHeight() - pointB[1]);
                if (i + 1 == shape.path.length) {
                    paint.setColor(shape.pathColors[i]);
                    canvas.drawPath(path, paint);
                } else if (paint.getColor() != shape.pathColors[i+1]) {
                    paint.setColor(shape.pathColors[i]);
                    canvas.drawPath(path, paint);
                    path.reset();
                    path.moveTo(pointB[0], canvas.getHeight() - pointB[1]);
                }
            }
        }

        private void drawPolygon(Canvas canvas, Paint paint, Shape shape, float scale) {
            LatLng startPoint = overlayBounds.southwest;
            int[] pointA = projection(startPoint, shape.path[0], scale);
            path.reset();
            path.moveTo(pointA[0], canvas.getHeight() - pointA[1]);
            for (int i = 1; i < shape.path.length; i++) {
                int[] pointB = projection(startPoint, shape.path[i], scale);
                path.lineTo(pointB[0], canvas.getHeight() - pointB[1]);
            }
            if (shape.closePath)
                path.close();

            canvas.drawPath(path, paint);
        }

        /**
         * LatLng to pixel projection
         *
         * @param pointA Point A
         * @param pointB Point B
         * @param scale  Scale factor
         * @return [0] - x, [1] - y
         */
        private int[] projection(LatLng pointA, LatLng pointB, float scale) {
            int[] projection = new int[2];
            float[] distance = new float[3];
            Location.distanceBetween(pointA.latitude, pointA.longitude, pointB.latitude, pointA.longitude, distance);
            projection[1] = (int) (distance[0] * scale);
            Location.distanceBetween(pointA.latitude, pointA.longitude, pointA.latitude, pointB.longitude, distance);
            projection[0] = (int) (distance[0] * scale);
            return projection;
        }
    }

    private static class Shape {
        int lineColor;
        int fillColor;
        int width;
        boolean closePath;
        LatLng[] path;
        LatLng circleCenter;
        int circleRadius;
        int[] pathColors;
        LatLngBounds bounds;
    }
}
