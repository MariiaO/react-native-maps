//
//  AIRMapShapeOverlay.h
//  AirMapsExplorer
//
//  Created by Akex Vegner on 5/17/16.
//

#import "AIRMapCallout.h"

#import <MapKit/MapKit.h>
#import <UIKit/UIKit.h>

#import "RCTConvert+AirMap.h"
#import <React/RCTComponent.h>
#import "AIRMap.h"
#import "AIRMapShapeOverlayRenderer.h"

@class RCTBridge;

@interface AIRMapShapeOverlay : UIView <MKOverlay>

@property (nonatomic, strong) AIRMapShapeOverlayRenderer *renderer;
@property (nonatomic, weak) AIRMap *map;
@property (nonatomic, weak) RCTBridge *bridge;

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong, readonly) NSArray *shapes;
@property (nonatomic, strong, readonly) UIImage *overlayImage;
@property (nonatomic, assign) NSInteger rotation;
@property (nonatomic, assign) CGFloat transparency;
@property (nonatomic, assign) CGFloat scale;
@property (nonatomic, assign) NSInteger zIndex;

@property (nonatomic, copy) RCTBubblingEventBlock onPress;

#pragma mark MKPathOverlay protocol

@property(nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property(nonatomic, assign) MKMapRect mapRect;
- (BOOL)intersectsMapRect:(MKMapRect)mapRect;
- (BOOL)canReplaceMapContent;
- (void)recycle;

@end
