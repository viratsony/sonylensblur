package com.sony.viratsingh.lensblursony;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import utils.Logger;

import static android.view.View.*;

public class CameraActivity extends Activity {

  ///////////////////////////////////////////////////////////////////
  // UI
  ///////////////////////////////////////////////////////////////////
  @InjectView(R.id.capture_button) Button captureButton;
  @InjectView(R.id.pictures_taken_textView) TextView picturesTakenTextView;

  ///////////////////////////////////////////////////////////////////
  // FIELDS
  ///////////////////////////////////////////////////////////////////
  private Camera camera;
  private CameraView cameraView;

  private boolean captureStarted;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    makeFullScreen();
    setContentView(R.layout.activity_camera);

    ButterKnife.inject(this);

    Logger.plant(new Logger.AndroidTree());

    createCamera();

    captureButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!captureStarted) {
          if (cameraView != null) {
            cameraView.resetPictureCount();
          }
          captureStarted = true;
          captureButton.setText("Stop");
          Toast.makeText(CameraActivity.this, "capture started", Toast.LENGTH_SHORT).show();
        } else {
          captureStarted = false;

          captureButton.setText("Capture");
          Toast.makeText(CameraActivity.this, "capture stopped", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (camera == null) {
      createCamera();
    }
  }

  private void createCamera() {
    // Create an instance of Camera
    if (hasCameraHardware()) {
      camera = getCameraInstance();
    }
    createCameraView();
  }

  private void createCameraView() {
    // Create Preview view and set it as content of CameraActivity
    cameraView = new CameraView(this, camera);

    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(cameraView);
  }

  @Override
  protected void onPause() {
    super.onPause();
    resetCaptureMode();
    releaseCamera();
  }

  private void resetCaptureMode() {
    captureStarted = false;
    captureButton.setText("Capture");
    if (cameraView != null) {
      cameraView.resetPictureCount();
    }
  }

  /** Check if this device has a camera */
  private boolean hasCameraHardware() {
    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
      // this device has a camera
      return true;
    } else {
      // no camera on this device
      return false;
    }
  }

  /** A safe way to get an instance of the Camera object. */
  public static Camera getCameraInstance(){
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }

  private void releaseCamera() {
    if (camera != null) {
      camera.stopPreview();
      camera.setPreviewCallback(null);
      cameraView.getHolder().removeCallback(cameraView);
      camera.release();
      camera = null;
    }
  }

  private void makeFullScreen() {
    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    // Hide the status bar and other OS-level chrome
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

  }

  public boolean isCaptureStarted() {
    return captureStarted;
  }
}
