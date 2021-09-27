#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint video_compressor.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'video_compressor'
  s.version          = '1.0.0'
  s.summary          = 'Compress & Format videos (mp4)'
  s.description      = <<-DESC
	Compress & Format videos (mp4)
                       DESC
  s.homepage         = 'https://github.com/pikomit/video_compressor#readme'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Pikomit' => 'pikomit.inc@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '11.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
