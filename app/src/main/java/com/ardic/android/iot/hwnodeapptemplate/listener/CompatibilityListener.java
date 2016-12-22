package com.ardic.android.iot.hwnodeapptemplate.listener;

import com.ardic.android.iotignite.exceptions.UnsupportedVersionException;

/**
 * Created by yavuz.erzurumlu on 30.11.2016.
 */

public interface CompatibilityListener {
    void onUnsupportedVersionExceptionReceived(UnsupportedVersionException exception);
}
