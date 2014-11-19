package com.sony.viratsingh.lensblursony;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import utils.Logger;

import static android.hardware.Camera.PictureCallback;

/** A basic Camera preview class */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

  private SurfaceHolder surfaceHolder;
  private Camera camera;
  private int cameraDisplayOrientation = 0;
  private Camera.Parameters cameraParameters;
  private Activity activity;

  public CameraView(Context context, Camera camera) {
    super(context);
    this.camera = camera;
    this.cameraParameters = camera.getParameters();
    this.activity = (CameraActivity) context;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    surfaceHolder = getHolder();
    surfaceHolder.addCallback(this);
  }

  private void setCameraParameters(Camera camera) {
    cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    camera.setParameters(cameraParameters);
  }

  public void takePicture() {
    camera.takePicture(null, null, pictureCallback);
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

    // set preview size and make any resize, rotate or
    // reformatting changes here


    // start preview with new settings
    try {
      camera.setPreviewDisplay(this.surfaceHolder);
      camera.startPreview();

    } catch (Exception e){
      Logger.debug("Error starting camera preview: " + e.getMessage());
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    // empty. Take care of releasing the Camera preview in CameraActivity.

  }

  private PictureCallback pictureCallback = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

      File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
      if (pictureFile == null){
        Logger.debug("Error creating media file, check storage permissions");
        return;
      }

      try {
        FileOutputStream fos = new FileOutputStream(pictureFile);
        fos.write(data);
        fos.close();
      } catch (FileNotFoundException e) {
        Logger.error( "File not found: " + e.getMessage());
      } catch (IOException e) {
        Logger.error("Error accessing file: " + e.getMessage());
      }
    }
  };





//  /**
//   * Set the orientation of the UI.
//   *
//   * The orientation value should be one of the enumerated screen orientations
//   * from ActivityInfo, such as SCREEN_ORIENTATION_LANDSCAPE, etc.
//   *
//   * @param orientation   The current orientation of the UI.
//   */
//  public void setUIOrientation(int orientation) {
//    switch (orientation) {
//      case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//        // camera preview doesn't need rotation
//        cameraDisplayOrientation = 0;
//        return;
//      case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//        // camera preview must rotate 90 degrees
//        cameraDisplayOrientation = 90;
//        return;
//      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
//        // camera preview must rotate 180 degrees
//        cameraDisplayOrientation = 180;
//        return;
//      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
//        // camera preview must rotate 270 degrees
//        cameraDisplayOrientation = 270;
//        return;
//      default:
//        // default to no orientation on bad value
//        cameraDisplayOrientation = 0;
//    }
//    Logger.debug("cameraDisplayOrientation = " + cameraDisplayOrientation);
//  }

//  public Point getRealDisplaySize() {
//    Point size = new Point();
//    activity.getWindowManager().getDefaultDisplay().getRealSize(size);
//    return size;
//  }

//  public static void setCameraDisplayOrientation(Activity activity,
//                                                 int cameraId, android.hardware.Camera camera) {
//    android.hardware.Camera.CameraInfo info =
//        new android.hardware.Camera.CameraInfo();
//    android.hardware.Camera.getCameraInfo(cameraId, info);
//    int rotation = activity.getWindowManager().getDefaultDisplay()
//        .getRotation();
//    int degrees = 0;
//    switch (rotation) {
//      case Surface.ROTATION_0: degrees = 0; break;
//      case Surface.ROTATION_90: degrees = 90; break;
//      case Surface.ROTATION_180: degrees = 180; break;
//      case Surface.ROTATION_270: degrees = 270; break;
//    }
//
//    int result;
//    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//      result = (info.orientation + degrees) % 360;
//      result = (360 - result) % 360;  // compensate the mirror
//    } else {  // back-facing
//      result = (info.orientation - degrees + 360) % 360;
//    }
//    camera.setDisplayOrientation(result);
//  }
//  @Override
//  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    Logger.debug("onMeasure");
//    try {
//      Point sourceSize = (cameraDisplayOrientation % 180 == 0) ? new Point(previewSize.height, previewSize.width)
//          : new Point(previewSize.width, previewSize.height);
//      Point screenSize = getRealDisplaySize();
//      Point destSize = ScalingHelper.centerCrop(sourceSize, screenSize);
//
//      Logger.debug("onMeasure, setting CameraView size: {" + destSize.x + ", " + destSize.y + "}");
//      setMeasuredDimension(destSize.x, destSize.y);
//    } catch (Exception e) {//this may happen when the camera is not obtained
//      setMeasuredDimension(0, 0);
//      Logger.debug("Error in onMeasure(): " + e);
//    }
//  }
//
//  private void startPreview() {
//    if (camera != null) {
//      try {
//        surfaceHolder = getHolder();
//        surfaceHolder.addCallback(this);
//        camera.setPreviewDisplay(surfaceHolder);
//        camera.setDisplayOrientation(cameraDisplayOrientation);
//        camera.startPreview();
//      } catch (Exception e) {
//        // Seen when method called after release().
//        Logger.debug("Exception starting Preview: " + e);
//        recoverCamera();
//      }
//    }
//  }
//  private void recoverCamera() {
//    stopCamera();
//  }
//
//  /**
//   * Restart the camera after it has been stopped.
//   */
//  public void restartCamera() {
//    Logger.verbose("restartCamera: ");
//    camera = cameraHelper.getCameraInstance(cameraId);
//    if (camera != null) {
//      initCameraParameters();
//      // update the preview, this also restarts it
//      restartPreview();
//    }
//  }
//  /**
//   * Stop using the camera. Call this when the parent activity
//   * is paused, to ensure that other apps can use the camera.
//   */
//  public void stopCamera() {
//    Logger.verbose("stopCamera: ");
//    // release
//    releaseCamera();
//    releaseSurface();
//  }
//
//  private void releaseCamera() {
//    if (camera != null) {
//      try {
//        camera.release();
//      } catch (Exception e) {
//        Logger.debug("Exception releasing camera: " + e);
//      }
//      camera = null; //make mCamera null even if there is exception in release.
//    }
//  }
//  private void releaseSurface() {
//    if (surfaceHolder != null) {
//      surfaceHolder.removeCallback(this);
//      surfaceHolder = null;
//    }
//  }
}
