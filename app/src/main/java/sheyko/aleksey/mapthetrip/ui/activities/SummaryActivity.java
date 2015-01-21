package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.utils.helpers.VolleySingleton;

public class SummaryActivity extends Activity {

    private Trip mCurrentTrip;
    private String mTripId;
    private String mStateCodes;
    private String mStateDistances;
    private String mTotalDistance;
    private String mStateDurations;
    private boolean mIsSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mCurrentTrip = getIntent().getExtras().getParcelable("CurrentTrip");
        mTripId = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("trip_id", "-1");

        // Update UI
        ((TextView) findViewById(R.id.TripLabelDistance))
                .setText(mCurrentTrip.getDistance());
        ((EditText) findViewById(R.id.tripNameField))
                .setHint("Trip on " + mCurrentTrip.getStartTime());
    }

    public void saveButtonPressed(View view) {
        getSummaryInfo();
    }

    private void getSummaryInfo() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("64.251.25.139")
                .appendPath("trucks_app")
                .appendPath("ws")
                .appendPath("get-distance.php")
                .appendQueryParameter("truck_id", mTripId);
        String url = builder.build().toString();

        Log.i("SummaryActivity", "Service: " + "GetSummaryInfo" + ",\n" +
                "Query: " + url);

        RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        queue.add(new JsonObjectRequest(url, null,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonRoot) {

                        try {
                            String mQueryStatus = jsonRoot.getJSONObject("status").getString("code");

                            if (mQueryStatus.equals("OK")) {
                                JSONObject mDataObject = jsonRoot.getJSONObject("data");
                                JSONObject mDistances = mDataObject.getJSONObject("distance");

                                Iterator<?> keys = mDistances.keys();

                                List<String> keyList = new ArrayList<>();

                                while (keys.hasNext()) {
                                    String state = (String) keys.next();
                                    keyList.add(state);

                                    if (!state.equals("total"))
                                        if (mStateDurations.equals("")) {
                                            mStateDurations = mStateDurations + "0";
                                        } else {
                                            mStateDurations = mStateDurations + ", " + "0";
                                        }

                                    if (mStateCodes.equals("")) {
                                        mStateCodes = mStateCodes + state;
                                    } else {
                                        mStateCodes = mStateCodes + "," + state;
                                    }
                                }

                                for (String key : keyList) {
                                    if (!key.equals("total")) {

                                        String distance = mDistances.getDouble(key) + "";

                                        if (mStateDistances.equals("")) {
                                            mStateDistances = mStateDistances + distance;
                                        } else {
                                            mStateDistances = mStateDistances + ", " + distance;
                                        }
                                    }
                                }
                                mStateCodes = mStateCodes.replace("total,", "");
                                mTotalDistance = mDistances.getDouble("total") + "";
                                if (mStateDurations.equals("")) mStateDurations = "0";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            saveTrip();
                        }
                    }
                }, null));
        startActivity(new Intent(this, MainActivity.class));
    }

    private void saveTrip() {
        String tripName = ((EditText)
                findViewById(R.id.tripNameField)).getText().toString();
        String tripNotes = ((EditText)
                findViewById(R.id.tripNotesField)).getText().toString();
        if (tripName.equals("")) tripName =
                "Trip on " + mCurrentTrip.getStartTime();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("wsapp.mapthetrip.com")
                .appendPath("TrucFuelLog.svc")
                .appendPath("TFLSaveTripandSummaryInfo")
                .appendQueryParameter("TripId", mTripId)
                .appendQueryParameter("IsTripSaved", mIsSaved + "")
                .appendQueryParameter("TotalDistanceTraveled", mTotalDistance)
                .appendQueryParameter("TotalTripDuration", mCurrentTrip.getDuration() + "")
                .appendQueryParameter("TripName", "" + tripName)
                .appendQueryParameter("TripDesc", "" + tripName)
                .appendQueryParameter("TripNotes", "" + tripNotes)
                .appendQueryParameter("StateCd", mStateCodes)
                .appendQueryParameter("TotalStateDistanceTraveled", mStateDistances)
                .appendQueryParameter("Total_State_Trip_Duration", "" + mStateDurations)
                .appendQueryParameter("EntityId", "1")
                .appendQueryParameter("UserId", "1");
        String url = builder.build().toString();

        Log.i("SummaryActivity", "Service: TFLSaveTripandSummaryInfo,\n" +
                "Query: " + url);

        RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("SummaryActivity", "Service: TFLSaveTripandSummaryInfo,\n" +
                        "Result: " + response);
            }
        }, null);
        queue.add(stringRequest);
    }

    public void cancelTrip(View view) {
        cancelTrip();
    }

    private void cancelTrip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SummaryActivity.this);
        builder.setTitle(R.string.discard_trip_dialog_title);
        builder.setMessage(R.string.discard_trip_dialog_message);
        builder.setIcon(R.drawable.ic_action_discard);
        // Add the buttons
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User confirm exit
                mIsSaved = false;
                getSummaryInfo();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelTrip();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
