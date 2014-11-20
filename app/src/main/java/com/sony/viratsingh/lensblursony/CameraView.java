package com.sony.viratsingh.lensblursony;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import utils.Logger;
import utils.StorageManager;

import static android.hardware.Camera.PictureCallback;
import static android.hardware.Camera.PreviewCallback;

/** A basic Camera preview class */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

  private SurfaceHolder surfaceHolder;
  private Camera camera;
  private int cameraDisplayOrientation = 0;
  private Camera.Parameters cameraParameters;
  private Activity activity;
  private AtomicInteger frame_count;

  public CameraView(Context context, Camera camera) {
    super(context);
    this.camera = camera;
    this.cameraParameters = camera.getParameters();
    this.activity = (CameraActivity) context;
    this.frame_count = new AtomicInteger(0);

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
    camera.takePicture(null, null, jpegPictureCallback);
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

  private PictureCallback jpegPictureCallback = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      Logger.debug("onPictureTaken called");

      new SavePictureTask().execute(data);
      restartPreview();
    }
  };

  private PreviewCallback previewCallback = new PreviewCallback() {
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
      Logger.debug("onPreviewFrame called");
      if (frame_count.get() % 20 == 0) {
        Logger.debug("onPreviewFrame new picture taken");
        new SavePictureTask().execute(bytes);
      }
      frame_count.incrementAndGet();
    }
  };

  private class SavePictureTask extends AsyncTask<byte[], Void, Void> {

    @Override
    protected Void doInBackground(byte[]... bytes) {
      final byte[] data = bytes[0];

      Camera.Size previewSize = camera.getParameters().getPreviewSize();
      //YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
      YuvImage yuvimage=new YuvImage(data, camera.getParameters().getPreviewFormat(), previewSize.width, previewSize.height, null);
      Logger.debug("PG: size & format " + previewSize.width + "x" + previewSize.height + " format:" + camera.getParameters().getPreviewFormat());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
      //byte[] jdata = baos.toByteArray();
      //Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
      Bitmap bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
      SaveImage(bmp);


//      File pictureFile = StorageManager.getOutputMediaFile(StorageManager.MEDIA_TYPE_IMAGE);
//      if (pictureFile == null){
//        Logger.debug("Error creating media file, check storage permissions");
//        return null;
//      }
//
//      try {
//        FileOutputStream fos = new FileOutputStream(pictureFile);
//        fos.write(data);
//        fos.flush();
//        fos.close();
//        restartPreview();
//      } catch (FileNotFoundException e) {
//        Logger.error( "File not found: " + e.getMessage());
//      } catch (IOException e) {
//        Logger.error("Error accessing file: " + e.getMessage());
//      }

      return null;
    }
  }

  private void SaveImage(Bitmap finalBitmap) {

    String root = Environment.getExternalStorageDirectory().toString();
    File myDir = new File(root + "/lensBlur_previewFrame_images");
    myDir.mkdirs();
    Random generator = new Random();
    int n = 10000;
    n = generator.nextInt(n);
    String fname = "Image-"+ n +".jpg";
    File file = new File (myDir, fname);
    if (file.exists ()) file.delete ();
    try {
      FileOutputStream out = new FileOutputStream(file);
      finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
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
