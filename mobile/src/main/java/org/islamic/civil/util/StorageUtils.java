package org.islamic.civil.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.islamic.civil.quran_karim.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Amirhakh on 04/12/2015.
 */
/**
 * Based on:
 * - http://sapienmobile.com/?p=204
 * - http://stackoverflow.com/a/15612964
 * - http://renzhi.ca/2012/02/03/how-to-list-all-sd-cards-on-android/
 */
public class StorageUtils {

    public static File[] getDataDirectory(Context context){
        File[] dirs = new File[]{context.getFilesDir()};
        if(isExternalStorageWritable())
            dirs = Utils.concatenate(dirs,ContextCompat.getExternalFilesDirs(context, null));
        return dirs;
    }

    public static File[] getCacheDirectory(Context context){
        File[] dirs = new File[]{context.getCacheDir()};
        if(isExternalStorageWritable())
            dirs = Utils.concatenate(dirs,ContextCompat.getExternalCacheDirs(context));
        return dirs;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static final String TAG = StorageUtils.class.getSimpleName();

    /**
     * @return A List of all storage locations available
     */
    public static List<Storage> getAllStorageLocations(Context context) {
        List<String> mounts = new ArrayList<>();

        final File[] mountPoints = ContextCompat.getExternalFilesDirs(context, null);
        if (mountPoints != null && mountPoints.length > 1) {
            for (File mountPoint : mountPoints) {
                mounts.add(mountPoint.getAbsolutePath());
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mounts = readMountsFile();

            // As per http://source.android.com/devices/tech/storage/config.html
            // device-specific vold.fstab file is removed after Android 4.2.2
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Set<String> volds = readVoldsFile();

                List<String> toRemove = new ArrayList<>();
                for (String mount : mounts) {
                    if (!volds.contains(mount)) {
                        toRemove.add(mount);
                    }
                }

                for (String s : toRemove) {
                    mounts.remove(s);
                }
            } else {
                Log.d(TAG, "Android version: " + Build.VERSION.SDK_INT + ", skip reading vold.fstab file");
            }
        }

        Log.d(TAG, "mounts list is: " + mounts);
        return buildMountsList(context, mounts);
    }

    private static List<Storage> buildMountsList(Context context, List<String> mounts) {
        List<Storage> list = new ArrayList<>(mounts.size());

        int externalSdcardsCount = 0;
        if (mounts.size() > 0) {
            // Follow Android SD Cards naming conventions
            if (!Environment.isExternalStorageRemovable() || Environment.isExternalStorageEmulated()) {
                list.add(new Storage(context.getString(R.string.prefs_sdcard_internal),
                        Environment.getExternalStorageDirectory().getAbsolutePath()));
            } else {
                externalSdcardsCount = 1;
                list.add(new Storage(context.getString(R.string.prefs_sdcard_external,
                        externalSdcardsCount), mounts.get(0)));
            }

            // All other mounts rather than the first mount point are considered as External SD Card
            if (mounts.size() > 1) {
                externalSdcardsCount++;
                for (int i = 1/*skip the first item*/; i < mounts.size(); i++) {
                    list.add(new Storage(context.getString(R.string.prefs_sdcard_external,
                            externalSdcardsCount++), mounts.get(i)));
                }
            }
        }

        Log.d(TAG, "final storage list is: " + list);
        return list;
    }

    private static List<String> readMountsFile() {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        List<String> mounts = new ArrayList<>();
        mounts.add(sdcardPath);

        Log.d(TAG, "reading mounts file begin");
        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists()) {
                Log.d(TAG, "mounts file exists");
                Scanner scanner = new Scanner(mountFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    Log.d(TAG, "line: " + line);
                    if (line.startsWith("/dev/block/vold/")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];
                        Log.d(TAG, "mount element is: " + element);
                        if (!sdcardPath.equals(element)) {
                            mounts.add(element);
                        }
                    } else {
                        Log.d(TAG, "skipping mount line: " + line);
                    }
                }
            } else {
                Log.d(TAG, "mounts file doesn't exist");
            }

            Log.d(TAG, "reading mounts file end.. list is: " + mounts);
        } catch (Exception e) {
            Log.e(TAG, "Error reading mounts file", e);
        }
        return mounts;
    }

    /**
     * Reads volume manager daemon file for auto-mounted storage.
     * Read more about it <a href="http://vold.sourceforge.net/">here</a>.
     *
     * Set usage, to safely avoid duplicates, is intentional.
     * @return Set of mount points from `vold.fstab` configuration file
     */
    private static Set<String> readVoldsFile() {
        Set<String> volds = new HashSet<>();
        volds.add(Environment.getExternalStorageDirectory().getAbsolutePath());

        Log.d(TAG, "reading volds file");
        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (voldFile.exists()) {
                Log.d(TAG, "reading volds file begin");
                Scanner scanner = new Scanner(voldFile);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    Log.d(TAG, "line: " + line);
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];
                        Log.d(TAG, "volds element is: " + element);

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                            Log.d(TAG, "volds element is: " + element);
                        }

                        Log.d(TAG, "adding volds element to list: " + element);
                        volds.add(element);
                    } else {
                        Log.d(TAG, "skipping volds line: " + line);
                    }
                }
            } else {
                Log.d(TAG, "volds file doesn't exit");
            }
            Log.d(TAG, "reading volds file end.. list is: " + volds);
        } catch (Exception e) {
            Log.e(TAG, "Error reading volds file", e);
        }

        return volds;
    }

    public static class Storage {
        private String label;
        private String mountPoint;
        private int freeSpace;

        public Storage(String label, String mountPoint) {
            this.label = label;
            this.mountPoint = mountPoint;
            computeSpace();
        }

        private void computeSpace() {
            StatFs stat = new StatFs(mountPoint);
            long bytesAvailable;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                bytesAvailable = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            } else {
                //noinspection deprecation
                bytesAvailable = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
            }
            // Convert total bytes to megabytes
            freeSpace = Math.round(bytesAvailable / (1024 * 1024));
        }

        public String getLabel() {
            return label;
        }

        public String getMountPoint() {
            return mountPoint;
        }

        /**
         * @return available free size in Megabytes
         */
        public int getFreeSpace() {
            return freeSpace;
        }
    }
}
