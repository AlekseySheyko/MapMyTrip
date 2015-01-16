package sheyko.aleksey.mapthetrip.utils.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GetSummaryInfoTask extends AsyncTask<String, Void, HashMap<String, String>> {

    public static final String TAG = GetSummaryInfoTask.class.getSimpleName();

    private String mStateCodes = "";
    private String mDistances = "";
    private String mTotalDistance = "";

    private HashMap<String, String> mStatesInfo;

    @Override
    protected HashMap<String, String> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain JSON responses as a string
        String getSummaryInfoJsonResponse = null;
        mStatesInfo = new HashMap<>();

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


            String resultJsonStr = "" +
                    "{status:{code:OK},data:{distance:{CA:417.25203435309,NV:362.34274672353,UT:190.47643510752,ID:578.45556674095,WA:623.92654697171,total:2172.4533298968},num_points:9}}";

            try {
                JSONObject mResponseObject = new JSONObject(resultJsonStr);
                String mQueryStatus = mResponseObject.getJSONObject("status").getString("code");
                if (mQueryStatus.equals("OK")) {
                    JSONObject mDataObject = mResponseObject.getJSONObject("data");
                    JSONObject mStateDistances = mDataObject.getJSONObject("distance");

                    Iterator<?> keys = mStateDistances.keys();

                    List<String> keyList = new ArrayList<>();

                    while (keys.hasNext()) {
                        String state = (String) keys.next();
                        keyList.add(state);

                        if (mStateCodes.equals("")) {
                            mStateCodes = mStateCodes + state;
                        } else {
                            mStateCodes = mStateCodes + ", " + state;
                        }
                    }

                    for (String key : keyList) {
                        if (!key.equals("total")) {
                            Log.i(TAG, mStateDistances.getDouble(key) + "");

                            String distance = mStateDistances.getDouble(key) + "";

                            if (mDistances.equals("")) {
                                mDistances = mDistances + distance;
                            } else {
                                mDistances = mDistances + ", " + distance;
                            }
                        }
                    }

                    mStateCodes = mStateCodes.replace("total, ", "");
                    mTotalDistance = mStateDistances.getDouble("total") + "";

                    Log.i(TAG, "States: " + mStateCodes);
                    Log.i(TAG, "Distances: " + mDistances);
                    Log.i(TAG, "Total distance: " + mTotalDistance);

                    mStatesInfo.put("codes", mStateCodes);
                    mStatesInfo.put("distances", mDistances);
                    mStatesInfo.put("total", mTotalDistance);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }


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
            //            getSummaryInfoJsonResponse = buffer.toString();
            //
            //            Log.i(TAG, "Service: " + GetSummaryInfoTask.class.getSimpleName() + ",\n" +
            //                    "Result: " + java.net.URLDecoder.decode(getSummaryInfoJsonResponse, "UTF-8"));

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
        return mStatesInfo;
    }
}