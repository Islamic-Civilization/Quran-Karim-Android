package org.islamic.civil.quran_karim;

import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class QuranActivity extends AppCompatActivity {
    static public final String Title = "title";
    static public final String StartSura = "sSura",SuraStart = StartSura,
            SuraIndex = "iSura", SuraAya = "aSura";
    static public final String StartPage = "sPage";
    static public final String StartAll = "sAll";
    static public final String OrderType = "tOrder";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        if(getIntent().getAction().equals(StartSura));
        setContentView(R.layout.activity_quran);
    }
}
