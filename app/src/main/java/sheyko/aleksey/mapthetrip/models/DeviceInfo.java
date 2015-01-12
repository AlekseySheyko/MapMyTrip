package sheyko.aleksey.mapthetrip.models;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import sheyko.aleksey.mapthetrip.R;

public class DeviceInfo extends Application {

    String id;
    String type;
    String manufacturer;
    String model;
    String androidVersion;
    String timezone;
    String language;
    String isCameraAvailable;
    String dateTime;

    public DeviceInfo() {
        id = getDeviceId();
        type = getDeviceType();
        manufacturer = getManufacturer();
        model = getModel();
        androidVersion = getAndroidVersion();
        timezone = getTimeZone();
        language = getLocale();
        isCameraAvailable = isCameraAvailable();
        dateTime = getCurrentDateTime();
    }

    public String getDeviceId() {
        return Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getDeviceType() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            return "Tablet";
        } else {
            return "Phone";
        }
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

    public String isCameraAvailable() {
        if (this.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
    }
}
