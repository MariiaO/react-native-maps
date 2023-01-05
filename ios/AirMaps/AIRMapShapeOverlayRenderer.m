//
//  AIRMapOverlayView.m
//  AirMapsExplorer
//
//  Created by Akex Vegner on 5/17/16.
//

#import "AIRMapShapeOverlayRenderer.h"
#import "AIRMapShapeOverlay.h"

@implementation AIRMapShapeOverlayRenderer

- (void)drawMapRect:(MKMapRect)mapRect zoomScale:(MKZoomScale)zoomScale inContext:(CGContextRef)context {
    UIImage *image = [(AIRMapShapeOverlay *)self.overlay overlayImage];
  
    CGContextSaveGState(context);
  
    CGImageRef imageReference = image.CGImage;
    
    MKMapRect theMapRect = [self.overlay boundingMapRect];
    CGRect theRect = [self rectForMapRect:theMapRect];
    
    // Core Graphics uses a different coordinate system, where the origin is in the lower left corner.
    // So, we need to flip the image by translating the image by 0 units on the x axis and by the images
    // height on the y axis.
    CGContextTranslateCTM(context, 0, theRect.size.height);
    CGContextScaleCTM(context, 1.0, -1.0);
    
    CGContextSetAlpha(context, 1.0 - self.transparency);
    CGContextRotateCTM(context, M_PI * self.rotation / 180.0);
  
    CGContextAddRect(context, theRect);
    CGContextDrawImage(context, theRect, imageReference);
  
    CGContextRestoreGState(context);
}

- (BOOL)canDrawMapRect:(MKMapRect)mapRect zoomScale:(MKZoomScale)zoomScale {
    return [(AIRMapShapeOverlay *)self.overlay overlayImage] != nil;
}

@end
