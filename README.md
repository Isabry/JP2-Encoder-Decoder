# JP2-Encoder-Decoder

## Introduction
**JPEG 2000 (JP2)** is an image compression standard and coding system. It was developed from 1997 to 2000 by a Joint Photographic Experts Group committee chaired by Touradj Ebrahimi (later the JPEG president),[1] with the intention of superseding their original JPEG standard (created in 1992), which is based on a discrete cosine transform (DCT), with a newly designed, wavelet-based method. The standardized filename extension is .jp2 for ISO/IEC 15444-1 conforming files and .jpx for the extended part-2 specifications, published as ISO/IEC 15444-2. The MIME types for JPEG 2000 are defined in RFC 3745.[2] The MIME type for JPEG 2000 (ISO/IEC 15444-1) is image/jp2.

## Source
An open-source JPEG-2000 image encoder/decoder for Android based on [OpenJPEG](http://www.openjpeg.org/) v2.4.0.

## Basic Usage
Encoding an image:
```kotlin
val portrait: InputStream = context.assets.open("portrait.png")
val bitmap: Bitmap = BitmapFactory.decodeStream(portrait)

// lossless encode
val jp2Data: ByteArray = JP2Encoder(bitmap).encode()

// lossy encode (target PSNR = 50dB)
val jp2Data: ByteArray = JP2Encoder(bmp)
                            .setVisualQuality(50)
                            .encode()
```

Decoding an image:
```kotlin
val bitmap: Bitmap = JP2Decoder(jp2data).decode()
imgView.setImageBitmap(bitmap)
```


## Advanced Usage
### Multiple Resolutions
A single JPEG-2000 image can contain multiple resolutions.
The final resolution is always equal to `<image_width> x <image_height>`; each additional resolution reduces the width and height by the factor of two. If you  don't need the full resolution, you can save memory and decoding time by decoding at a lower resolution.

#### Encoding
Default number of resolutions is 6, but you can specify your own value:
```kotlin
val jp2Data: ByteArray = JP2Encoder(bitmap)
                            .setNumResolutions(3)
                            .encode()
```
The number of resolutions must be between 1 and 32 and both image width and height must be greater or equal to `2^(numResolutions - 1)`.

#### Decoding
You can obtain the number of resolutions (as well as some other information  about the image) by calling the `readHeader()` method:
```kotlin
val header: Header = JP2Decoder(jp2data).readHeader()
val numResolutions = header.numResolutions
```
If you don't need the full resolution image, you can skip one or more resolutions during the decoding process.
```kotlin
val reducedBmp: Bitmap = JP2Decoder(jp2data)
                            .setSkipResolutions(2)
                            .decode()
```


### Multiple Quality Layers
Multiple layers can be encoded in a JPEG-2000 image, each having a different visual quality. If you don't need maximum visual quality, you can save  decoding time by skipping the higher-quality layers.


#### Encoding
Quality layers can be specified in two ways: as a list of compression ratios  or visual qualities. The **compression ratios** are specified as factors of compression, i.e. 20 means the size will be 20 times less than the raw uncompressed size.  Compression ratio 1 means lossless compression. Example:
```kotlin
//specify 3 quality layers with compression ratios 1:50, 1:20, and lossless.
val jp2Data: ByteArray = JP2Encoder(bitmap)
                            .setCompressionRatio(50, 10, 1)
                            .encode()
```

**Visual quality** is specified as [PSNR](https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio) values in dB. Usable values are roughly between 20 (very aggressive compression) and 70 (almost lossless). Special value 0 indicates lossless compression. Example:
```kotlin
//specify 3 quality layers with PSNR 30dB, 50dB, and lossless.
val jp2Data: ByteArray = JP2Encoder(bitmap)
                            .setVisualQuality(30, 50, 0)
                            .encode()
```

`setCompressionRatio()` and `setVisualQuality()` can not be used at the same time.


#### Decoding
You can obtain the number of available quality layers by calling the `readHeader()` method:
```kotlin

val header: Header = JP2Decoder(jp2data).readHeader()
val numQualityLayers = header.numQualityLayers
```

If you don't need a maximum quality image, you can trade some visual quality for a shorter decoding time by not decoding all the quality layers.
```kotlin

Bitmap lowQualityBmp = JP2Decoder(jp2data)      
                            .setLayersToDecode(2)
                            .decode()
```

### File Format
`JP2Encoder` supports two output formats:
* JP2 - standard JPEG-2000 file format (encapsulating a JPEG-2000 codestream)
* J2K - unencapsulated JPEG-2000 codestream

JP2 is the default output format, but it can be changed:
```kotlin
val jp2kData: ByteArray = JP2Encoder(bitmap)
                            .setOutputFormat(FORMAT_J2K)
                            .encode()
```

## Set up
Add dependency to your `build.gradle`:
```groovy
implementation("fr.sabry.jp2:jp2:1.1.1")
```
