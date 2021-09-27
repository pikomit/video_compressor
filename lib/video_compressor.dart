import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

/// The allowed video quality to pass for compression
enum VideoQuality {
  VERY_LOW,
  LOW,
  MEDIUM,
  HIGH,
  VERY_HIGH,
}

class VideoCompressor {
  static const MethodChannel _channel = MethodChannel('fr.supernovae.pikomit.video_compressor');

  /// A stream to listen to video compression progress
  static const EventChannel progressStream = EventChannel('fr.supernovae.pikomit.video_compressor.events');

  /// This function compresses a given [path] video file and writes the
  /// compressed video file at [destinationPath].
  ///
  /// The required parameters are;
  /// * [path] is path of the provided video file to be compressed.
  /// * [destinationPath] the path where the output compressed video file should
  /// be saved.
  /// * [videoQuality] to allow choosing a video quality that can be
  /// [VideoQuality.very_low], [VideoQuality.low], [VideoQuality.medium],
  /// [VideoQuality.high], and [VideoQuality.very_high].
  ///
  /// The optional parameters are;
  /// * [isMinBitRateEnabled] to determine if the checking for a minimum bitrate
  /// threshold before compression is enabled or not. This defaults to `true`.
  /// * [keepOriginalResolution] to keep the original video height and width when
  /// compressing. This defaults to `false`.
  static Future<Map<String, dynamic>> startCompression({
    required String srcPath,
    required String destPath,
    required VideoQuality videoQuality,
    bool isMinBitRateEnabled = true,
    bool keepOriginalResolution = false,
  }) async {
    return json.decode(await _channel.invokeMethod<dynamic>('start', <String, dynamic>{
      'srcPath': srcPath,
      'destPath': destPath,
      'videoQuality': videoQuality.toString().split('.').last.toLowerCase(),
      'isMinBitRateEnabled': isMinBitRateEnabled,
      'keepOriginalResolution': keepOriginalResolution,
    }));
  }

  /// Call this function to cancel video compression process.
  static void cancelCompression() async {
    await _channel.invokeMethod<dynamic>('cancel');
  }
}

