package sheyko.aleksey.mapthetrip.utils.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GetSummaryInfoTask extends AsyncTask<String, Void, HashMap<String, String>> {
    public static final String TAG = GetSummaryInfoTask.class.getSimpleName();

    protected OnSummaryDataRetrieved mCallback;

    // Interface to return states data
    public interface OnSummaryDataRetrieved {
        public void onSummaryDataRetrieved(
                String stateCodes, String stateDistances,
                String totalDistance, String stateDurations);
    }

    public GetSummaryInfoTask(OnSummaryDataRetrieved callback) {
        mCallback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... params) {

        String stateCodes = "";
        String distances = "";
        String stateDurations = "";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        HashMap<String, String> statesData = new HashMap<>();

        try {
            // Construct the URL for the query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("64.251.25.139")
                    .appendPath("trucks_app")
                    .appendPath("ws")
                    .appendPath("get-distance.php")
                    .appendQueryParameter("truck_id", params[0]);
            String mUrlString = builder.build().toString();

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
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            try {
                JSONObject responseObject = new JSONObject(buffer.toString());
                String mQueryStatus = responseObject
                        .getJSONObject("status").getString("code");
                if (mQueryStatus.equals("OK")) {
                    JSONObject mDataObject = responseObject.getJSONObject("data");
                    JSONObject mStateDistances = mDataObject.getJSONObject("distance");
                    Iterator<?> keys = mStateDistances.keys();
                    List<String> keyList = new ArrayList<>();
                    while (keys.hasNext()) {
                        String state = (String) keys.next();
                        keyList.add(state);

                        if (!state.equals("total"))
                        if (stateDurations.equals("")) {
                            stateDurations = stateDurations + "0";
                        } else {
                            stateDurations = stateDurations + ", " + "0";
                        }

                        if (stateCodes.equals("")) {
                            stateCodes = stateCodes + state;
                        } else {
                            stateCodes = stateCodes + "," + state;
                        }
                    }

                    for (String key : keyList) {
                        if (!key.equals("total")) {
                            String distance = mStateDistances.getDouble(key) + "";
                            if (distances.equals("")) {
                                distances = distances + distance;
                            } else {
                                distances = distances + ", " + distance;
                            }
                        }
                    }
                    stateCodes = stateCodes.replace("total,", "");
                    String totalDistance = mStateDistances.getDouble("total") + "";
                    if (stateDurations.equals("")) stateDurations = "0";

                    statesData.put("stateCodes", stateCodes);
                    statesData.put("stateDistances", distances);
                    statesData.put("totalDistance", totalDistance);
                    statesData.put("stateDurations", stateDurations);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
                    "Result: " + java.net.URLDecoder.decode(buffer.toString(), "UTF-8"));

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
        return statesData;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> mStatesInfo) {
        super.onPostExecute(mStatesInfo);

        mCallback.onSummaryDataRetrieved(
                mStatesInfo.get("stateCodes"),
                mStatesInfo.get("stateDistances"),
                mStatesInfo.get("totalDistance"),
                mStatesInfo.get("stateDurations"));
    }
}