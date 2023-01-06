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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const react_1 = __importDefault(require("react"));
// import {
//   View,
// } from 'react-native';
const decorateMapComponent_1 = __importStar(require("./decorateMapComponent"));
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
class MapShapeTile extends react_1.default.Component {
    render() {
        const AIRMapShapeTile = this.getNativeComponent();
        return <AIRMapShapeTile {...this.props}/>;
    }
}
// MapShapeTile.propTypes = propTypes;
exports.default = (0, decorateMapComponent_1.default)(MapShapeTile, 'ShapeTile', {
    google: {
        ios: decorateMapComponent_1.SUPPORTED,
        android: decorateMapComponent_1.USES_DEFAULT_IMPLEMENTATION,
    },
});
