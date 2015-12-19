package org.islamic.civil.quran_karim.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.islamic.civil.quran_karim.R;
import org.islamic.civil.quran_karim.data.model.QuranAyaModel;
import org.islamic.civil.quran_karim.data.model.QuranJuzHizbModel;
import org.islamic.civil.quran_karim.data.model.QuranPageModel;
import org.islamic.civil.quran_karim.data.model.QuranSuraModel;

import java.util.ArrayList;

/**
 * Created by Amirhakh on 04/12/2015.
 */
public class QuranMetaDataHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "quranDataClean.db";
    private static final int DATABASE_VERSION = 1;

    static QuranMetaDataHelper instance = null;

    public static QuranMetaDataHelper getInstance(Context context){
        if(instance == null)
            instance = new QuranMetaDataHelper(context);
        return instance;
    }


    public QuranMetaDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }

    //------------------------------------------------------ convert data

    public short suraAyaToIndex(short sura,short aya){
        short index = -1;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT `index` FROM quran_text WHERE `sura` = ? AND `aya` =?;",
                new String[]{Short.toString(sura),Short.toString(aya)});
        if(c.moveToFirst())
            index = c.getShort(0);
        return index;
    }

    public QuranAyaModel indexToSuraAya(short index){
        QuranAyaModel model = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT `sura`, `aya` FROM quran_text WHERE `index` = ?",
                new String[]{Short.toString(index)});
        if(c.moveToFirst()){
            model = new QuranAyaModel();
            model.start = index;
            model.sura = c.getShort(0);
            model.aya = c.getShort(1);
        }
        db.close();
        return model;
    }

    public short suraAyaToPage(short sura,short aya){
        short page = -1;
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor c = db.rawQuery("SELECT `index` FROM quran_data_page WHERE `sura` <= ? AND `aya` <= ?; ",
//                new String[]{Short.toString(sura),Short.toString(aya)});
//        if(c.moveToFirst())
//            page = c.getShort(0);
        if (aya == 0) aya = 1;
        if ((sura < 1) || (sura > SurasCount)
                || (aya < AyaMin) ||
                (aya > AyaMax))
            return -1;

        // what page does the sura start on?
        page = (short) (SURA_PAGE_START[sura - 1] - 1);
        while (page < PagesLast){
            // what's the first sura in that page?
            int ss = PAGE_SURA_START[page];

            // if we've passed the sura, return the previous page
            // or, if we're at the same sura and passed the ayah
            if (ss > sura || ((ss == sura) &&
                    (PAGE_AYAH_START[page] > aya))) {
                break;
            }

            // otherwise, look at the next page
            page++;
        }
        return page;
    }

    public QuranAyaModel pageToSuraAya(short page){
        QuranPageModel model = new QuranPageModel();
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor c = db.rawQuery("SELECT `sura`, `aya` FROM quran_data_page WHERE `index` = ?",
//                new String[]{Short.toString(page)});
//        if(c.moveToFirst()){
//            model = new QuranAyaModel();
//            model.index = -1;//page;
//            model.sura = c.getShort(0);
//            model.aya = c.getShort(1);
//        }
//        db.close();
        model.start = -1;
        model.index = page;
        model.sura = PAGE_SURA_START[page];
        model.aya = PAGE_AYAH_START[page];
        return model;
    }

    //------------------------------------------------------ get model

    public ArrayList<QuranSuraModel> getSuraModel(){
        ArrayList<QuranSuraModel> mModels = new ArrayList<>();
        short i=0;
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT `start`, `ayas`, `order`, `name`,`tname`,`type` FROM quran_data_sura LIMIT "+SurasCount,null);
//            db.query("quran_data_sura",
//                    new String[]{"start", "ayas", "order", "name", "tname", "type"},
//                    "*", null, null, null, null);

            while(c.moveToNext()){
                if(c.getShort(1)<=1)
                    break;
                QuranSuraModel sura = new QuranSuraModel();
                sura.sura = ++i;
                sura.start = c.getShort(0);
                sura.ayas = c.getShort(1);
                sura.order = c.getShort(2);
                sura.name = c.getString(3);
                sura.tname = c.getString(4);
                sura.medinan = c.getInt(5) == 1;
                mModels.add(sura);
            }
//            c.close();
        } catch (Exception e){
            e.printStackTrace();
            mModels.clear();
        } finally {
            db.close();
        }

        return mModels;
    }

    public ArrayList<QuranJuzHizbModel> getJuzHizbModel(){
        ArrayList<QuranJuzHizbModel> models = new ArrayList<>(30*2*4);
        SQLiteDatabase db = getReadableDatabase();
        try{
            Cursor c = db.rawQuery("SELECT `index`, `sura`, `aya` FROM quran_data_hizb LIMIT "+HizbCount,null);
            while(c.moveToNext()){
                QuranJuzHizbModel hizb = new QuranJuzHizbModel();
                hizb.index = c.getShort(0);
                hizb.sura = c.getShort(1);
                hizb.aya = c.getShort(2);
                models.add(hizb);
            }
        } catch (Exception e){
            e.printStackTrace();
            models.clear();
        } finally {
            db.close();
        }

        return models;
    }

    //------------------------------------------------------ quran android data
    public static final int PagesFirst = 1;
    public static final int PagesLast = 604;
    public static final int PAGES_LAST_DUAL = PagesLast / 2;
    public static final int SURA_FIRST = 1;
    public static final int SURA_LAST = 114;
    public static final int SurasCount = 114;
    public static final int JuzCount = 30;
    public static final int HizbCount = 240;
    public static final int AyaMin = 1;
    public static final int AyaMax = 286;

    public static short[] SURA_PAGE_START = {
            1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262,
            267, 282, 293, 305, 312, 322, 332, 342, 350, 359, 367, 377, 385, 396,
            404, 411, 415, 418, 428, 434, 440, 446, 453, 458, 467, 477, 483, 489,
            496, 499, 502, 507, 511, 515, 518, 520, 523, 526, 528, 531, 534, 537,
            542, 545, 549, 551, 553, 554, 556, 558, 560, 562, 564, 566, 568, 570,
            572, 574, 575, 577, 578, 580, 582, 583, 585, 586, 587, 587, 589, 590,
            591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597, 598, 598, 599,
            599, 600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604, 604,
            604
    };

    public static short[] SURA_NUM_AYAHS = {
            7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111,
            43, 52, 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77,
            227, 93, 88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75,
            85, 54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55,
            78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52,
            44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25,
            22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
            11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
    };

    public static boolean[] SURA_IS_MAKKI = {
            // 1 - 10
            true, false, false, false, false, true, true, false, false, true,
            // 11 - 20
            true, true, false, true, true, true, true, true, true, true,
            // 21 - 30
            true, false, true, false, true, true, true, true, true, true,
            // 31 - 40
            true, true, false, true, true, true, true, true, true, true,
            // 41 - 50
            true, true, true, true, true, true, false, false, false, true,
            // 51 - 60
            true, true, true, true, false, true, false, false, false, false,
            // 61 - 70
            false, false, false, false, false, false, true, true, true, true,
            // 71 - 80
            true, true, true, true, true, false, true, true, true, true,
            // 81 - 90
            true, true, true, true, true, true, true, true, true, true,
            // 91 - 100
            true, true, true, true, true, true, true, true, false, true,
            // 101 - 110
            true, true, true, true, true, true, true, true, true, false,
            // 111 - 114
            true, true, true, true
    };

    public static short[] PAGE_SURA_START = {
            1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9,
            9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
            11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13,
            13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16,
            16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17,
            17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18,
            19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,
            21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22,
            22, 23, 23, 23, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24,
            24, 25, 25, 25, 25, 25, 25, 25, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
            27, 27, 27, 27, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 28, 28, 28, 28,
            28, 28, 29, 29, 29, 29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 30, 31, 31,
            31, 31, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34, 34, 34,
            34, 34, 34, 34, 35, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37, 37,
            37, 37, 37, 37, 38, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39, 39, 39, 39,
            39, 40, 40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41, 41, 41, 42, 42,
            42, 42, 42, 42, 42, 43, 43, 43, 43, 43, 43, 44, 44, 44, 45, 45, 45, 45,
            46, 46, 46, 46, 47, 47, 47, 47, 48, 48, 48, 48, 48, 49, 49, 50, 50, 50,
            51, 51, 51, 52, 52, 53, 53, 53, 54, 54, 54, 55, 55, 55, 56, 56, 56, 57,
            57, 57, 57, 58, 58, 58, 58, 59, 59, 59, 60, 60, 60, 61, 62, 62, 63, 64,
            64, 65, 65, 66, 66, 67, 67, 67, 68, 68, 69, 69, 70, 70, 71, 72, 72, 73,
            73, 74, 74, 75, 76, 76, 77, 78, 78, 79, 80, 81, 82, 83, 83, 85, 86, 87,
            89, 89, 91, 92, 95, 97, 98, 100, 103, 106, 109, 112
    };

    public static short[] PAGE_AYAH_START = {
            1, 1, 6, 17, 25, 30, 38, 49, 58, 62, 70, 77, 84, 89, 94, 102, 106, 113,
            120, 127, 135, 142, 146, 154, 164, 170, 177, 182, 187, 191, 197, 203,
            211, 216, 220, 225, 231, 234, 238, 246, 249, 253, 257, 260, 265, 270,
            275, 282, 283, 1, 10, 16, 23, 30, 38, 46, 53, 62, 71, 78, 84, 92, 101,
            109, 116, 122, 133, 141, 149, 154, 158, 166, 174, 181, 187, 195, 1, 7,
            12, 15, 20, 24, 27, 34, 38, 45, 52, 60, 66, 75, 80, 87, 92, 95, 102,
            106, 114, 122, 128, 135, 141, 148, 155, 163, 171, 176, 3, 6, 10, 14,
            18, 24, 32, 37, 42, 46, 51, 58, 65, 71, 77, 83, 90, 96, 104, 109, 114,
            1, 9, 19, 28, 36, 45, 53, 60, 69, 74, 82, 91, 95, 102, 111, 119, 125,
            132, 138, 143, 147, 152, 158, 1, 12, 23, 31, 38, 44, 52, 58, 68, 74,
            82, 88, 96, 105, 121, 131, 138, 144, 150, 156, 160, 164, 171, 179, 188,
            196, 1, 9, 17, 26, 34, 41, 46, 53, 62, 70, 1, 7, 14, 21, 27, 32, 37,
            41, 48, 55, 62, 69, 73, 80, 87, 94, 100, 107, 112, 118, 123, 1, 7, 15,
            21, 26, 34, 43, 54, 62, 71, 79, 89, 98, 107, 6, 13, 20, 29, 38, 46, 54,
            63, 72, 82, 89, 98, 109, 118, 5, 15, 23, 31, 38, 44, 53, 64, 70, 79,
            87, 96, 104, 1, 6, 14, 19, 29, 35, 43, 6, 11, 19, 25, 34, 43, 1, 16,
            32, 52, 71, 91, 7, 15, 27, 35, 43, 55, 65, 73, 80, 88, 94, 103, 111,
            119, 1, 8, 18, 28, 39, 50, 59, 67, 76, 87, 97, 105, 5, 16, 21, 28, 35,
            46, 54, 62, 75, 84, 98, 1, 12, 26, 39, 52, 65, 77, 96, 13, 38, 52, 65,
            77, 88, 99, 114, 126, 1, 11, 25, 36, 45, 58, 73, 82, 91, 102, 1, 6,
            16, 24, 31, 39, 47, 56, 65, 73, 1, 18, 28, 43, 60, 75, 90, 105, 1,
            11, 21, 28, 32, 37, 44, 54, 59, 62, 3, 12, 21, 33, 44, 56, 68, 1, 20,
            40, 61, 84, 112, 137, 160, 184, 207, 1, 14, 23, 36, 45, 56, 64, 77,
            89, 6, 14, 22, 29, 36, 44, 51, 60, 71, 78, 85, 7, 15, 24, 31, 39, 46,
            53, 64, 6, 16, 25, 33, 42, 51, 1, 12, 20, 29, 1, 12, 21, 1, 7, 16, 23,
            31, 36, 44, 51, 55, 63, 1, 8, 15, 23, 32, 40, 49, 4, 12, 19, 31, 39,
            45, 13, 28, 41, 55, 71, 1, 25, 52, 77, 103, 127, 154, 1, 17, 27, 43,
            62, 84, 6, 11, 22, 32, 41, 48, 57, 68, 75, 8, 17, 26, 34, 41, 50, 59,
            67, 78, 1, 12, 21, 30, 39, 47, 1, 11, 16, 23, 32, 45, 52, 11, 23, 34,
            48, 61, 74, 1, 19, 40, 1, 14, 23, 33, 6, 15, 21, 29, 1, 12, 20, 30, 1,
            10, 16, 24, 29, 5, 12, 1, 16, 36, 7, 31, 52, 15, 32, 1, 27, 45, 7, 28,
            50, 17, 41, 68, 17, 51, 77, 4, 12, 19, 25, 1, 7, 12, 22, 4, 10, 17, 1,
            6, 12, 6, 1, 9, 5, 1, 10, 1, 6, 1, 8, 1, 13, 27, 16, 43, 9, 35, 11, 40,
            11, 1, 14, 1, 20, 18, 48, 20, 6, 26, 20, 1, 31, 16, 1, 1, 1, 7, 35, 1,
            1, 16, 1, 24, 1, 15, 1, 1, 8, 10, 1, 1, 1, 1
    };

    public static short[] JUZ_PAGE_START = {
            1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
            201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
            402, 422, 442, 462, 482, 502, 522, 542, 562, 582
    };

    public static short[] RUB3_PAGE_START = {
            5, 7, 9, 11, 14, 17, 19, 22, 24,
            27, 29, 32, 34, 37, 39, 42, 44, 46, 49, 51, 54, 56, 59, 62, 64, 67,
            69, 72, 74, 77, 79, 82, 84, 87, 89, 92, 94, 97, 100, 102, 104, 106,
            109, 112, 114, 117, 119, 121, 124, 126, 129, 132, 134, 137, 140,
            142, 144, 146, 148, 151, 154, 156, 158, 162, 164, 167, 170, 173,
            175, 177, 179, 182, 184, 187, 189, 192, 194, 196, 199, 201, 204,
            206, 209, 212, 214, 217, 219, 222, 224, 226, 228, 231, 233, 236,
            238, 242, 244, 247, 249, 252, 254, 256, 259, 262, 264, 267, 270,
            272, 275, 277, 280, 282, 284, 287, 289, 292, 295, 297, 299, 302,
            304, 306, 309, 312, 315, 317, 319, 322, 324, 326, 329, 332, 334,
            336, 339, 342, 344, 347, 350, 352, 354, 356, 359, 362, 364, 367,
            369, 371, 374, 377, 379, 382, 384, 386, 389, 392, 394, 396, 399,
            402, 404, 407, 410, 413, 415, 418, 420, 422, 425, 426, 429, 431,
            433, 436, 439, 442, 444, 446, 449, 451, 454, 456, 459, 462, 464,
            467, 469, 472, 474, 477, 479, 482, 484, 486, 488, 491, 493, 496,
            499, 502, 505, 507, 510, 513, 515, 517, 519, 522, 524, 526, 529,
            531, 534, 536, 539, 542, 544, 547, 550, 553, 554, 558, 560, 562,
            564, 566, 569, 572, 575, 577, 579, 582, 585, 587, 589, 591, 594,
            596, 599
    };

    public static short[] PAGE_RUB3_START = {
            -1, -1, -1, -1, 1, -1, 2, -1, 3, -1, 4, -1, -1, 5, -1, -1, 6, -1, 7,
            -1, -1, 8, -1, 9, -1, -1, 10, -1, 11, -1, -1, 12, -1, 13, -1, -1, 14,
            -1, 15, -1, -1, 16, -1, 17, -1, 18, -1, -1, 19, -1, 20, -1, -1, 21, -1,
            22, -1, -1, 23, -1, -1, 24, -1, 25, -1, -1, 26, -1, 27, -1, -1, 28, -1,
            29, -1, -1, 30, -1, 31, -1, -1, 32, -1, 33, -1, -1, 34, -1, 35, -1, -1,
            36, -1, 37, -1, -1, 38, -1, -1, 39, -1, 40, -1, 41, -1, 42, -1, -1, 43,
            -1, -1, 44, -1, 45, -1, -1, 46, -1, 47, -1, 48, -1, -1, 49, -1, 50, -1,
            -1, 51, -1, -1, 52, -1, 53, -1, -1, 54, -1, -1, 55, -1, 56, -1, 57, -1,
            58, -1, 59, -1, -1, 60, -1, -1, 61, -1, 62, -1, 63, -1, -1, -1, 64, -1,
            65, -1, -1, 66, -1, -1, 67, -1, -1, 68, -1, 69, -1, 70, -1, 71, -1, -1,
            72, -1, 73, -1, -1, 74, -1, 75, -1, -1, 76, -1, 77, -1, 78, -1, -1, 79,
            -1, 80, -1, -1, 81, -1, 82, -1, -1, 83, -1, -1, 84, -1, 85, -1, -1, 86,
            -1, 87, -1, -1, 88, -1, 89, -1, 90, -1, 91, -1, -1, 92, -1, 93, -1, -1,
            94, -1, 95, -1, -1, -1, 96, -1, 97, -1, -1, 98, -1, 99, -1, -1, 100, -1,
            101, -1, 102, -1, -1, 103, -1, -1, 104, -1, 105, -1, -1, 106, -1, -1,
            107, -1, 108, -1, -1, 109, -1, 110, -1, -1, 111, -1, 112, -1, 113, -1,
            -1, 114, -1, 115, -1, -1, 116, -1, -1, 117, -1, 118, -1, 119, -1, -1,
            120, -1, 121, -1, 122, -1, -1, 123, -1, -1, 124, -1, -1, 125, -1, 126,
            -1, 127, -1, -1, 128, -1, 129, -1, 130, -1, -1, 131, -1, -1, 132, -1,
            133, -1, 134, -1, -1, 135, -1, -1, 136, -1, 137, -1, -1, 138, -1, -1,
            139, -1, 140, -1, 141, -1, 142, -1, -1, 143, -1, -1, 144, -1, 145, -1,
            -1, 146, -1, 147, -1, 148, -1, -1, 149, -1, -1, 150, -1, 151, -1, -1,
            152, -1, 153, -1, 154, -1, -1, 155, -1, -1, 156, -1, 157, -1, 158, -1,
            -1, 159, -1, -1, 160, -1, 161, -1, -1, 162, -1, -1, 163, -1, -1, 164,
            -1, 165, -1, -1, 166, -1, 167, -1, 168, -1, -1, 169, 170, -1, -1, 171,
            -1, 172, -1, 173, -1, -1, 174, -1, -1, 175, -1, -1, 176, -1, 177, -1,
            178, -1, -1, 179, -1, 180, -1, -1, 181, -1, 182, -1, -1, 183, -1, -1,
            184, -1, 185, -1, -1, 186, -1, 187, -1, -1, 188, -1, 189, -1, -1, 190,
            -1, 191, -1, -1, 192, -1, 193, -1, 194, -1, 195, -1, -1, 196, -1, 197,
            -1, -1, 198, -1, -1, 199, -1, -1, 200, -1, -1, 201, -1, 202, -1, -1,
            203, -1, -1, 204, -1, 205, -1, 206, -1, 207, -1, -1, 208, -1, 209, -1,
            210, -1, -1, 211, -1, 212, -1, -1, 213, -1, 214, -1, -1, 215, -1, -1,
            216, -1, 217, -1, -1, 218, -1, -1, 219, -1, -1, 220, 221, -1, -1, -1,
            222, -1, 223, -1, 224, -1, 225, -1, 226, -1, -1, 227, -1, -1, 228, -1,
            -1, 229, -1, 230, -1, 231, -1, -1, 232, -1, -1, 233, -1, 234, -1, 235,
            -1, 236, -1, -1, 237, -1, 238, -1, -1, 239, -1, -1, -1, -1, -1
    };

    public static short[][] QUARTERS = new short[][]{
            // hizb 1
            {1, 1}, {2, 26}, {2, 44}, {2, 60},
            // hizb 2
            {2, 75}, {2, 92}, {2, 106}, {2, 124},
            // hizb 3
            {2, 142}, {2, 158}, {2, 177}, {2, 189},
            // hizb 4
            {2, 203}, {2, 219}, {2, 233}, {2, 243},
            // hizb 5
            {2, 253}, {2, 263}, {2, 272}, {2, 283},
            // hizb 6
            {3, 15}, {3, 33}, {3, 52}, {3, 75},
            // hizb 7
            {3, 93}, {3, 113}, {3, 133}, {3, 153},
            // hizb 8
            {3, 171}, {3, 186}, {4, 1}, {4, 12},
            // hizb 9
            {4, 24}, {4, 36}, {4, 58}, {4, 74},
            // hizb 10
            {4, 88}, {4, 100}, {4, 114}, {4, 135},
            // hizb 11
            {4, 148}, {4, 163}, {5, 1}, {5, 12},
            // hizb 12
            {5, 27}, {5, 41}, {5, 51}, {5, 67},
            // hizb 13
            {5, 82}, {5, 97}, {5, 109}, {6, 13},
            // hizb 14
            {6, 36}, {6, 59}, {6, 74}, {6, 95},
            // hizb 15
            {6, 111}, {6, 127}, {6, 141}, {6, 151},
            // hizb 16
            {7, 1}, {7, 31}, {7, 47}, {7, 65},
            // hizb 17
            {7, 88}, {7, 117}, {7, 142}, {7, 156},
            // hizb 18
            {7, 171}, {7, 189}, {8, 1}, {8, 22},
            // hizb 19
            {8, 41}, {8, 61}, {9, 1}, {9, 19},
            // hizb 20
            {9, 34}, {9, 46}, {9, 60}, {9, 75},
            // hizb 21
            {9, 93}, {9, 111}, {9, 122}, {10, 11},
            // hizb 22
            {10, 26}, {10, 53}, {10, 71}, {10, 90},
            // hizb 23
            {11, 6}, {11, 24}, {11, 41}, {11, 61},
            // hizb 24
            {11, 84}, {11, 108}, {12, 7}, {12, 30},
            // hizb 25
            {12, 53}, {12, 77}, {12, 101}, {13, 5},
            // hizb 26
            {13, 19}, {13, 35}, {14, 10}, {14, 28},
            // hizb 27
            {15, 1}, {15, 49}, {16, 1}, {16, 30},
            // hizb 28
            {16, 51}, {16, 75}, {16, 90}, {16, 111},
            // hizb 29
            {17, 1}, {17, 23}, {17, 50}, {17, 70},
            // hizb 30
            {17, 99}, {18, 17}, {18, 32}, {18, 51},
            // hizb 31
            {18, 75}, {18, 99}, {19, 22}, {19, 59},
            // hizb 32
            {20, 1}, {20, 55}, {20, 83}, {20, 111},
            // hizb 33
            {21, 1}, {21, 29}, {21, 51}, {21, 83},
            // hizb 34
            {22, 1}, {22, 19}, {22, 38}, {22, 60},
            // hizb 35
            {23, 1}, {23, 36}, {23, 75}, {24, 1},
            // hizb 36
            {24, 21}, {24, 35}, {24, 53}, {25, 1},
            // hizb 37
            {25, 21}, {25, 53}, {26, 1}, {26, 52},
            // hizb 38
            {26, 111}, {26, 181}, {27, 1}, {27, 27},
            // hizb 39
            {27, 56}, {27, 82}, {28, 12}, {28, 29},
            // hizb 40
            {28, 51}, {28, 76}, {29, 1}, {29, 26},
            // hizb 41
            {29, 46}, {30, 1}, {30, 31}, {30, 54},
            // hizb 42
            {31, 22}, {32, 11}, {33, 1}, {33, 18},
            // hizb 43
            {33, 31}, {33, 51}, {33, 60}, {34, 10},
            // hizb 44
            {34, 24}, {34, 46}, {35, 15}, {35, 41},
            // hizb 45
            {36, 28}, {36, 60}, {37, 22}, {37, 83},
            // hizb 46
            {37, 145}, {38, 21}, {38, 52}, {39, 8},
            // hizb 47
            {39, 32}, {39, 53}, {40, 1}, {40, 21},
            // hizb 48
            {40, 41}, {40, 66}, {41, 9}, {41, 25},
            // hizb 49
            {41, 47}, {42, 13}, {42, 27}, {42, 51},
            // hizb 50
            {43, 24}, {43, 57}, {44, 17}, {45, 12},
            // hizb 51
            {46, 1}, {46, 21}, {47, 10}, {47, 33},
            // hizb 52
            {48, 18}, {49, 1}, {49, 14}, {50, 27},
            // hizb 53
            {51, 31}, {52, 24}, {53, 26}, {54, 9},
            // hizb 54
            {55, 1}, {56, 1}, {56, 75}, {57, 16},
            // hizb 55
            {58, 1}, {58, 14}, {59, 11}, {60, 7},
            // hizb 56
            {62, 1}, {63, 4}, {65, 1}, {66, 1},
            // hizb 57
            {67, 1}, {68, 1}, {69, 1}, {70, 19},
            // hizb 58
            {72, 1}, {73, 20}, {75, 1}, {76, 19},
            // hizb 59
            {78, 1}, {80, 1}, {82, 1}, {84, 1},
            // hizb 60
            {87, 1}, {90, 1}, {94, 1}, {100, 9},
    };
}
