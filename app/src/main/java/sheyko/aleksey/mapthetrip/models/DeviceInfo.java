package sheyko.aleksey.mapthetrip.models;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceInfo {

    public DeviceInfo() {
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }

    public String getLocale() {
        return Locale.getDefault().getDisplayName();
    }

    public String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
    }
}
