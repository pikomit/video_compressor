import Flutter
import Photos
import UIKit

public class SwiftVideoCompressorPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
	private var eventSink: FlutterEventSink?
	private var compression: Compression?

	public static func register(with registrar: FlutterPluginRegistrar) {
		let channel = FlutterMethodChannel(name: "fr.supernovae.pikomit.video_compressor", binaryMessenger: registrar.messenger())
		let event = FlutterEventChannel(name: "fr.supernovae.pikomit.video_compressor.events", binaryMessenger: registrar.messenger())

		let instance = SwiftVideoCompressorPlugin()

		registrar.addMethodCallDelegate(instance, channel: channel)
		event.setStreamHandler(self)
	}

	public func handle(call: FlutterMethodCall, result: @escaping FlutterResult) {
		if call.method == "start" {
			if #available(iOS 11.0, *) {
				self?.startCompression(call: call, result: result)
			} else {
				result(FlutterError(code: "ios 11.0", message: "min iOS version : 11.0", details: nil))
			}
		} else if call.method == "cancel" {
			self?.cancelCompression(result: result)
		} else {
			result(FlutterMethodNotImplemented)
		}
	}

	func onListen(withArguments _: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
		eventSink = events
		return nil
	}

	func onCancel(withArguments _: Any?) -> FlutterError? {
		eventSink = nil
		return nil
	}

	private func getVideoQuality(quality: String) -> VideoQuality {
		switch quality {
		case "very_low":
			return VideoQuality.very_low
		case "low":
			return VideoQuality.low
		case "medium":
			return VideoQuality.medium
		case "high":
			return VideoQuality.high
		case "very_high":
			return VideoQuality.very_high
		default:
			return VideoQuality.medium
		}
	}

	@available(iOS 11.0, *)
	private func startCompression(call: FlutterMethodCall, result: @escaping FlutterResult) {
		let arguments = call.arguments as? [String: Any?]

		let srcPath: String? = arguments?["srcPath"] as? String
		let destPath: String? = arguments?["destPath"] as? String
		let isMinBitRateEnabled: Bool? = arguments?["isMinBitRateEnabled"] as? Bool
		let keepOriginalResolution: Bool? = arguments?["keepOriginalResolution"] as? Bool
		let videoQuality: String? = arguments?["videoQuality"] as? String

		if srcPath == nil || destPath == nil || isMinBitRateEnabled == nil || keepOriginalResolution == nil || videoQuality == nil {
			result(FlutterError(code: "arguments not found", message: "Missing some of required arguments", details: nil))
			return
		}

		let videoCompressor = VideoCompressor()

		compression = videoCompressor.compressVideo(
			source: URL(fileURLWithPath: srcPath!),
			destination: URL(fileURLWithPath: destPath!),
			quality: getVideoQuality(quality: videoQuality!),
			isMinBitRateEnabled: isMinBitRateEnabled!,
			keepOriginalResolution: keepOriginalResolution!,
			progressQueue: .main,
			progressHandler: { progress in
				DispatchQueue.main.async { [unowned self] in
					if self.eventSink != nil {
						self.eventSink!(Float(progress.fractionCompleted * 100))
					}
				}
			},
			completion: { compressionResult in
				switch compressionResult {
					case let .onSuccess(path):
						result(path.path)
					case .onStart: break
					case let .onFailure(error):
						result(nil)
					case .onCancelled:
						result(nil)
				}
			}
		)
	}

	private func cancelCompression(result: FlutterResult) {
		compression?.cancel = true
		result(nil)
	}
}
