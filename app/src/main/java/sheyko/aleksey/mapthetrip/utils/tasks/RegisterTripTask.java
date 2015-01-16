package sheyko.aleksey.mapthetrip.utils.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import sheyko.aleksey.mapthetrip.models.Device;

public class RegisterTripTask extends AsyncTask<String, Void, String> {
    private static final String TAG = RegisterTripTask.class.getSimpleName();

    protected OnTripRegistered mCallback;
    private Context mContext;

    // Interface to return trip ID
    public interface OnTripRegistered {
        public void onTripRegistered(Context context, String tripId);
    }

    public RegisterTripTask(Context context, OnTripRegistered callback){
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON response
        String resultJsonStr;

        String mTripId = null;
        Device mDevice = new Device(mContext);

        try {
            // Construct the URL for the query
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
            String mUrlString = builder.build().toString();

            Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
                    "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

            URL mUrl = new URL(mUrlString);
            // Create the request and open the connection
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            resultJsonStr = buffer.toString();

            Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
                    "Result: " + java.net.URLDecoder.decode(resultJsonStr, "UTF-8"));

            try {
                JSONObject mParseObject = new JSONObject(resultJsonStr);
                JSONObject mServerResponseObject = mParseObject.getJSONObject("TFLRegDeviceandGetTripIdResult");

                String mParseStatus = mServerResponseObject.getString("Status");
                if (mParseStatus.equals("Success")) {
                    mTripId = mServerResponseObject.getString("TripId");
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return mTripId;
    }

    @Override
    protected void onPostExecute(String mTripId) {
        super.onPostExecute(mTripId);

        mCallback.onTripRegistered(mContext, mTripId);
    }
}
