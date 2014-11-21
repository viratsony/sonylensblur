package com.sony.viratsingh.lensblursony;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import utils.Logger;
import utils.StorageManager;

import static android.hardware.Camera.PreviewCallback;

/** A basic Camera preview class */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

  private SurfaceHolder surfaceHolder;
  private Camera camera;
  private int cameraDisplayOrientation = 0;
  private Camera.Parameters cameraParameters;
  private final CameraActivity activity;
  private final AtomicInteger frame_count;
  private final AtomicInteger picture_count;

  private String picturesTaken = getResources().getString(R.string.picturesTaken);

  public CameraView(Context context, Camera camera) {
    super(context);
    this.activity = (CameraActivity) context;
    this.camera = camera;
    this.frame_count = new AtomicInteger(0);
    this.picture_count = new AtomicInteger(0);

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    surfaceHolder = getHolder();
    surfaceHolder.addCallback(this);
  }

  private void setCameraParameters(Camera camera) {
    cameraParameters = camera.getParameters();
    cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    cameraParameters.setRotation(180);
    camera.setParameters(cameraParameters);


    Logger.info("isAutoExposureLockSupported(): " + cameraParameters.isAutoExposureLockSupported());
    Logger.info("isAutoWhiteBalanceLockSupported(): " + cameraParameters.isAutoWhiteBalanceLockSupported());
    Logger.info("isVideoStabilizationSupported(): " + cameraParameters.isVideoStabilizationSupported());
    Logger.info("getMinExposureCompensation(): " + cameraParameters.getMinExposureCompensation());
    Logger.info("getMaxExposureCompensation(): " + cameraParameters.getMaxExposureCompensation());
    List<int[]> fps_range = cameraParameters.getSupportedPreviewFpsRange();
    for (int[] fps_pair : fps_range) {
      StringBuilder sb = new StringBuilder();
      for (int fps : fps_pair) {
        sb.append(fps + " ");
      }
      Logger.info("getSupportedPreviewFpsRange(): " + sb.toString());
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    try {
      camera.setPreviewDisplay(this.surfaceHolder);
      camera.startPreview();
    } catch (IOException e) {
      Logger.error("Error setting camera preview: " + e.getMessage());
    }

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.

    setCameraParameters(camera);

    Logger.debug("cameraDisplayOrientation = " + cameraDisplayOrientation);
    if (this.surfaceHolder.getSurface() == null){
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try {
      camera.stopPreview();
    } catch (Exception e){
    // ignore: tried to stop a non-existent preview
    }

    restartPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    // empty. Take care of releasing the Camera preview in CameraActivity.
  }

  private PreviewCallback previewCallback = new PreviewCallback() {
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
      Logger.debug("onPreviewFrame called");
      if (frame_count.get() % 30 == 0 && activity.isCaptureStarted()) {
        Logger.debug("onPreviewFrame new picture taken");
        new SavePictureTask().execute(bytes);
        activity.picturesTakenTextView.setText(picturesTaken + " " + picture_count.incrementAndGet());

        // TODO - Do we want to autoFocus before saving pic? If so, screen pulsates on autoFocus call
//        autoFocus(bytes);
      }
      frame_count.incrementAndGet();
    }
  };

  private class SavePictureTask extends AsyncTask<byte[], Void, Void> {

    @Override
    protected Void doInBackground(final byte[]... bytes) {
      byte[] data = bytes[0];
      try {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        YuvImage yuvimage = new YuvImage(data, camera.getParameters().getPreviewFormat(), previewSize.width, previewSize.height, null);
        Logger.debug("PG: size & format " + previewSize.width + "x" + previewSize.height + " format:" + camera.getParameters().getPreviewFormat());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);

        Bitmap bmp_unrotated = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());

        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        matrix.postScale(.5f, .5f);
        Bitmap bmp_rotated = Bitmap.createBitmap(bmp_unrotated, 0, 0, bmp_unrotated.getWidth(), bmp_unrotated.getHeight(), matrix, true);

        StorageManager.saveImage(bmp_rotated);
      } catch (RuntimeException e) {
        Logger.error("Error in AsyncTask CameraView: " + e);
      }

      return null;
    }
  }

  public void resetPictureCount() {
    picture_count.set(0);
    activity.picturesTakenTextView.setText(picturesTaken + " " + picture_count.get());
  }

  private void restartPreview() {
    if (camera != null) {
      try {
        camera.stopPreview();
        camera.setPreviewDisplay(this.surfaceHolder);
        setCameraPreviewSize();
        camera.setPreviewCallback(previewCallback);

        camera.startPreview();
      } catch (IOException e) {
        Logger.error("Error starting camera preview: " + e.getMessage());
      } catch (RuntimeException e) {
        Logger.error("Camera has already been released: " + e.getMessage());
      }
    }
  }

  private void setCameraPreviewSize() {
    // Get the supported preview sizes:
    Camera.Parameters parameters = camera.getParameters();
    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
    Camera.Size previewSize = previewSizes.get(0);
    // And set them:
    parameters.setPreviewSize(previewSize.width, previewSize.height);
    Logger.debug("previewSize.width: " + previewSize.width + " previewSize.height: " + previewSize.height);
    camera.setParameters(parameters);
  }

}
