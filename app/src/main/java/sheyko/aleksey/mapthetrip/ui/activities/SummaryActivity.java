package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask.OnStatesDataRetrieved;
import sheyko.aleksey.mapthetrip.utils.tasks.SaveTripTask;

public class SummaryActivity extends Activity
        implements OnStatesDataRetrieved {

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

        if (mTripId != null && isConnected()) {
            new GetSummaryInfoTask(this).execute(mTripId);
            startActivity(new Intent(this, MainActivity.class));

        }
    }

    private void saveTrip() {
        String tripName = ((EditText)
                findViewById(R.id.tripNameField)).getText().toString();
        String tripNotes = ((EditText)
                findViewById(R.id.tripNotesField)).getText().toString();
        if (tripName.equals("")) tripName =
                "Trip on " + mCurrentTrip.getStartTime();

        new SaveTripTask(this).execute(
                mTripId, mIsSaved + "", mTotalDistance,
                mCurrentTrip.getDuration() + "", tripName, tripNotes,
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
    public void onStatesDataRetrieved(String stateCodes, String stateDistances, String totalDistance, String statesDurations) {
        mStateCodes = stateCodes;
        mStateDistances = stateDistances;
        mTotalDistance = totalDistance;
        mStateDurations = statesDurations;

        saveTrip();
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
