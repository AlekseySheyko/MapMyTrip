package sheyko.aleksey.mapthetrip.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.utils.SaveTripTask;

public class SummaryActivity extends Activity {

    private String mTripId, mDistance, mDuration, mDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        // Get trip info
        mTripId = sharedPrefs.getString("TripId", "Unspecified");
        mDistance = sharedPrefs.getString("Distance", "Unspecified");
        mDuration = sharedPrefs.getString("Duration", "Unspecified");
        mDateTime = sharedPrefs.getString("DateTime", "Unspecified");

        // Update UI
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);
        ((EditText) findViewById(R.id.tripNameField)).setHint("Trip on " + mDateTime);
    }

    public void saveTrip(View view) {
        saveTrip(true);
    }

    private void saveTrip(boolean isSaved) {
        String tripName = ((EditText) findViewById(R.id.tripNameField)).getText().toString();
        if (tripName.equals("")) tripName = "Trip on " + mDateTime;
        String tripNotes = ((EditText) findViewById(R.id.tripNotesField)).getText().toString();

        new SaveTripTask().execute(mTripId, isSaved + "", mDistance, mDuration, tripName, tripNotes);
        startActivity(new Intent(this, MainActivity.class));
    }

    public void cancelTrip(View view) {
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
}
