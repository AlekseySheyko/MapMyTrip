package sheyko.aleksey.mapthetrip.utils.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

public class GetSummaryInfoTask extends AsyncTask<String, Void, HashMap<String, String>> {
    public static final String TAG = GetSummaryInfoTask.class.getSimpleName();

    protected OnStatesDataRetrieved mCallback;

    private String mStateCodes = "";
    private String mDistances = "";

    // Interface to return states data
    public interface OnStatesDataRetrieved {
        public void onStatesDataRetrieved(String stateCodes, String stateDistances, String totalDistance, String statesCount);
    }

    public GetSummaryInfoTask(OnStatesDataRetrieved callback) {
        mCallback = callback;
    }

    @Override
    protected HashMap<String, String> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        String resultJsonStr = null;
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

//            URL mUrl = new URL(mUrlString);
//
//            // Create the request and open the connection
//            urlConnection = (HttpURLConnection) mUrl.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                // Nothing to do.
//                return null;
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                // But it does make debugging a *lot* easier if you print out the completed
//                // buffer for debugging.
//                buffer.append(line + "\n");
//            }
//
//            resultJsonStr = buffer.toString();
//
//            try {
//                JSONObject mResponseObject = new JSONObject(resultJsonStr);
//                String mQueryStatus = mResponseObject.getJSONObject("status").getString("code");
//                if (mQueryStatus.equals("OK")) {
//                    JSONObject mDataObject = mResponseObject.getJSONObject("data");
//                    JSONObject mStateDistances = mDataObject.getJSONObject("distance");
//
//                    Iterator<?> keys = mStateDistances.keys();
//
//                    List<String> keyList = new ArrayList<>();
//
//                    int statesCount = 0;
//                    while (keys.hasNext()) {
//                        String state = (String) keys.next();
//                        keyList.add(state);
//                        statesCount++;
//
//                        if (mStateCodes.equals("")) {
//                            mStateCodes = mStateCodes + state;
//                        } else {
//                            mStateCodes = mStateCodes + ", " + state;
//                        }
//                    }
//
//                    for (String key : keyList) {
//                        if (!key.equals("total")) {
//
//                            String distance = mStateDistances.getDouble(key) + "";
//
//                            if (mDistances.equals("")) {
//                                mDistances = mDistances + distance;
//                            } else {
//                                mDistances = mDistances + ", " + distance;
//                            }
//                        }
//                    }
//
//                    mStateCodes = mStateCodes.replace("total, ", "");
//                    String totalDistance = mStateDistances.getDouble("total") + "";
//
//                    statesData.put("stateCodes", mStateCodes);
//                    statesData.put("stateDistances", mDistances);
//                    statesData.put("totalDistance", totalDistance);
//                    statesData.put("statesCount", statesCount + "");
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, e.getMessage());
//            }
//
//            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
//                    "Result: " + java.net.URLDecoder.decode(resultJsonStr, "UTF-8"));

        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
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

        mCallback.onStatesDataRetrieved(
                mStatesInfo.get("stateCodes"),
                mStatesInfo.get("stateDistances"),
                mStatesInfo.get("totalDistance"),
                mStatesInfo.get("statesCount"));
    }
}