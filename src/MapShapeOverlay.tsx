import * as React from 'react';
import {StyleSheet, Animated, ViewProps} from 'react-native';

import decorateMapComponent, {
  NativeComponent,
  SUPPORTED,
  USES_DEFAULT_IMPLEMENTATION,
} from './decorateMapComponent';

export type MapOverlayProps = ViewProps;

export class MapShapeOverlay extends React.Component<MapOverlayProps> {
  static Animated: Animated.AnimatedComponent<typeof MapShapeOverlay>;
  getNativeComponent!: () => NativeComponent<MapOverlayProps>;

  render() {
    const AIRMapShapeOverlay = this.getNativeComponent();
    return (
      <AIRMapShapeOverlay
        {...this.props}
        style={[styles.overlay, this.props.style]}
      />
    );
  }
}

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
  },
});

MapShapeOverlay.Animated = Animated.createAnimatedComponent(MapShapeOverlay);

export default decorateMapComponent(MapShapeOverlay, 'ShapeOverlay', {
  google: {
    ios: SUPPORTED,
    android: USES_DEFAULT_IMPLEMENTATION,
  },
});
