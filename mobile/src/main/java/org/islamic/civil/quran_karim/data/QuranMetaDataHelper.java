package org.islamic.civil.quran_karim.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Amirhakh on 04/12/2015.
 */
public class QuranMetaDataHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "quranDataClean.db";
    private static final int DATABASE_VERSION = 1;

    public QuranMetaDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
