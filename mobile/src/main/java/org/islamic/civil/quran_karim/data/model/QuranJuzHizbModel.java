package org.islamic.civil.quran_karim.data.model;

/**
 * Created by Amirhakh on 11/12/2015.
 */
public class QuranJuzHizbModel extends QuranAyaModel {
    public short index;
    public String title = null;

    public boolean isJuz(){
        return (index)%8 == 1;
    }

    public boolean isHizb(){
        return (index)%4 == 1;
    }

    public short getJuz(){
        return (short) ((index-1)/8+1);
    }

    public short getHizb(){
        return (short) ((index-1)/4+1);
    }
}
