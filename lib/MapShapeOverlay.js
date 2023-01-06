"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
const react_1 = __importStar(require("react"));
// import PropTypes from 'prop-types';
const react_native_1 = require("react-native");
const decorateMapComponent_1 = __importStar(require("./decorateMapComponent"));
const viewConfig = {
    uiViewClassName: 'AIR<provider>MapPathOverlay',
    validAttributes: {
        shape: true,
    },
};
const defaultProps = {
    name: '',
    rotation: 0,
    scale: 2.0,
    zIndex: 0,
    transparency: 0.0,
    onPress: () => { },
};
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
class MapShapeOverlay extends react_1.Component {
    render() {
        const AIRMapShapeOverlay = this.getNativeComponent();
        return (<AIRMapShapeOverlay {...this.props} style={[styles.overlay, this.props.style]}/>);
    }
}
// MapShapeOverlay.propTypes = propTypes;
MapShapeOverlay.defaultProps = defaultProps;
MapShapeOverlay.viewConfig = viewConfig;
const styles = react_native_1.StyleSheet.create({
    overlay: {
        position: 'absolute',
        backgroundColor: 'transparent',
    },
});
MapShapeOverlay.Animated = react_native_1.Animated.createAnimatedComponent(MapShapeOverlay);
exports.default = (0, decorateMapComponent_1.default)(MapShapeOverlay, 'ShapeOverlay', {
    google: {
        ios: decorateMapComponent_1.SUPPORTED,
        android: decorateMapComponent_1.USES_DEFAULT_IMPLEMENTATION,
    },
});
