package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask.OnLocationSent;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class SummaryActivity extends Activity
        implements OnStatesDataRetrieved, OnLocationSent {

    private String mTripId;
    private int mDuration;
    private String mDistance;
    private String mStartTime;
    private String mTripName;
    private String mTripNotes;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_summary);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Trip currentTrip = getIntent().getExtras().getParcelable("CurrentTrip");
        mDistance = currentTrip.getDistance();
        mDuration = currentTrip.getDuration();
        mStartTime = currentTrip.getStartTime();
        mTripId = mSharedPrefs.getString("trip_id", "");

        // Update UI
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);
        ((EditText) findViewById(R.id.tripNameField)).setHint("Trip on " + mStartTime);
    }

    public void finishSession(View view) {
        finishSession(true);
    }

    private void finishSession(boolean isSaved) {
        mSharedPrefs.edit().putBoolean("is_saved", isSaved);
        if (isOnline()) {
            setProgressBarIndeterminateVisibility(true);
            sendCoordinatesToServer();
            updateStatusOnServer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SummaryActivity.this);
            builder.setTitle("Network lost");
            builder.setMessage("Please wait for a network to update trip status");
            builder.setIcon(R.drawable.ic_action_airplane_mode_on);
            // Add the buttons
            builder.setPositiveButton("Wait", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                }
            });
            builder.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User confirm exit
                    startActivity(new Intent(SummaryActivity.this, MainActivity.class));
                }
            });
            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void sendCoordinatesToServer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> coordinates, ParseException e) {
                new SendLocationTask(SummaryActivity.this, SummaryActivity.this).execute(coordinates);
                for (ParseObject coordinate : coordinates) {
                    coordinate.deleteInBackground();
                }
            }
        });
    }

    private void updateStatusOnServer() {
        ParseQuery<ParseObject> status = ParseQuery.getQuery("Status");
        status.fromLocalDatastore();
        status.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> coordinates, ParseException e) {
                for (ParseObject status : coordinates) {
                    new UpdateTripStatusTask(SummaryActivity.this).execute(
                            status.getString("trip_id"), status.getString("status"));
                    status.deleteInBackground();
                }
            }
        });
    }

    @Override
    public void onLocationSent() {
        new GetSummaryInfoTask(SummaryActivity.this).execute(mTripId);
    }

    private void startSummaryTask() {
        ParseQuery<ParseObject> status = ParseQuery.getQuery("SummaryTask");
        status.fromLocalDatastore();
        status.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> summaryTasks, ParseException e) {
                for (ParseObject task : summaryTasks) {
                    new GetSummaryInfoTask(SummaryActivity.this).execute(
                            task.getString("trip_id"));
                    task.deleteInBackground();
                }
            }
        });
    }

    @Override
    public void onSummaryDataRetrieved(String id, String stateCodes, String stateDistances,
                                       String totalDistance, String statesDurations) {
        saveTrip(id, stateCodes, stateDistances, totalDistance, statesDurations);
    }

    private void saveTrip(String id, String stateCodes, String stateDistances,
                          String totalDistance, String statesDurations) {
        mTripName = ((EditText) findViewById(R.id.tripNameField)).getText().toString();
        if (mTripName.isEmpty()) mTripName = "Trip on " + mStartTime;
        mTripNotes = ((EditText) findViewById(R.id.tripNotesField)).getText().toString();

        try {
            ParseObject coordinates = new ParseObject("SaveTripTask");
            coordinates.put("trip_id", id);
            coordinates.put("is_saved", "true");
            coordinates.put("total_distance", totalDistance);
            coordinates.put("duration", mDuration + "");
            coordinates.put("name", mTripName);
            coordinates.put("notes", mTripNotes);
            coordinates.put("state_codes", stateCodes);
            coordinates.put("state_distances", stateDistances);
            coordinates.put("state_durations", statesDurations);
            coordinates.pinInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isOnline()) {
            saveTripOnServer(id, stateCodes, stateDistances, totalDistance, statesDurations);
        }
        setProgressBarIndeterminateVisibility(false);

        startActivity(new Intent(this, StatsActivity.class)
                .putExtra("total_distance", totalDistance)
                .putExtra("state_codes", stateCodes)
                .putExtra("state_distances", stateDistances));
    }

    private void saveTripOnServer(final String id, final String stateCodes, final String stateDistances,
                                  final String totalDistance, final String stateDurations) {
        new SaveTripTask(SummaryActivity.this).execute(
                id, "true", totalDistance,
                mDuration + "", mTripName, mTripNotes,
                stateCodes, stateDistances, stateDurations
        );
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
