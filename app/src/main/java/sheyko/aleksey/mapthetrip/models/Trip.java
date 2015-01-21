package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import sheyko.aleksey.mapthetrip.utils.helpers.MySingleton;
import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip implements Parcelable {

    private Context mContext;
    private String tripId;
    float distance = 0;
    int duration = 0;
    String startTime;
    String stateCodes;
    String stateDistances;
    String totalDistance;

    // Listens for location service
    private Intent mLocationIntent;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";
    private RequestQueue mRequestQueue;

    public Trip() {
    }

    public Trip(Parcel in) {
        readFromParcel(in);
    }

    public void start(Context context) {
        mContext = context;
        registerTrip();
    }

    public void resume() {
        updateStatus(RESUME);
        mContext.startService(mLocationIntent);
    }

    public void pause() {
        updateStatus(PAUSE);
        if (mLocationIntent != null) {
            mContext.stopService(mLocationIntent);
        }
    }

    public void finish() {
        updateStatus(FINISH);
        mContext.stopService(mLocationIntent);
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask(mContext).execute(tripId, status);
    }

    public String getId() {
        return tripId;
    }

    public String getDistance() {
        return String.format("%.1f", distance);
    }

    public void increazeDistance(float increment) {
        distance = distance + increment;
    }

    public int getDuration() {
        return duration;
    }

    public void incrementDuration(int increment) {
        duration = duration + increment;
    }

    public String getStartTime() {
        return startTime;
    }

    private void setStartTime() {
        startTime = new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase();
    }

    public static final Parcelable.Creator CREATOR = new Creator<Trip>() {
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tripId);
        dest.writeFloat(distance);
        dest.writeInt(duration);
        dest.writeString(startTime);
        dest.writeString(stateCodes);
        dest.writeString(stateDistances);
        dest.writeString(totalDistance);
    }

    private void readFromParcel(Parcel in) {
        tripId = in.readString();
        distance = in.readFloat();
        duration = in.readInt();
        startTime = in.readString();
        stateCodes = in.readString();
        stateDistances = in.readString();
        totalDistance = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    private void registerTrip() {
        Device mDevice = new Device(mContext);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("wsapp.mapthetrip.com")
                .appendPath("TrucFuelLog.svc")
                .appendPath("TFLRegDeviceandGetTripId")
                .appendQueryParameter("DeviceUID", mDevice.getDeviceId())
                .appendQueryParameter("DeviceName", mDevice.getModelName())
                .appendQueryParameter("DeviceType", mDevice.getDeviceType())
                .appendQueryParameter("DeviceManufacturerName", mDevice.getManufacturer())
                .appendQueryParameter("DeviceModelName", mDevice.getModelName())
                .appendQueryParameter("DeviceModelNumber", mDevice.getModelNumber())
                .appendQueryParameter("DeviceSystemName", mDevice.getSystemName())
                .appendQueryParameter("DeviceSystemVersion", mDevice.getAndroidVersion())
                .appendQueryParameter("DeviceSoftwareVersion", mDevice.getSoftwareVersion())
                .appendQueryParameter("DevicePlatformVersion", mDevice.getAndroidVersion())
                .appendQueryParameter("DeviceFirmwareVersion", mDevice.getAndroidVersion())
                .appendQueryParameter("DeviceOS", mDevice.getDeviceOs())
                .appendQueryParameter("DeviceTimezone", mDevice.getTimeZone())
                .appendQueryParameter("LanguageUsedOnDevice", mDevice.getLocale())
                .appendQueryParameter("HasCamera", mDevice.isCameraAvailable())
                .appendQueryParameter("UserId", mDevice.getUserId())
                .appendQueryParameter("TripDateTime", mDevice.getCurrentDateTime())
                .appendQueryParameter("TripTimezone", mDevice.getTimeZone())
                .appendQueryParameter("UserDefinedTripId", mDevice.getUserDefinedTripId())
                .appendQueryParameter("TripReferenceNumber", mDevice.getTripReferenceNumber())
                .appendQueryParameter("EntityId", mDevice.getEntityId());
        String url = builder.build().toString();

        RequestQueue queue = MySingleton.getInstance(mContext.getApplicationContext()).
                getRequestQueue();

        queue.add(new JsonObjectRequest(url, null,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonRoot) {
                        JSONObject mServerResponseObject;
                        try {
                            mServerResponseObject = jsonRoot.getJSONObject("TFLRegDeviceandGetTripIdResult");
                            String mParseStatus = mServerResponseObject.getString("Status");
                            if (mParseStatus.equals("Success")) {
                                tripId = mServerResponseObject.getString("TripId");
                                Log.i("TripId", "Trip ID: " + tripId);

                                setStartTime();
                                // Sends location to server
                                mLocationIntent = new Intent(mContext, LocationService.class);
                                mLocationIntent.putExtra("Trip ID", tripId);
                                mContext.startService(mLocationIntent);

                                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                        .putString("trip_id", tripId).apply();
                            }
                        } catch (JSONException e) {
                            Log.e("RegisterTrip", e.getMessage());
                        }
                    }
                }, null));
    }
}
