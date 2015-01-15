package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants;

public class Device {

    private Context mContext;

    public Device(Context context) {
        mContext = context;
    }

    public String getDeviceId() {
        return Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getDeviceType() {
        boolean tabletSize = mContext.getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            return "Tablet";
        } else {
            return "Phone";
        }
    }

    public String isCameraAvailable() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getModelName() {
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

    public String getModelNumber() {
        return Constants.Device.MODEL_NUMBER;
    }

    public String getSystemName() {
        return Constants.Device.SYSTEM_NAME;
    }

    public String getSoftwareVersion() {
        return Constants.Device.SOFTWARE_VERSION;
    }

    public String getDeviceOs() {
        return Constants.Device.SYSTEM_NAME;
    }

    public String getUserId() {
        return Constants.Device.USER_ID;
    }

    public String getUserDefinedTripId() {
        return Constants.Device.USER_DEFINED_TRIP_ID;
    }

    public String getTripReferenceNumber() {
        return Constants.Device.REFERENCE_NUMBER;
    }

    public String getEntityId() {
        return Constants.Device.ENTITY_ID;
    }

    public String getCoordinatesCountry() {
        return "US";
    }
}
