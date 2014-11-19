package com.sony.viratsingh.lensblursony;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import utils.Logger;

import static android.view.View.*;


public class CameraActivity extends Activity {

  ///////////////////////////////////////////////////////////////////
  // UI
  ///////////////////////////////////////////////////////////////////
  @InjectView(R.id.capture_button) Button captureButton;


  ///////////////////////////////////////////////////////////////////
  // FIELDS
  ///////////////////////////////////////////////////////////////////
  private Camera camera;
  private CameraView cameraView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    ButterKnife.inject(this);

    Logger.plant(new Logger.AndroidTree());
  }

  @Override
  protected void onResume() {
    super.onResume();

    makeFullScreen();

    // Create an instance of Camera
    if (hasCameraHardware()) {
      camera = getCameraInstance();
    }

    // Create Preview view and set it as content of CameraActivity
    cameraView = new CameraView(this, camera);
//    cameraView.setUIOrientation(getRequestedOrientation());

    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

    preview.addView(cameraView);

    captureButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        cameraView.takePicture();
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera();
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

  private void restartCamera() {
    // TODO - restart the camera
  }

  private void makeFullScreen() {
    // Hide notication bar
    if (Build.VERSION.SDK_INT < 16) {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
    } else {
      getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Hide Action Bar
    getActionBar().hide();
  }
}
