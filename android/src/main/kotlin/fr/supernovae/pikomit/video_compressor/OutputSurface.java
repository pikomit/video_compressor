package com.supernovae.pikomit.video_compressor;

import android.graphics.SurfaceTexture;
import android.view.Surface;

public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener {

  private SurfaceTexture mSurfaceTexture;
  private Surface mSurface;
  private final Object mFrameSyncObject = new Object();
  private boolean mFrameAvailable;
  private TextureRenderer mTextureRender;

  /**
   * Creates an OutputSurface using the current EGL context. This Surface will be
   * passed to MediaCodec.configure().
   */
  public OutputSurface() {
    setup();
  }

  /**
   * Creates instances of TextureRender and SurfaceTexture, and a Surface associated
   * with the SurfaceTexture.
   */
  private void setup() {
    mTextureRender = new TextureRenderer();
    mTextureRender.surfaceCreated();

    // Even if we don't access the SurfaceTexture after the constructor returns, we
    // still need to keep a reference to it. The Surface doesn't retain a reference
    // at the Java level, so if we don't either then the object can get GCed, which
    // causes the native finalizer to run.
    mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
    mSurfaceTexture.setOnFrameAvailableListener(this);
    mSurface = new Surface(mSurfaceTexture);
  }

  /**
   * Discards all resources held by this class, notably the EGL context.
   */
  public void release() {
    mSurface.release();

    mTextureRender = null;
    mSurface = null;
    mSurfaceTexture = null;
  }

  /**
   * Returns the Surface that we draw onto.
   */
  public Surface getSurface() {
    return mSurface;
  }

  /**
   * Latches the next buffer into the texture.  Must be called from the thread that created
   * the OutputSurface object, after the onFrameAvailable callback has signaled that new
   * data is available.
   */
  public void awaitNewImage() {
    final int TIMEOUT_MS = 500;

    synchronized (mFrameSyncObject) {
      while (!mFrameAvailable) {
        try {
          // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
          // stalling the test if it doesn't arrive.
          mFrameSyncObject.wait(TIMEOUT_MS);
          if (!mFrameAvailable) {
            throw new RuntimeException("Surface frame wait timed out");
          }
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
      }
      mFrameAvailable = false;
    }
    mTextureRender.checkGlError("before updateTexImage");
    mSurfaceTexture.updateTexImage();
  }

  /**
   * Draws the data from SurfaceTexture onto the current EGL surface.
   */
  public void drawImage() {
    mTextureRender.drawFrame(mSurfaceTexture);
  }

  @Override
  public void onFrameAvailable(SurfaceTexture st) {
    synchronized (mFrameSyncObject) {
      if (mFrameAvailable) {
        throw new RuntimeException(
          "mFrameAvailable already set, frame could be dropped"
        );
      }
      mFrameAvailable = true;
      mFrameSyncObject.notifyAll();
    }
  }
}
