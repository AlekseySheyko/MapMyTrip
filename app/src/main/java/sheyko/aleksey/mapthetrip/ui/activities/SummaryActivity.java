package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask.OnStatesDataRetrieved;
import sheyko.aleksey.mapthetrip.utils.tasks.SaveTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;

public class SummaryActivity extends Activity
        implements OnStatesDataRetrieved {

    private String mTripId;
    private int mDuration;
    private String mDistance;
    private String mStartTime;
    private String mStateCodes;
    private String mStateDistances;
    private String mTotalDistance;
    private String mStateDurations;
    private String mStatesCount;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        sendCoordinatesToServer();
        
        Trip currentTrip = getIntent().getExtras().getParcelable("CurrentTrip");
        // Get trip info
        mDistance = currentTrip.getDistance();
        mDuration = currentTrip.getDuration();
        mStartTime = currentTrip.getStartTime();
        mTripId = currentTrip.getTripId();
        if (mTripId == null) mTripId = sharedPrefs.getString("trip_id", "");

        // Update UI
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);
        ((EditText) findViewById(R.id.tripNameField)).setHint("Trip on " + mStartTime);
    }

    public void finishSession(View view) {
        finishSession(true);
    }

    private void finishSession(boolean isSaved) {

        if (mTripId != null && isConnected()) {
            sharedPrefs.edit().putBoolean("is_saved", isSaved);
            new GetSummaryInfoTask(this).execute(mTripId);
            startActivity(new Intent(this, MainActivity.class));

        } else {
            Toast.makeText(this, "Waiting for network...",
                    Toast.LENGTH_SHORT).show();

            IntentFilter filter = new IntentFilter("ac");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isConnected() && mTripId != null) {
                        Toast.makeText(SummaryActivity.this, "Now you can save the trip",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
            registerReceiver(receiver, filter);
        }
    }

    private void sendCoordinatesToServer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> coordinates, ParseException e) {
                for (ParseObject coordinate : coordinates) {
                    coordinate.put("trip_id", mTripId);
                }
                new SendLocationTask(SummaryActivity.this).execute(coordinates);
                for (ParseObject coordinate : coordinates) {
                    coordinate.unpinInBackground();
                }
            }
        });
    }

    private void saveTrip(boolean isSaved) {
        String tripName = ((EditText) findViewById(R.id.tripNameField)).getText().toString();
        if (tripName.equals("")) tripName = "Trip on " + mStartTime;
        String tripNotes = ((EditText) findViewById(R.id.tripNotesField)).getText().toString();

        new SaveTripTask(this).execute(
                mTripId, isSaved + "", mTotalDistance,
                mDuration + "", tripName, tripNotes,
                mStateCodes, mStateDistances, mStateDurations
        );
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
                finishSession(false);
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
    public void onStatesDataRetrieved(String stateCodes, String stateDistances, String totalDistance, String statesCount) {
        mStateCodes = stateCodes;
        mStateDistances = stateDistances;
        mTotalDistance = totalDistance;
        mStatesCount = statesCount;

        saveTrip(sharedPrefs.getBoolean("is_saved", true));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // do something useful
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
