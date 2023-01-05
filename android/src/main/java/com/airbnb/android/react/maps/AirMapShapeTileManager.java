package com.airbnb.android.react.maps;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Created by alex on 6/13/17.
 */

public class AirMapShapeTileManager  extends ViewGroupManager<AirMapShapeTile> {

    private DisplayMetrics metrics;

    public AirMapShapeTileManager(ReactApplicationContext reactContext) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            metrics = new DisplayMetrics();
            ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRealMetrics(metrics);
        } else {
            metrics = reactContext.getResources().getDisplayMetrics();
        }
    }

    @Override
    public String getName() {
        return "AIRMapShapeTile";
    }

    @Override
    public AirMapShapeTile createViewInstance(ThemedReactContext context) {
        return new AirMapShapeTile(context);
    }

    @ReactProp(name = "shapes")
    public void setShapes(AirMapShapeTile view, ReadableArray shape) {
        view.setShapes(shape);
    }

    @ReactProp(name = "zIndex", defaultFloat = -1.0f)
    public void setZIndex(AirMapShapeTile view, float zIndex) {
        view.setZIndex(zIndex);
    }

}
