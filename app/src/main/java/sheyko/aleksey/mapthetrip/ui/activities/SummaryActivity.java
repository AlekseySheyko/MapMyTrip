package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    private String mStartTime;
    String mStateCodes;
    String mStateDistances;
    String mTotalDistance;
    String mStateDurations = "";
    int mStatesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Trip currentTrip = getIntent().getExtras().getParcelable("CurrentTrip");

        // Get trip info
        mTripId = currentTrip.getTripId();
        if (mTripId == null)
            PreferenceManager.getDefaultSharedPreferences(this).
                    getString("trip_id", "");

        String mDistance = currentTrip.getDistance();
        mDuration = currentTrip.getDuration();
        mStartTime = currentTrip.getStartTime();

        // Retrieve saved coordinates from local database
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

        new GetSummaryInfoTask(this).execute(mTripId);

        // Update UI
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);
        ((EditText) findViewById(R.id.tripNameField)).setHint("Trip on " + mStartTime);
    }

    public void saveTrip(View view) {
        saveTrip(true);
    }

    private void saveTrip(boolean isSaved) {
        String tripName = ((EditText) findViewById(R.id.tripNameField)).getText().toString();
        if (tripName.equals("")) tripName = "Trip on " + mStartTime;
        String tripNotes = ((EditText) findViewById(R.id.tripNotesField)).getText().toString();

        for (int i = 0; i <= mStatesCount; i++) {
            if (mStateDurations.equals("")) {
                mStateDurations = mStateDurations + "0";
            } else {
                mStateDurations = mStateDurations + ", " + "0";
            }
        }

        new SaveTripTask().execute(
                mTripId, isSaved + "", mTotalDistance,
                mDuration + "", tripName, tripNotes,
                mStateCodes, mStateDistances, mStateDurations
        );
        startActivity(new Intent(this, MainActivity.class));
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
                saveTrip(false);
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
        mStatesCount = 0;
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
}
