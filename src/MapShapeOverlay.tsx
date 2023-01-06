import React, {Component} from 'react';
// import PropTypes from 'prop-types';
import {
  // View,
  StyleSheet,
  Animated,
  ViewProps,
} from 'react-native';

import decorateMapComponent, {
  NativeComponent,
  SUPPORTED,
  USES_DEFAULT_IMPLEMENTATION,
} from './decorateMapComponent';

// const viewConfig = {
//   uiViewClassName: 'AIR<provider>MapPathOverlay',
//   validAttributes: {
//     shape: true,
//   },
// };

// const defaultProps = {
//   name: '',
//   rotation: 0,
//   scale: 2.0,
//   zIndex: 0,
//   transparency: 0.0,
//   onPress: () => {},
// };

// const propTypes = {
//   // ...ViewPropTypes,
//   // shape support path in path
//   // to use one color set shape.color
//   // to use multiple colors set shape.path.color
//   // it also support mulpy path shape.path.path

//   scale: PropTypes.number,

//   shapes: PropTypes.arrayOf(PropTypes.shape({
//     path: PropTypes.arrayOf(PropTypes.shape({
//       latitude: PropTypes.number,
//       longitude: PropTypes.number,
//       color: PropTypes.string,
//     })),
//     circle: PropTypes.shape({
//       latitude: PropTypes.number,
//       longitude: PropTypes.number,
//       radius: PropTypes.number,
//     }),
//     strokeColor: PropTypes.string,
//     strokeWidth: PropTypes.number,
//     closePath: PropTypes.bool,
//     fillColor: PropTypes.string,
//   })),
//   // A name for the image overlay
//   name: PropTypes.string,
//   // A number of degrees from north to rotate the image clockwise
//   rotation: PropTypes.number,
//   // A number indicating the render order of the image
//   zIndex: PropTypes.number,
//   // A decimal from 0 to 1 in indicating the opaqueness of the overlay 1 = completely transparent.
//   transparency: PropTypes.number,
//   // Callback that is called when the user presses on the overlay
//   onPress: PropTypes.func,
// };

export type MapOverlayProps = ViewProps;

export class MapShapeOverlay extends Component<MapOverlayProps> {
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

// MapShapeOverlay.propTypes = propTypes;
// MapShapeOverlay.defaultProps = defaultProps;
// MapShapeOverlay.viewConfig = viewConfig;

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
