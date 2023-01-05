//
//  AIRMapShapeOverlayManager.m
//  AirMapsExplorer
//
//  Created by Akex Vegner on 5/17/16.
//

#import "AIRMapShapeOverlayManager.h"

#import <React/RCTConvert+CoreLocation.h>
#import <React/RCTUIManager.h>
#import <React/UIView+React.h>
#import "AIRMapShapeOverlay.h"

@interface AIRMapShapeOverlayManager () <MKMapViewDelegate>

@end

@implementation AIRMapShapeOverlayManager

RCT_EXPORT_MODULE()

- (UIView *)view {
  AIRMapShapeOverlay *overlay = [AIRMapShapeOverlay new];
  overlay.bridge = self.bridge;
  return overlay;
}

RCT_EXPORT_VIEW_PROPERTY(shapes, NSArray)
RCT_EXPORT_VIEW_PROPERTY(scale, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(name, NSString)
RCT_EXPORT_VIEW_PROPERTY(rotation, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(transparency, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(zIndex, NSInteger)
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

@end
