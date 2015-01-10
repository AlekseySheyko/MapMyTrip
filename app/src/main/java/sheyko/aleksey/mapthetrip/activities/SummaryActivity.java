package sheyko.aleksey.mapthetrip.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.utils.SaveTripTask;

public class SummaryActivity extends Activity {

    private String mTripId;
    private String mDistance;
    private String mDuration;
    private String mTripName;
    private String mTripNotes;
    private String mDateTime;

    private EditText tripNameField;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        sharedPrefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_APPEND);

        // Get trip info
        mTripId = sharedPrefs.getString("TripId", "Unspecified");
        mDistance = sharedPrefs.getString("Distance", "Unspecified");
        mDuration = sharedPrefs.getString("Duration", "Unspecified");
        mDateTime = sharedPrefs.getString("DateTime", "Unspecified");

        // Update counters with trip info
        ((TextView) findViewById(R.id.TripLabelDistance)).setText(mDistance);

        tripNameField = (EditText) findViewById(R.id.tripNameField);
        tripNameField.setHint("Trip on " + mDateTime);

        TextView discardButton = (TextView) findViewById(R.id.discardButton);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });
    }

    public void saveButtonPressed(View view) {
        saveTrip(true);
    }

    private void saveTrip(boolean isSaved) {
        mTripName = ((EditText) findViewById(R.id.tripNameField)).getText().toString();
        if (mTripName.equals("")) mTripName = "Trip on " + mDateTime;
        mTripNotes = ((EditText) findViewById(R.id.tripNotesField)).getText().toString();

        new SaveTripTask().execute(mTripId, "" + isSaved, mDistance, mDuration, mTripName, mTripNotes);
        startActivity(new Intent(this, MainActivity.class));
    }
}
