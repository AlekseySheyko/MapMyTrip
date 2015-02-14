package sheyko.aleksey.mapthetrip.utils.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import sheyko.aleksey.mapthetrip.models.Device;

public class SendCoordinatesTask extends AsyncTask<List<ParseObject>, Void, Void> {
    public static final String TAG = SendCoordinatesTask.class.getSimpleName();

    private Context mContext;

    protected OnLocationSent mCallback;

    public interface OnLocationSent {
        public void onLocationSent();
    }

    public SendCoordinatesTask(Context context, OnLocationSent callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(List<ParseObject>... coordinatesList) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader;
        Device mDevice = new Device(mContext);

        try {
            for (List<ParseObject> coordinates : coordinatesList) {
                for (ParseObject coordinateSet : coordinates) {
                    String tripId = coordinateSet.getString("trip_id");
                    String latitude = coordinateSet.getString("latitude");
                    String longitude = coordinateSet.getString("longitude");
                    String datetime = coordinateSet.getString("datetime");
                    String altitude = coordinateSet.getString("altitude");
                    String accuracy = coordinateSet.getString("accuracy");

                    // Construct the URL for the first query
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("wsapp.mapthetrip.com")
                            .appendPath("TrucFuelLog.svc")
                            .appendPath("TFLRecordTripCoordinates")
                            .appendQueryParameter("TripId", tripId)
                            .appendQueryParameter("Latitute", latitude)
                            .appendQueryParameter("Longitude", longitude)
                            .appendQueryParameter("CoordinatesRecordDateTime", datetime)
                            .appendQueryParameter("CoordinatesRecordTimezone", mDevice.getTimeZone())
                            .appendQueryParameter("CoordinatesIdStatesRegions", "")
                            .appendQueryParameter("CoordinatesStateRegionCode", "")
                            .appendQueryParameter("CoordinatesCountry", mDevice.getCoordinatesCountry())
                            .appendQueryParameter("UserId", mDevice.getUserId())
                            .appendQueryParameter("Altitude", altitude)
                            .appendQueryParameter("Accuracy", accuracy);
                    String mUrlString = builder.build().toString();

                    Log.i(TAG, "Service: TFLRecordTripCoordinates,\n" +
                            "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

                    URL mUrl = new URL(mUrlString);

                    // Create the request and open the connection
                    urlConnection = (HttpURLConnection) mUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    Log.i(TAG, "Service: TFLRecordTripCoordinates,\n" +
                            "Result: " + java.net.URLDecoder.decode(buffer.toString(), "UTF-8"));

                    // urlConnection.disconnect();
                    try {
                        reader.close();
                    } catch (final Exception e) {
                        Log.e(TAG, "Error closing stream", e);
                    }

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
                            .appendQueryParameter("datetime", datetime)
                            .appendQueryParameter("timezone", mDevice.getTimeZone())
                            .appendQueryParameter("accuracy", accuracy);
                    mUrlString = builder.build().toString();

                    Log.i(TAG, "Service: record-position.php,\n" +
                            "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

                    mUrl = new URL(mUrlString);
                    // Create the request and open the connection
                    urlConnection = (HttpURLConnection) mUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    inputStream = urlConnection.getInputStream();
                    buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String response = java.net.URLDecoder.decode(
                            buffer.toString(), "UTF-8");
                    Log.i(TAG, "Service: record-position.php,\n" +
                            "Result: " + response);

                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing stream", e);
                    }

                    coordinateSet.deleteInBackground();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        if (mCallback != null) {
            mCallback.onLocationSent();
        }
    }
}
