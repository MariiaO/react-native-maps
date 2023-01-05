import React from 'react';

// import {
//   View,
// } from 'react-native';

import decorateMapComponent, {
  USES_DEFAULT_IMPLEMENTATION,
  SUPPORTED,
} from './decorateMapComponent';

// const propTypes = {
//   ...View.propTypes,

//   shapes: PropTypes.arrayOf(PropTypes.shape({
//     path: PropTypes.arrayOf(PropTypes.shape({
//       latitude: PropTypes.number,
//       longitude: PropTypes.number,
//       color: PropTypes.string,
//     })).isRequired,
//     strokeColor: PropTypes.string,
//     strokeWidth: PropTypes.number,
//     closePath: PropTypes.bool,
//     fillColor: PropTypes.string,
//   })),

//   /**
//    * The order in which this tile overlay is drawn with respect to other overlays. An overlay
//    * with a larger z-index is drawn over overlays with smaller z-indices. The order of overlays
//    * with the same z-index is arbitrary. The default zIndex is -1.
//    *
//    * @platform android
//    */
//   zIndex: PropTypes.number,
// };

class MapShapeTile extends React.Component {
  render() {
    const AIRMapShapeTile = this.getNativeComponent();
    return <AIRMapShapeTile {...this.props} />;
  }
}

// MapShapeTile.propTypes = propTypes;

export default decorateMapComponent(MapShapeTile, 'ShapeTile', {
  google: {
    ios: SUPPORTED,
    android: USES_DEFAULT_IMPLEMENTATION,
  },
});
