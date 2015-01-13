package sheyko.aleksey.mapthetrip.utils;

import android.content.Intent;
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

import sheyko.aleksey.mapthetrip.helpers.Constants;
import sheyko.aleksey.mapthetrip.models.DeviceInfo;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.services.SendLocationService;

public class RegisterDeviceTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = RegisterDeviceTask.class.getSimpleName();

    @Override
    protected String doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON response
        String resultJsonStr;
        DeviceInfo deviceInfo = new DeviceInfo();

        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("wsapp.mapthetrip.com")
                    .appendPath("TrucFuelLog.svc")
                    .appendPath("TFLRegDeviceandGetTripId")
                    .appendQueryParameter("DeviceUID", deviceInfo.getDeviceId())
                    .appendQueryParameter("DeviceName", deviceInfo.getModel())
                    .appendQueryParameter("DeviceType", deviceInfo.getDeviceType())
                    .appendQueryParameter("DeviceManufacturerName", deviceInfo.getManufacturer())
                    .appendQueryParameter("DeviceModelName", deviceInfo.getModel())
                    .appendQueryParameter("DeviceModelNumber", Constants.modelNumber)
                    .appendQueryParameter("DeviceSystemName", Constants.systemName)
                    .appendQueryParameter("DeviceSystemVersion", deviceInfo.getAndroidVersion())
                    .appendQueryParameter("DeviceSoftwareVersion", Constants.softwareVersion)
                    .appendQueryParameter("DevicePlatformVersion", deviceInfo.getAndroidVersion())
                    .appendQueryParameter("DeviceFirmwareVersion", deviceInfo.getAndroidVersion())
                    .appendQueryParameter("DeviceOS", Constants.systemName)
                    .appendQueryParameter("DeviceTimezone", deviceInfo.getTimeZone())
                    .appendQueryParameter("LanguageUsedOnDevice", deviceInfo.getLocale())
                    .appendQueryParameter("HasCamera", deviceInfo.isCameraAvailable())
                    .appendQueryParameter("UserId", Constants.userId)
                    .appendQueryParameter("TripDateTime", deviceInfo.getCurrentDateTime())
                    .appendQueryParameter("TripTimezone", deviceInfo.getTimeZone())
                    .appendQueryParameter("UserDefinedTripId", Constants.userDefinedTripId)
                    .appendQueryParameter("TripReferenceNumber", Constants.referenceNumber)
                    .appendQueryParameter("EntityId", Constants.entityId);

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

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            resultJsonStr = buffer.toString();

            Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
                    "Result: " + java.net.URLDecoder.decode(resultJsonStr, "UTF-8"));

            try {
                JSONObject regResultObject = new JSONObject(resultJsonStr);
                JSONObject regResponse = regResultObject.getJSONObject("TFLRegDeviceandGetTripIdResult");

                String mParseStatus = regResponse.getString("Status");
                if (mParseStatus.equals("Success")) {
                    mTripId = regResponse.getString("TripId");
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

        new Trip(mTripId + "").save();

        mLocationClient.connect();

        // Start service to send location
        getActivity().startService(new Intent(getActivity(), SendLocationService.class)
                .putExtra("action", "startTimer")
                .putExtra("tripId", mTripId));
    }
}
