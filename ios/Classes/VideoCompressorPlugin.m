#import "VideoCompressorPlugin.h"
#if __has_include(<video_compressor/video_compressor-Swift.h>)
#import <video_compressor/video_compressor-Swift.h>
#else
#import "video_compressor-Swift.h"
#endif

@implementation VideoCompressorPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftVideoCompressorPlugin registerWithRegistrar:registrar];
}
@end
