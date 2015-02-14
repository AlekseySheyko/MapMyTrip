package sheyko.aleksey.mapthetrip.utils.tasks;

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

public class SaveTripTask extends AsyncTask<List<ParseObject>, Void, Void> {

    public static final String TAG = SaveTripTask.class.getSimpleName();

    public SaveTripTask() {
    }

    @Override
    protected Void doInBackground(List<ParseObject>... saveTaskList) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            for (List<ParseObject> saveTasks : saveTaskList) {
                for (ParseObject saveTask : saveTasks) {
                    String id = saveTask.getString("trip_id");
                    String isSaved = saveTask.getString("is_saved");
                    String distance = saveTask.getString("total_distance");
                    String duration = saveTask.getString("duration");
                    String name = saveTask.getString("name");
                    String notes = saveTask.getString("notes");
                    String stateCodes = saveTask.getString("state_codes");
                    String stateDistances = saveTask.getString("state_distances");
                    String stateDurations = saveTask.getString("state_durations");

                    // Construct the URL for the query
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("wsapp.mapthetrip.com")
                            .appendPath("TrucFuelLog.svc")
                            .appendPath("TFLSaveTripandSummaryInfo")
                            .appendQueryParameter("TripId", id)
                            .appendQueryParameter("IsTripSaved", isSaved)
                            .appendQueryParameter("TotalDistanceTraveled", "" + distance)
                            .appendQueryParameter("TotalTripDuration", duration)
                            .appendQueryParameter("TripName", "" + name)
                            .appendQueryParameter("TripDesc", "" + notes)
                            .appendQueryParameter("TripNotes", "" + notes)
                            .appendQueryParameter("StateCd", stateCodes)
                            .appendQueryParameter("TotalStateDistanceTraveled", stateDistances)
                            .appendQueryParameter("Total_State_Trip_Duration", "" + stateDurations)
                            .appendQueryParameter("EntityId", "1")
                            .appendQueryParameter("UserId", "1");
                    String urlString = builder.build().toString();

                    Log.i(TAG, "Service: TFLSaveTripandSummaryInfo,\n" +
                            "Query: " + java.net.URLDecoder.decode(urlString, "UTF-8"));

                    URL url = new URL(urlString);

                    // Create the request and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
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
                    Log.i(TAG, "Service: TFLSaveTripandSummaryInfo,\n" +
                            "Result: " + response);

                    saveTask.unpinInBackground();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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