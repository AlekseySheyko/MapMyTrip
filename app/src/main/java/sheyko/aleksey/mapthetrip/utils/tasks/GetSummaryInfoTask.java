package sheyko.aleksey.mapthetrip.utils.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetSummaryInfoTask extends AsyncTask<String, Void, Void> {

    public static final String TAG = GetSummaryInfoTask.class.getSimpleName();
    private boolean ENABLE_LOGGING = true;

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        String getSummaryInfoJsonResponse = null;

        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("wsgis.mapthetrip.com")
                    .appendPath("trucks_app")
                    .appendPath("ws")
                    .appendPath("get-distance.php")
                    .appendQueryParameter("truck_id", params[0]);
            String mUrlString = builder.build().toString();

            if (ENABLE_LOGGING)
            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
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

            getSummaryInfoJsonResponse = buffer.toString();

            if (ENABLE_LOGGING)
            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
                    "Result: " + java.net.URLDecoder.decode(getSummaryInfoJsonResponse, "UTF-8"));

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