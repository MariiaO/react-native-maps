import * as React from 'react';
import { Animated, ViewProps } from 'react-native';
import { NativeComponent } from './decorateMapComponent';
export declare type MapOverlayProps = ViewProps;
export declare class MapShapeOverlay extends React.Component<MapOverlayProps> {
    static Animated: Animated.AnimatedComponent<typeof MapShapeOverlay>;
    getNativeComponent: () => NativeComponent<MapOverlayProps>;
    render(): JSX.Element;
}
declare const _default: typeof MapShapeOverlay;
export default _default;
