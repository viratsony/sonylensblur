// Copyright (C) 2013 Sony Mobile Communications Inc.
// All rights, including trade secret rights, reserved.
package utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author christopherperry
 */
public class Logger {
  private static final String LOG_TAG = "Info-eye";
  private static final String FORMAT_LOG_MESSAGE = "[%s]: %s";
  private static final List<Tree> FOREST = new ArrayList<Tree>();

  public static void plant(Tree tree) {
    FOREST.add(tree);
  }

  public static void clearForest() {
    FOREST.clear();
  }

  public interface Tree {
    void verbose(String tag, String message);

    void info(String tag, String message);

    void debug(String tag, String message);

    void warn(String tag, String message);

    void error(String tag, String message);

    void stackTrace(String tag, String message);
  }

  public static class AndroidTree implements Tree {
    @Override
    public void verbose(String tag, String message) {
      Log.v(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }

    @Override
    public void info(String tag, String message) {
      Log.i(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }

    @Override
    public void debug(String tag, String message) {
      Log.d(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }

    @Override
    public void warn(String tag, String message) {
      Log.w(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }

    @Override
    public void error(String tag, String message) {
      Log.e(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }

    @Override
    public void stackTrace(String tag, String message) {
      Log.e(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }
  }

  public static class AndroidErrorTree extends HollowTree {
    @Override
    public void error(String tag, String message) {
      Log.e(logtagWithThreadId(), String.format(FORMAT_LOG_MESSAGE, tag, message));
    }
  }

  ////////////////////////////////////////////////////////
  // Auto tagged
  ////////////////////////////////////////////////////////

  public static void verbose(String message, Object... args) {
    String tag = createTag();
    message = formatString(message, args);

    for (Tree tree : FOREST) {
      tree.verbose(tag, message);
    }
  }

  public static void debug(String message, Object... args) {
    String tag = createTag();
    message = formatString(message, args);

    for (Tree tree : FOREST) {
      tree.debug(tag, message);
    }
  }

  public static void info(String message, Object... args) {
    String tag = createTag();
    message = formatString(message, args);

    for (Tree tree : FOREST) {
      tree.info(tag, message);
    }
  }

  public static void warn(String message, Object... args) {
    String tag = createTag();
    message = formatString(message, args);

    for (Tree tree : FOREST) {
      tree.warn(tag, message);
    }
  }

  public static void error(String message, Object... args) {
    String tag = createTag();
    message = formatString(message, args);

    for (Tree tree : FOREST) {
      tree.error(tag, message);
    }
  }

  public static void stackTrace() {
    String tag = createTag();
    String message = getStackTrace();
    for (Tree tree : FOREST) {
      tree.stackTrace(tag, message);
    }
  }

  static String getStackTrace() {
    StringBuilder sb = new StringBuilder();
    StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    for (StackTraceElement element : stackTrace) {
      sb.append(element.toString()).append("\n");
    }
    return sb.toString();
  }

  static String formatString(String message, Object... args) {
    return args.length == 0 ? message : String.format(message, args);
  }

  private static String createTag() {
    String className = new Throwable().getStackTrace()[2].getClassName();
    return className.substring(className.lastIndexOf('.') + 1);
  }

  private static String logtagWithThreadId() {
    return LOG_TAG + " {" + Thread.currentThread().getId() + "}";
  }

  // Useful for extending
  static class HollowTree implements Tree {
    @Override
    public void verbose(String tag, String message) { }

    @Override
    public void info(String tag, String message) { }

    @Override
    public void debug(String tag, String message) { }

    @Override
    public void warn(String tag, String message) { }

    @Override
    public void error(String tag, String message) { }

    @Override
    public void stackTrace(String tag, String message) { }
  }
}
