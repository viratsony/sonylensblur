package com.sony.viratsingh.lensblursony;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import utils.Logger;

import static android.view.View.*;


public class CameraActivity extends Activity {
  ///////////////////////////////////////////////////////////////////
  // CONSTANTS
  ///////////////////////////////////////////////////////////////////
  private static final int MAX_PICTURES = 3;


  ///////////////////////////////////////////////////////////////////
  // UI
  ///////////////////////////////////////////////////////////////////
  @InjectView(R.id.capture_button) Button captureButton;


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
    captureStarted = false;
  }

  @Override
  protected void onResume() {
    super.onResume();

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
        if (!captureStarted) {
          captureStarted = true;
          startPictureTaking();
        } else {
          captureStarted = false;
        }
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
    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    // Hide the status bar and other OS-level chrome
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

  }

  private void startPictureTaking() {
    final Timer timer = new Timer();
    final AtomicInteger pictures_taken = new AtomicInteger(0);

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (pictures_taken.get() < MAX_PICTURES) {
          cameraView.takePicture();
          pictures_taken.incrementAndGet();
        } else {
          timer.cancel();
          pictures_taken.set(0);
          captureStarted = false;
        }
      }
    }, 1000, 3000);
  }
}
