# features/media/

Audio/video demux + decode for the in-page player. Recommended: a **minimal
FFmpeg** build (enable only needed demuxers/decoders) plus **dav1d** for AV1.
Expose demux/seek/frame calls via JNI; render frames to a Surface.
