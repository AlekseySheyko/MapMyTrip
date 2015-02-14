package sheyko.aleksey.mapthetrip.utils.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import sheyko.aleksey.mapthetrip.models.Device;

public class SendStatusTask extends AsyncTask<List<ParseObject>, Void, Void> {

    public static final String TAG = SendStatusTask.class.getSimpleName();
    private Context mContext;

    public SendStatusTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(List<ParseObject>... statusesList) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        Device device = new Device(mContext);

        try {
            for (List<ParseObject> statuses : statusesList) {
                for (ParseObject statusUpdate : statuses) {
                    String id = statusUpdate.getString("trip_id");
                    String status = statusUpdate.getString("status");
                    String datetime = statusUpdate.getString("datetime");

                    // Construct the URL for the query
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("wsapp.mapthetrip.com")
                            .appendPath("TrucFuelLog.svc")
                            .appendPath("TFLUpdateTripStatus")
                            .appendQueryParameter("TripId", id)
                            .appendQueryParameter("TripStatus", status)
                            .appendQueryParameter("TripDateTime", datetime)
                            .appendQueryParameter("TripTimezone", device.getTimeZone())
                            .appendQueryParameter("UserId", device.getUserId());
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
                    StringBuilder buffer = new StringBuilder();
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
                        buffer.append(line).append("\n");
                    }
                    String response = java.net.URLDecoder.decode(
                            buffer.toString(), "UTF-8");
                    Log.i(TAG, "Service: TFLUpdateTripStatus " + "(" + status + " trip)" + ",\n" +
                            "Result: " + response);

                    statusUpdate.unpinInBackground();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return null;
    }
}
