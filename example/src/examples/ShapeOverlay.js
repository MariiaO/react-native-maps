import React, {Component} from 'react';
import {StyleSheet, View, Dimensions} from 'react-native';

import MapView, {ShapeOverlay as MapShapeOverlay} from 'react-native-maps';

import customData from './assets/fuel_use.json';

const {width, height} = Dimensions.get('window');

const ASPECT_RATIO = width / height;
const LATITUDE = 40.499286;
const LONGITUDE = -98.633583;
const LATITUDE_DELTA = 0.0222;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;

const RED = '#ff0000';
const YELLOW = '#ffff00';
const GREEN = '#008000';

export default class ShapeOverlay extends Component {
  static propTypes = {
    provider: MapView.ProviderPropType,
  };

  constructor(props) {
    super(props);

    const pathWithColor = customData
      .filter(ob => ob.geometry.type === 'Point')
      .map(ob => {
        return {
          latitude: ob.geometry.coordinates[1],
          longitude: ob.geometry.coordinates[0],
          color: this.getColor(
            ob.properties['Instantaneous Liquid Fuel Usage (L/hour)'],
          ),
        };
      });

    this.state = {
      region: {
        latitude: LATITUDE,
        longitude: LONGITUDE,
        latitudeDelta: LATITUDE_DELTA,
        longitudeDelta: LONGITUDE_DELTA,
      },
      pathOverlay: {
        shapes: [
          {
            path: pathWithColor.slice(),
            strokeWidth: 10,
            fillColor: GREEN,
          },
          {
            path: pathWithColor.slice(),
            strokeWidth: 6,
          },
          {
            path: pathWithColor.slice(),
            strokeWidth: 1,
            strokeColor: RED,
          },
        ],
        scale: 2,
        rotation: 0,
        transparency: 0.0,
        zIndex: 2,
      },
      pathOverlay2: {
        shapes: [
          {
            path: pathWithColor.slice(),
            strokeWidth: 6,
          },
        ],
        scale: 2,
        rotation: 0,
        transparency: 0.0,
        zIndex: 3,
      },

      pathOverlay3: {
        shapes: [
          {
            path: pathWithColor.slice(),
            strokeWidth: 1,
            strokeColor: RED,
          },
        ],
        scale: 2,
        rotation: 0,
        transparency: 0.0,
        zIndex: 4,
      },
      pathOverlay4: {
        shapes: [
          {
            path: pathWithColor.slice(),
            strokeWidth: 6,
          },
          {
            circle: {
              latitude: LATITUDE,
              longitude: LONGITUDE,
              radius: 4,
            },
            strokeWidth: 0,
            fillColor: RED,
          },
        ],
        scale: 20,
        rotation: 0,
        transparency: 0.0,
        zIndex: 5,
      },
    };
  }

  onOverlayPress = e => {
    console.log(e.nativeEvent.coordinate);
  };

  render() {
    return (
      <View style={styles.container}>
        <MapView
          provider={this.props.provider}
          style={styles.map}
          initialRegion={this.state.region}>
          <MapShapeOverlay
            {...this.state.pathOverlay3}
            key="2"
            onPress={this.onOverlayPress}
          />
        </MapView>
      </View>
    );
  }

  getColor(fuelsPerHour) {
    if (fuelsPerHour < 45) {
      return GREEN;
    } else if (fuelsPerHour < 50) {
      return YELLOW;
    } else {
      return RED;
    }
  }
}

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  bubble: {
    backgroundColor: 'rgba(255,255,255,0.7)',
    paddingHorizontal: 18,
    paddingVertical: 12,
    borderRadius: 20,
  },
  latlng: {
    width: 200,
    alignItems: 'stretch',
  },
  button: {
    width: 80,
    paddingHorizontal: 12,
    alignItems: 'center',
    marginHorizontal: 10,
  },
  buttonContainer: {
    flexDirection: 'row',
    marginVertical: 20,
    backgroundColor: 'transparent',
  },
});
