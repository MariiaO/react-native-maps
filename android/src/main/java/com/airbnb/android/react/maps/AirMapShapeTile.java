package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Point;

import java.io.ByteArrayOutputStream;

import static com.airbnb.android.react.maps.TileUtils.DIMENSION;

/**
 * Created by alex on 6/13/17.
 */

public class AirMapShapeTile extends AirMapFeature {

    private GoogleMap map;

    private TileOverlayOptions tileOverlayOptions;
    private TileOverlay tileOverlay;
    private ShapeTileProvider tileProvider;

    private float zIndex;
    private ReadableArray rawShapes;
    private Shape[] shapes = null;
    private LatLngBounds overlayBounds = null;
    private AsyncTask<Void, Void, Void> parseShapesTask;


    public AirMapShapeTile(Context context) {
        super(context);
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (tileOverlay != null) {
            tileOverlay.setZIndex(zIndex);
        }
    }

    public TileOverlayOptions getTileOverlayOptions() {
        if (tileOverlayOptions == null) {
            tileOverlayOptions = createTileOverlayOptions();
        }
        return tileOverlayOptions;
    }

    private TileOverlayOptions createTileOverlayOptions() {
        TileOverlayOptions options = new TileOverlayOptions();
        options.zIndex(zIndex);
        this.tileProvider = new ShapeTileProvider(shapes, overlayBounds);
        options.tileProvider(this.tileProvider);
        return options;
    }

    @Override
    public Object getFeature() {
        return tileOverlay;
    }

    @Override
    public void addToMap(GoogleMap map) {
        this.map = map;
        this.tileOverlay = map.addTileOverlay(getTileOverlayOptions());
    }

    @Override
    public void removeFromMap(GoogleMap map) {
        tileOverlay.remove();
    }

    public void setShapes(ReadableArray shapes) {
        this.rawShapes = shapes;
        if (this.parseShapesTask != null) {
            this.parseShapesTask.cancel(false);
        }
        this.parseShapesTask = new ParseShapesTask(rawShapes).execute();
    }


    public class ShapeTileProvider implements TileProvider {
        private final Shape[] shapes;
        private final LatLngBounds shapeBounds;

        public ShapeTileProvider(Shape[] shapes, LatLngBounds shapeBounds) {
            this.shapes = shapes;
            this.shapeBounds = shapeBounds;
        }

        @Override
        public Tile getTile(int x, int y, int zoom) {
            if (shapes == null || shapeBounds == null) {
                return TileProvider.NO_TILE;
            }
            Matrix matrix = new Matrix();
            double scale = Math.pow(2, zoom) * TileUtils.SCALE / TileUtils.FACTOR;
            matrix.postScale((float) scale, (float) scale);
            matrix.postTranslate(-x * DIMENSION, -y * DIMENSION);
            Bitmap bitmap = Bitmap.createBitmap(DIMENSION, DIMENSION, Bitmap.Config.ARGB_8888); //save memory on old phones
            Canvas c = new Canvas(bitmap);
            c.setMatrix(matrix);
            drawCanvas(c, scale, x, y, zoom);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, stream);
            return new Tile(DIMENSION, DIMENSION, stream.toByteArray());
        }

        private void drawCanvas(Canvas canvas, double scale, int x, int y, int zoom) {
            for (Shape shape: shapes) {
                Paint paint = new Paint();
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(getLineWidth(shape.width, scale));
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
                if (shape.fillColor != 0 || shape.lineColor != 0) {
                    drawPolygon(canvas, paint, shape, scale);
                } else if (shape.pathColors.length > 1) {
                    drawColoredPath(canvas, paint, shape, scale);
                }
            }
        }

        private void drawColoredPath(Canvas canvas, Paint paint, Shape shape, double scale) {
            Point pointA = TileUtils.toPoint(shape.path[0]); //first point
            Path path= new Path();
            path.moveTo((float) (pointA.x), (float) (pointA.y));
            for (int i = 1; i < shape.path.length; i++) {
                Point pointB = TileUtils.toPoint(shape.path[i]);
                path.lineTo((float) (pointB.x), (float) (pointB.y));
                if (i + 1 == shape.path.length) {
                    paint.setColor(shape.pathColors[i]);
                    canvas.drawPath(path, paint);
                } else if (paint.getColor() != shape.pathColors[i+1]) {
                    paint.setColor(shape.pathColors[i]);
                    canvas.drawPath(path, paint);
                    path.reset();
                    path.moveTo((float) (pointB.x), (float) (pointB.y));
                }
            }
        }

        private void drawPolygon(Canvas canvas, Paint paint, Shape shape, double scale) {
            Point pointA = TileUtils.toPoint(shape.path[0]); //first point
            Path path= new Path();
            path.moveTo((float) (pointA.x), (float) (pointA.y));
            for (int i = 1; i < shape.path.length; i++) {
                Point pointB = TileUtils.toPoint(shape.path[i]);
                path.lineTo((float) (pointB.x), (float) (pointB.y));
            }
            if (shape.closePath)
                path.close();

            canvas.drawPath(path, paint);
        }

        private float getLineWidth(float width, double scale) {
            return (float) (width / scale);
        }

    }

    private class ParseShapesTask extends AsyncTask<Void, Void, Void> {

        private static final double BOUNDS_MARGIN = 0.000006;

        private Shape[] shapes;
        private volatile LatLngBounds overlayBounds;
        private ReadableArray rawShapes;

        public ParseShapesTask(ReadableArray rawShapes) {
            this.rawShapes = rawShapes;
        }

        @Override
        protected Void doInBackground(Void... params) {
            parseShape(this.rawShapes);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (shapes != null && ((ThemedReactContext)getContext()).hasCurrentActivity()) {
                AirMapShapeTile.this.shapes = shapes;
                AirMapShapeTile.this.overlayBounds = overlayBounds;
                AirMapShapeTile.this.tileOverlayOptions = null;
                if (map != null) {
                    removeFromMap(map);
                    addToMap(map);
                }
            }
        }

        private void parseShape(ReadableArray rawShapes) {
            LatLngBounds.Builder overlayBoundsBuilder = new LatLngBounds.Builder();
            Shape[] shapesList = new Shape[rawShapes.size()];
            for(int i = 0; i < rawShapes.size(); i++) {
                Shape shape = new Shape();
                shapesList[i] = shape;
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

                ReadableArray pathRaw = shapeRaw.getArray("path");
                LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
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
                if (shape.path.length > 0) {
                    shape.bounds = addMarginForLatLngBoundsBuilder(latLngBoundsBuilder.build(), shape.width);
                    overlayBoundsBuilder.include(shape.bounds.northeast);
                    overlayBoundsBuilder.include(shape.bounds.southwest);
                }
            }
            try {
                this.overlayBounds = overlayBoundsBuilder.build();
            } catch (IllegalStateException e) {
                this.overlayBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(0, 0));
            }
            this.shapes = shapesList;
        }

        private LatLngBounds addMarginForLatLngBoundsBuilder(LatLngBounds overlayBounds, int width) {

            double margin = BOUNDS_MARGIN * width;
            LatLng southwest = new LatLng(overlayBounds.southwest.latitude - margin, overlayBounds.southwest.longitude - margin);
            LatLng northeast = new LatLng(overlayBounds.northeast.latitude + margin, overlayBounds.northeast.longitude + margin);
            return new LatLngBounds(southwest, northeast);
        }
    }

    private static class Shape {
        int lineColor;
        int fillColor;
        int width;
        boolean closePath;
        LatLng[] path;
        int[] pathColors;
        LatLngBounds bounds;
    }

}
