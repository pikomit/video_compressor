package fr.supernovae.pikomit.video_compressor

import androidx.annotation.NonNull

import androidx.core.content.ContextCompat

import android.os.Build

import android.content.Context
import android.content.pm.PackageManager

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel

import com.supernovae.pikomit.video_compressor.CompressionListener
import com.supernovae.pikomit.video_compressor.VideoCompressor
import com.supernovae.pikomit.video_compressor.VideoQuality

class VideoCompressorPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  private lateinit var channel : MethodChannel
	private lateinit var event : EventChannel
	
	private var eventSink: EventChannel.EventSink? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fr.supernovae.pikomit.video_compressor")
		event = EventChannel(flutterEngine.dartExecutor.binaryMessenger, "fr.supernovae.pikomit.video_compressor.events")
    
		channel.setMethodCallHandler(this)
		event.setStreamHandler(this)
	}

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
		when (call.method) {
			"start" -> {
				startCompression(call, result)
			}
			"cancel" -> {
				cancelCompression(call, result)
			}
			else -> {
    		result.notImplemented()
			}
		}
  }

	override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
		eventSink = events
	}

	override fun onCancel(arguments: Any?) {
		eventSink = null
	}

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
		event.setStreamHandler(null)
  }

	private fun startCompression(@NonNull call: MethodCall, @NonNull result: Result) {
		val srcPath: String? = call.argument<String>("srcPath")
		val destPath: String? = call.argument<String>("destPath")
		val isMinBitRateEnabled: Boolean? = call.argument<Boolean>("isMinBitRateEnabled")
		val keepOriginalResolution: Boolean? = call.argument<Boolean>("keepOriginalResolution")

		val quality: VideoQuality = when (call.argument<String>("videoQuality")) {
			"very_low" -> VideoQuality.VERY_LOW
			"low" -> VideoQuality.LOW
			"medium" -> VideoQuality.MEDIUM
			"high" -> VideoQuality.HIGH
			"very_high" -> VideoQuality.VERY_HIGH
			else -> VideoQuality.MEDIUM
		}

		if (srcPath == null || destPath == null || isMinBitRateEnabled == null || keepOriginalResolution == null) {
			result.error("ARGS_NOT_FOUND", "Missing some of required arguments", null)
			return
		}

		if (Build.VERSION.SDK_INT >= 23) {
			if (
				ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
			) {
				ActivityCompat.requestPermissions(activity, permissions, 1)
			}
		}

		VideoCompressor.start(
			srcPath = srcPath,
			destPath = destPath,
			quality = quality,
			isMinBitRateEnabled = isMinBitRateEnabled,
			keepOriginalResolution = keepOriginalResolution,
			listener = object : CompressionListener {
				override fun onProgress(percent: Float) {
					Handler(Looper.getMainLooper()).post {
						eventSink?.success(percent)
					}
				}

				override fun onStart() {}

				override fun onSuccess() {
					result.success(destPath)
				}

				override fun onFailure(failureMessage: String) {
					result.success(null)
				}

				override fun onCancelled() {
					Handler(Looper.getMainLooper()).post {
						result.success(null)
					}
				}
			},
		)
	}

	private fun cancelCompression(@NonNull call: MethodCall, @NonNull result: Result) {
		VideoCompressor.cancel()
		result.success(null)
	}
}
