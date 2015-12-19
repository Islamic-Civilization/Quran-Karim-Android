package org.islamic.civil.quran_karim.data.model;

import java.util.Comparator;

/**
 * Created by Amirhakh on 11/12/2015.
 */
public class QuranSuraModel extends QuranAyaModel{
    public short order,ayas;
    public String name,tname;
    public boolean medinan;

    public QuranSuraModel(){}

    public QuranSuraModel(String name, String tname, short index){
        this.name = name;
        this.tname = tname;
        this.sura = index;
    }

    public static class NameComparator implements Comparator<QuranSuraModel>
    {
        public int compare(QuranSuraModel left, QuranSuraModel right) {
            return left.name.compareTo(right.name);
        }
    }
    public static class IndexComparator implements Comparator<QuranSuraModel>
    {
        public int compare(QuranSuraModel left, QuranSuraModel right) {
            return (left.sura-right.sura);
        }
    }
    public static class OrderComparator implements Comparator<QuranSuraModel>
    {
        public int compare(QuranSuraModel left, QuranSuraModel right) {
            return (left.order-right.order);
        }
    }
    public static class AyaComparator implements Comparator<QuranSuraModel>
    {
        public int compare(QuranSuraModel left, QuranSuraModel right) {
            return (left.ayas-right.ayas);
        }
    }
}