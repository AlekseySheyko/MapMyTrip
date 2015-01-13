package sheyko.aleksey.mapthetrip.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import sheyko.aleksey.mapthetrip.helpers.Constants.Device;
import sheyko.aleksey.mapthetrip.models.DeviceInfo;

public class UpdateTripStatusTask extends AsyncTask<String, Void, Void> {

    public static final String TAG = UpdateTripStatusTask.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        String updateTripJsonResponse = null;

        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("wsapp.mapthetrip.com")
                    .appendPath("TrucFuelLog.svc")
                    .appendPath("TFLUpdateTripStatus")
                    .appendQueryParameter("TripId", params[0])
                    .appendQueryParameter("TripStatus", params[1])
                    .appendQueryParameter("TripDateTime", new DeviceInfo().getCurrentDateTime())
                    .appendQueryParameter("TripTimezone", new DeviceInfo().getTimeZone())
                    .appendQueryParameter("UserId", Device.USER_ID);
            String mUrlString = builder.build().toString();

            Log.i(TAG, "Service: TFLUpdateTripStatus,\n" +
                    "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

            URL mUrl = new URL(mUrlString);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            updateTripJsonResponse = buffer.toString();
            Log.i(TAG, "Service: TFLUpdateTripStatus " + "(" + params[1] + " trip)" + ",\n" +
                    "Result: " + java.net.URLDecoder.decode(updateTripJsonResponse, "UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
        } finally{
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
        return null;
    }
}
