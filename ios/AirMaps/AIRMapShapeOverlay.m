//
//  AIRMapOverlay.m
//  AirMapsExplorer
//
//  Created by Akex Vegner on 5/17/17.
//

#import "AIRMapShapeOverlay.h"

#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTImageLoader.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>


static float const DEFAULT_SCALE = 4;
static int const DEFAULT_WIDTH = 2;
static Boolean const DEFAULT_CLOSE_PATH = FALSE;
static double const BOUNDS_MARGIN = 0.000015;
static float const LIMIT_RESOLUTION = 4096;


#pragma mark Shape model

@interface Shape : NSObject

@property (nonatomic, strong) NSArray <NSValue *> *points;
@property (nonatomic, strong) NSArray <UIColor *> *pointColors;
@property (nonatomic, strong) NSValue *circleCenter;
@property (nonatomic, assign) NSInteger circleRadius;
@property (nonatomic, strong) UIColor *strokeColor;
@property (nonatomic, strong) UIColor *fillColor;
@property (nonatomic, assign) NSInteger strokeWidth;
@property (nonatomic, assign) BOOL closePath;
@property (nonatomic, assign) CLLocationCoordinate2D southwest;
@property (nonatomic, assign) CLLocationCoordinate2D northeast;
@property (nonatomic, assign) MKMapRect mapRect;

@end

@implementation Shape
{
}
@end

#pragma mark ShapeParser Container to store parces shapes.

@interface ParsedShapes : NSObject

@property (nonatomic, strong) NSArray <Shape *> *shapes;
@property (nonatomic, assign) CGFloat scale;
@property (nonatomic, assign) NSInteger width;
@property (nonatomic, assign) CLLocationCoordinate2D southwest;
@property (nonatomic, assign) CLLocationCoordinate2D northeast;
@property (nonatomic, assign) MKMapRect mapRect;
@property (nonatomic, assign) CGSize size;

@end

@implementation ParsedShapes
{
}

@end



#pragma mark ShapeParser Container to store parces shapes.

@interface ShapePath : NSObject

@property (nonatomic, strong) UIBezierPath *path;
@property (nonatomic, strong) UIColor *strokeColor;
@property (nonatomic, strong) UIColor *fillColor;

@end

@implementation ShapePath
{
}

@end

#pragma mark AIRMapPathOverlay

@interface AIRMapShapeOverlay()

@property (nonatomic, strong, readwrite) UIImage *overlayImage;
@property (nonatomic, strong) NSArray<ShapePath*> *shapePaths;
@property (nonatomic, assign) CGSize size;
- (CLLocationDistance) distanceBetweend:(CLLocationCoordinate2D)from and:(CLLocationCoordinate2D)to;

@end

@implementation AIRMapShapeOverlay
{
}

- (void)setRotation:(NSInteger)rotation {
    _rotation = rotation;
    [self update];
}

- (void)setTransparency:(CGFloat)transparency {
    _transparency = transparency;
    [self update];
}

- (void)setZIndex:(NSInteger)zIndex {
    _zIndex = zIndex;
    self.layer.zPosition = _zIndex;
    [self update];
}

- (void)setScale:(CGFloat)scale {
    _scale = scale;
    [self resetShape];
}

- (void) setShapes:(NSArray*)shapes {
    _shapes = shapes;
    [self resetShape];
}

-(void) resetShape {
    if (_scale > 0.0001 && _shapes != nil) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            // Background work
            ParsedShapes *parsedShapes = [self parseShapes:_shapes];
            if (parsedShapes != nil) {
                parsedShapes.scale = _scale;
                [self regionForPath:parsedShapes];
                [self calculateScaledSize:parsedShapes];
                NSArray<ShapePath*> *shapePaths = [self preparePathsForDraw:parsedShapes];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    _shapePaths = shapePaths;
                    _size = parsedShapes.size;
                    _mapRect = parsedShapes.mapRect;
                    [self drawImage];
                    [self createOverlayRendererIfPossible];
                    [self update];
                });
            }
        });
        
        [self drawImage];
        [self createOverlayRendererIfPossible];
        [self update];
    }
}

- (ParsedShapes*) parseShapes:(NSArray*)shapesRaw {
    ParsedShapes *parsedShapes = [ParsedShapes new];
    parsedShapes.width = DEFAULT_WIDTH;
    NSMutableArray *shapes = [NSMutableArray arrayWithCapacity:shapesRaw.count];
    parsedShapes.shapes = shapes;
    // Prevent parse errors
    @try {
        for (NSUInteger i = 0; i < [shapesRaw count]; i++) {
            NSDictionary *shapeRaw = (NSDictionary *) shapesRaw[i];
            Shape *shape = [Shape new];
            [shapes addObject:shape];
            
            NSNumber *strokeWidth = shapeRaw[@"strokeWidth"];
            if (strokeWidth != nil) {
                shape.strokeWidth = [strokeWidth integerValue];
                if (parsedShapes.width > shape.strokeWidth) {
                    parsedShapes.width = shape.strokeWidth;
                }
            } else {
                shape.strokeWidth = DEFAULT_WIDTH;
            }
            
            NSString *strokeColor = shapeRaw[@"strokeColor"];
            if (strokeColor != nil) {
                shape.strokeColor = [self colorWithHexString:strokeColor];
            } else {
                shape.strokeColor = nil;
            }
            
            NSNumber *closePath = shapeRaw[@"closePath"];
            if (closePath != nil) {
                shape.closePath = [closePath boolValue];
            } else {
                shape.closePath = DEFAULT_CLOSE_PATH;
            }
            
            NSString *fillColor = shapeRaw[@"fillColor"];
            if (fillColor != nil) {
                shape.fillColor = [self colorWithHexString:fillColor];
                shape.closePath = true;
            } else {
                shape.fillColor = nil;
            }
            
            NSMutableArray *paths = [NSMutableArray array];
            NSMutableArray *colors = [NSMutableArray array];
            NSDictionary *circle = shapeRaw[@"circle"];
            if (circle != nil) {
                NSNumber *lng = circle[@"longitude"];
                NSNumber *lat = circle[@"latitude"];
                CLLocationCoordinate2D coordicate = CLLocationCoordinate2DMake([lat doubleValue], [lng doubleValue]);
                shape.circleCenter = [NSValue valueWithMKCoordinate:coordicate];
                NSNumber *radius = circle[@"radius"];
                shape.circleRadius = [radius integerValue];;
            } else {
                NSArray *pathArray = [shapeRaw mutableArrayValueForKey:@"path"];
                if (pathArray != nil && pathArray.count > 0) {
                    for (id it in pathArray) {
                        NSDictionary *aPath = (NSDictionary*)it;
                        NSNumber *lng = aPath[@"longitude"];
                        NSNumber *lat = aPath[@"latitude"];
                        CLLocationCoordinate2D coordicate = CLLocationCoordinate2DMake([lat doubleValue], [lng doubleValue]);
                        [paths addObject:[NSValue valueWithMKCoordinate:coordicate]];
                        
                        NSString *color = aPath[@"color"];
                        if (color != nil) {
                            [colors addObject:[self colorWithHexString:color]];
                        }
                    }
                    
                }
            }
            shape.points = [paths copy];
            shape.pointColors = [colors copy];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"ShapeOverlay parse exception: %@", exception.reason);
        parsedShapes = nil;
    }
    @finally {}
    
    return parsedShapes;
}

- (void)createOverlayRendererIfPossible {
    if (MKMapRectIsEmpty(_mapRect) || !self.overlayImage) return;
    __weak typeof(self) weakSelf = self;
    self.renderer = [[AIRMapShapeOverlayRenderer alloc] initWithOverlay:weakSelf];
}


- (void)update {
    if (!_renderer) return;
    _renderer.rotation = _rotation;
    _renderer.transparency = _transparency;
    
    if (_map == nil) return;
    [_map removeOverlay:self];
    [_map addOverlay:self];
}

#pragma mark Prepare for drawing

- (void) calculateScaledSize:(ParsedShapes*)parsedShapes {
    CGSize size = [self projectionSize:parsedShapes.southwest and:parsedShapes.northeast withScale:parsedShapes.scale];
    NSInteger maxPx = MAX(size.width, size.height);
    if (maxPx > LIMIT_RESOLUTION) {
        NSInteger factor = maxPx / LIMIT_RESOLUTION;
        size.width /= factor;
        size.height /= factor;
        parsedShapes.scale /= factor;
    }
    parsedShapes.size = size;
}

- (NSArray<ShapePath*> *) preparePathsForDraw:(ParsedShapes*)parsedShapes {
    NSMutableArray *shapePaths = [NSMutableArray array];
    NSArray *arrShapes = parsedShapes.shapes;
    for (Shape *shape in arrShapes) {
        UIBezierPath *currentPath;
        UIColor *currentColor;
        if (shape.circleCenter != nil) {
            currentPath = [self makeBezierPath:shape withScale:parsedShapes.scale];
            CGPoint circleCenterPoint = [self projection:parsedShapes.size from:parsedShapes.southwest and:[shape.circleCenter MKCoordinateValue] withScale:parsedShapes.scale];
            [currentPath addArcWithCenter:circleCenterPoint radius:(shape.circleRadius * parsedShapes.scale) startAngle:0 endAngle:2 * M_PI clockwise:YES];
            currentColor = shape.strokeColor;
        } else {
            for (int i = 1; i < [shape.points count];  i++) {
                UIColor *lineColor = nil;
                if (shape.strokeColor != nil) {
                    lineColor = shape.strokeColor != nil ? shape.strokeColor : [UIColor clearColor];
                } else if (shape.pointColors.count > 0 && shape.pointColors[i] != nil) {
                    lineColor = shape.pointColors[i];
                } else {
                    lineColor = [UIColor clearColor];
                }
                
                if (![lineColor isEqual:currentColor]) {
                    if (currentPath != nil) {
                        if (shape.closePath) {
                            [currentPath closePath];
                        }
                        ShapePath *shapePath = [ShapePath new];
                        shapePath.path = currentPath;
                        shapePath.fillColor = shape.fillColor;
                        shapePath.strokeColor = currentColor;
                        [shapePaths addObject:shapePath];
                    }
                    currentColor = lineColor;
                    currentPath = [self makeBezierPath:shape withScale:parsedShapes.scale];
                    CGPoint startPathPoint = [self projection:parsedShapes.size from:parsedShapes.southwest and:[shape.points[i-1] MKCoordinateValue] withScale:parsedShapes.scale];
                    [currentPath moveToPoint:startPathPoint];
                }
                CLLocationCoordinate2D coordinate = [shape.points[i] MKCoordinateValue];
                CGPoint point = [self projection:parsedShapes.size from:parsedShapes.southwest and:coordinate withScale:parsedShapes.scale];
                [currentPath addLineToPoint:point];
            }
            if (shape.closePath) {
                [currentPath closePath];
            }
        }
        ShapePath *shapePath = [ShapePath new];
        shapePath.path = currentPath;
        shapePath.fillColor = shape.fillColor;
        shapePath.strokeColor = currentColor;
        [shapePaths addObject:shapePath];
    }
    return [shapePaths copy];
}

- (UIBezierPath*) makeBezierPath:(Shape*)shape withScale:(CGFloat)scale {
    UIBezierPath *path = [UIBezierPath bezierPath];
    path.lineWidth = shape.strokeWidth  * scale;
    return path;
}

- (void) regionForPath:(ParsedShapes*)parsedShapes {
    double minLat = 90.0f;
    double maxLat = -90.0f;
    double minLng = 180.0f;
    double maxLng = -180.0f;
    double maxShapeWidth = 0;
    for (Shape *shape in parsedShapes.shapes) {
        if (shape.circleCenter != nil) {
            if (maxShapeWidth < shape.circleRadius) maxShapeWidth = shape.circleRadius;
            if (maxShapeWidth < shape.strokeWidth) maxShapeWidth = shape.strokeWidth;
            CLLocationCoordinate2D coordinate = [shape.circleCenter MKCoordinateValue];
            if (coordinate.latitude < minLat) minLat = coordinate.latitude;
            if (coordinate.latitude > maxLat) maxLat = coordinate.latitude;
            if (coordinate.longitude < minLng) minLng = coordinate.longitude;
            if (coordinate.longitude > maxLng) maxLng = coordinate.longitude;
        } else {
            for (NSValue *point in shape.points) {
                CLLocationCoordinate2D coordinate = [point MKCoordinateValue];
                if (coordinate.latitude < minLat) minLat = coordinate.latitude;
                if (coordinate.latitude > maxLat) maxLat = coordinate.latitude;
                if (coordinate.longitude < minLng) minLng = coordinate.longitude;
                if (coordinate.longitude > maxLng) maxLng = coordinate.longitude;
            }
        }
        if (maxShapeWidth < shape.strokeWidth) maxShapeWidth = shape.strokeWidth;
    }
    double margin = BOUNDS_MARGIN  * maxShapeWidth * parsedShapes.scale;
    parsedShapes.southwest = CLLocationCoordinate2DMake(minLat - margin, minLng - margin);
    parsedShapes.northeast = CLLocationCoordinate2DMake(maxLat + margin, maxLng + margin);
    MKMapPoint upperLeft = MKMapPointForCoordinate(parsedShapes.southwest);
    MKMapPoint lowerRight = MKMapPointForCoordinate(parsedShapes.northeast);
    
    parsedShapes.mapRect = MKMapRectMake(MIN(upperLeft.x, lowerRight.x),
                                         MIN(upperLeft.y, lowerRight.y),
                                         fabs(lowerRight.x - upperLeft.x),
                                         fabs(lowerRight.y - upperLeft.y));
}


- (CGSize)projectionSize:(CLLocationCoordinate2D)from and:(CLLocationCoordinate2D)to withScale:(double)scale {
    NSInteger width = scale  *[self distanceBetweend:from and:CLLocationCoordinate2DMake(from.latitude, to.longitude)];
    NSInteger height = scale  *[self distanceBetweend:from and:CLLocationCoordinate2DMake(to.latitude, from.longitude)];
    return CGSizeMake(width, height);
}

- (CGPoint)projection:(CGSize)size from:(CLLocationCoordinate2D)from and:(CLLocationCoordinate2D)to withScale:(double)scale {
    NSInteger width = scale  *[self distanceBetweend:from and:CLLocationCoordinate2DMake(from.latitude, to.longitude)];
    NSInteger height = scale  *[self distanceBetweend:from and:CLLocationCoordinate2DMake(to.latitude, from.longitude)];
    return CGPointMake(width, size.height - height);
}

#pragma mark Draw paths

- (void)drawImage {
    UIGraphicsBeginImageContextWithOptions(_size, NO, 0.0);
    // Start draw path
    if (_shapePaths != nil) {
        for (int i = 0; i < [_shapePaths count];  i++) {
            ShapePath *shapePath = (ShapePath *) _shapePaths[i];
            if (shapePath.fillColor != nil) {
                [shapePath.fillColor setFill];
                [shapePath.path fill];
            }
            if (shapePath.strokeColor != nil) {
                [shapePath.strokeColor setStroke];
                [shapePath.path stroke];
            }
        }
    }
    // End draw path
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    _overlayImage = image;
}


- (CLLocationDistance) distanceBetweend:(CLLocationCoordinate2D)from and:(CLLocationCoordinate2D)to {
    MKMapPoint point1 = MKMapPointForCoordinate(from);
    MKMapPoint point2 = MKMapPointForCoordinate(to);
    return MKMetersBetweenMapPoints(point1, point2);
}

#pragma mark MKOverlay implementation

- (CLLocationCoordinate2D)coordinate
{
    return MKCoordinateForMapPoint(MKMapPointMake(MKMapRectGetMidX(_mapRect), MKMapRectGetMidY(_mapRect)));
}

- (MKMapRect)boundingMapRect
{
    return _mapRect;
}

- (BOOL)intersectsMapRect:(MKMapRect)mapRect
{
    return MKMapRectIntersectsRect(_mapRect, mapRect);
}

- (BOOL)canReplaceMapContent
{
    return NO;
}

#pragma mark Color utils

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]


- (UIColor*) colorWithHexString: (NSString *) str
{
    const char *cStr = [str cStringUsingEncoding:NSASCIIStringEncoding];
    long x = strtol(cStr+1, NULL, 16);
    UIColor *color = UIColorFromRGB(x);
    return color;
}

#pragma mark Recycle object
- (void)recycle {
    _overlayImage = nil;
    _shapes = nil;
    _renderer = nil;
}

@end
