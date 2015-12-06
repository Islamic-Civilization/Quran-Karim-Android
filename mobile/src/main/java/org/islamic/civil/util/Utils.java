package org.islamic.civil.util;

import java.lang.reflect.Array;

/**
 * Created by Amirhakh on 04/12/2015.
 */
public class Utils {
    // TODO add ...
    public static  <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

//        List<String> list = new ArrayList<String>(Arrays.asList(a));
//        list.addAll(Arrays.asList(b));
//        Object [] c = list.toArray();

        return c;
    }
}
