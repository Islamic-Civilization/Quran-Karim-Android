package org.islamic.civil.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {
  public static final int BUFFER_SIZE = 4096;
  public static final String TAG = "ZipUtils";

  public static <T> boolean unzipFile(String zipFile,
      String destDirectory, T item, ZipListener<T> listener){
    try {
      File file = new File(zipFile);
      ZipFile zip = new ZipFile(file, ZipFile.OPEN_READ);
      int numberOfFiles = zip.size();
      Enumeration<? extends ZipEntry> entries = zip.entries();

      int processedFiles = 0;
      while (entries.hasMoreElements()) {
        processedFiles++;
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          File f = new File(destDirectory, entry.getName());
          if (!f.exists()){
            f.mkdirs();
          }
          continue;
        }

        // delete files that already exist
        File f = new File(destDirectory, entry.getName());
        if (f.exists()){
          f.delete();
        }

        InputStream is = zip.getInputStream(entry);
        FileOutputStream ostream = new FileOutputStream(f);

        int size;
        byte[] buf = new byte[BUFFER_SIZE];
        while ((size = is.read(buf)) > 0)
          ostream.write(buf, 0, size);
        is.close();
        ostream.close();

        if (listener != null) {
          listener.onProcessingProgress(item, processedFiles, numberOfFiles);
        }
      }

      zip.close();
      file.delete();
      return true;
    }
    catch (IOException ioe) {
      Log.e(TAG, "Error unzipping file: ", ioe);
      return false;
    }
  }

  public static <T> boolean unzipFile(File compressed,File raw,
                                      T item, ZipListener<T> listener){
    try {
      int fileSize = (int) compressed.length(),processed = 0;
      InputStream in =
              new InflaterInputStream(new FileInputStream(compressed));
      OutputStream out = new FileOutputStream(raw);
      byte[] buffer = new byte[BUFFER_SIZE];
      int len;
      while((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
        processed += len;
        if (listener != null) {
          listener.onProcessingProgress(item, processed, fileSize);
        }
      }
      in.close();
      out.close();
      return true;
    } catch (IOException ioe){
      Log.e(TAG, "Error unzipping file: ", ioe);
      return false;
    }
  }

  /**
   * Compresses a file with zlib compression.
   */
  public static void compressFile(File raw, File compressed)
          throws IOException
  {
    InputStream in = new FileInputStream(raw);
    OutputStream out =
            new DeflaterOutputStream(new FileOutputStream(compressed));
    shovelInToOut(in, out);
    in.close();
    out.close();
  }

  /**
   * Decompresses a zlib compressed file.
   */
  public static void decompressFile(File compressed, File raw)
          throws IOException
  {
    InputStream in =
            new InflaterInputStream(new FileInputStream(compressed));
    OutputStream out = new FileOutputStream(raw);
    shovelInToOut(in, out);
    in.close();
    out.close();
  }

  /**
   * Shovels all data from an input stream to an output stream.
   */
  private static void shovelInToOut(InputStream in, OutputStream out)
          throws IOException
  {
    byte[] buffer = new byte[1000];
    int len;
    while((len = in.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  public interface ZipListener<T> {
    void onProcessingProgress(T obj, int processed, int total);
  }
}
