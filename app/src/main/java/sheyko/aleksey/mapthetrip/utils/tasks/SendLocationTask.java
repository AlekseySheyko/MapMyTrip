package sheyko.aleksey.mapthetrip.utils.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.net.HttpURLConnection;

import sheyko.aleksey.mapthetrip.models.Device;

public class SendLocationTask extends AsyncTask<String, Void, Void> {
    public static final String TAG = SendLocationTask.class.getSimpleName();
    private Context mContext;

    public SendLocationTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader;
        Device mDevice = new Device(mContext);

        try {
                    String tripId = params[0];
                    String latitude = params[1];
                    String longitude = params[2];
                    String altitude = params[3];
                    String accuracy = params[4];

                    // Construct the URL for the first query
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("wsapp.mapthetrip.com")
                            .appendPath("TrucFuelLog.svc")
                            .appendPath("TFLRecordTripCoordinates")
                            .appendQueryParameter("TripId", tripId)
                            .appendQueryParameter("Latitute", latitude)
                            .appendQueryParameter("Longitude", longitude)
                            .appendQueryParameter("CoordinatesRecordDateTime", mDevice.getCurrentDateTime())
                            .appendQueryParameter("CoordinatesRecordTimezone", mDevice.getTimeZone())
                            .appendQueryParameter("CoordinatesIdStatesRegions", "")
                            .appendQueryParameter("CoordinatesStateRegionCode", "")
                            .appendQueryParameter("CoordinatesCountry", mDevice.getCoordinatesCountry())
                            .appendQueryParameter("UserId", mDevice.getUserId())
                            .appendQueryParameter("Altitude", altitude)
                            .appendQueryParameter("Accuracy", accuracy)
                    ;
                    String mUrlString = builder.build().toString();

                    Log.i(TAG, "Service: TFLRecordTripCoordinates,\n" +
                            "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

//                    URL mUrl = new URL(mUrlString);
//
//                    // Create the request and open the connection
//                    urlConnection = (HttpURLConnection) mUrl.openConnection();
//                    urlConnection.setRequestMethod("GET");
//                    urlConnection.connect();
//
//                    // Read the input stream into a String
//                    InputStream inputStream = urlConnection.getInputStream();
//                    StringBuilder buffer = new StringBuilder();
//                    reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        buffer.append(line);
//                    }
//
//                    Log.i(TAG, "Service: TFLRecordTripCoordinates,\n" +
//                            "Result: " + java.net.URLDecoder.decode(buffer.toString(), "UTF-8"));
//
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(TAG, "Error closing stream", e);
//                    }

                    // Construct the URL for the second query
                    builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("64.251.25.139")
                            .appendPath("trucks_app")
                            .appendPath("ws")
                            .appendPath("record-position.php")
                            .appendQueryParameter("lat", latitude)
                            .appendQueryParameter("lon", longitude)
                            .appendQueryParameter("alt", altitude)
                            .appendQueryParameter("id", tripId)
                            .appendQueryParameter("datetime", mDevice.getCurrentDateTime())
                            .appendQueryParameter("timezone", mDevice.getTimeZone())
                            .appendQueryParameter("accuracy", accuracy);
                    mUrlString = builder.build().toString();

                    Log.i(TAG, "Service: record-position.php,\n" +
                            "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

//                    mUrl = new URL(mUrlString);
//                    // Create the request and open the connection
//                    urlConnection = (HttpURLConnection) mUrl.openConnection();
//                    urlConnection.setRequestMethod("GET");
//                    urlConnection.connect();
//
//                    // Read the input stream into a String
//                    inputStream = urlConnection.getInputStream();
//                    buffer = new StringBuilder();
//                    reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                    while ((line = reader.readLine()) != null) {
//                        buffer.append(line);
//                    }
//
//                    Log.i(TAG, "Service: record-position.php,\n" +
//                            "Result: " + java.net.URLDecoder.decode(buffer.toString(), "UTF-8"));
//
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(TAG, "Error closing stream", e);
//                    }

        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
