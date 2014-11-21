package utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StorageManager {

  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;

  /** Create a File for saving an image or video */
  public static File getOutputMediaFile(int type){
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    /** Saves in Pictures Directory */
//    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//        Environment.DIRECTORY_PICTURES), "LensBlur");

    /** Saves in root directory */
    File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString(), "LensBlur_Pictures");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
      if (! mediaStorageDir.mkdirs()){
        Logger.debug("failed to create directory");
        return null;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE){
      mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "IMG_"+ timeStamp + ".jpg");

      // Override save previous pics
//      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Pic.jpg");
    } else if(type == MEDIA_TYPE_VIDEO) {
      mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "VID_"+ timeStamp + ".mp4");
    } else {
      return null;
    }

    Logger.debug("mediaFile: " + mediaFile);

    return mediaFile;
  }

  public static void saveImage(Bitmap finalBitmap) {
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.
    File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString(), "LensBlurPictures");

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
      if (! mediaStorageDir.mkdirs()){
        Logger.debug("failed to create directory");
        return;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
    File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "IMG_"+ timeStamp + ".jpg");

    // Save the image
    try {
      FileOutputStream out = new FileOutputStream(mediaFile);
      finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
