package com.airbnb.android.react.maps;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class AirMapShapeOverlayManager extends ViewGroupManager<AirMapShapeOverlay> {

    public AirMapShapeOverlayManager() {
    }

    @Override
    public String getName() {
        return "AIRMapShapeOverlay";
    }

    @Override
    public AirMapShapeOverlay createViewInstance(ThemedReactContext context) {
        return new AirMapShapeOverlay(context);
    }

    @ReactProp(name = "shapes")
    public void setShapes(AirMapShapeOverlay view, ReadableArray shapes) {
        view.setShapes(shapes);
    }

    @ReactProp(name = "name")
    public void setOverlayName(AirMapShapeOverlay view, String name) {
        view.setOverlayName(name);
    }

    @ReactProp(name = "rotation", defaultFloat = 0.0f)
    public void setOverlayRotation(AirMapShapeOverlay view, float rotation) {
        view.setRotation(rotation);
    }

    @ReactProp(name = "scale", defaultFloat = 2.0f)
    public void setScale(AirMapShapeOverlay view, float scale) {
        view.setScale(scale);
    }

    @ReactProp(name = "transparency", defaultFloat = 0.0f)
    public void setOverlayTransparency(AirMapShapeOverlay view, float transparency) {
        view.setTransparency(transparency);
    }

    @ReactProp(name = "zIndex", defaultFloat = 1.0f)
    public void setZIndex(AirMapShapeOverlay view, float zIndex) {
        view.setZIndex(zIndex);
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of("onPress", MapBuilder.of("registrationName", "onPress"));
    }
}
