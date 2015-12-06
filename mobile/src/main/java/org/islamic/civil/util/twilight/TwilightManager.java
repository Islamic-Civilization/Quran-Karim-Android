package org.islamic.civil.util.twilight;

import android.os.Handler;

public interface TwilightManager {
    void registerListener(TwilightListener listener, Handler handler);
    TwilightState getCurrentState();
}