package sheyko.aleksey.mapmytrip.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import sheyko.aleksey.mapmytrip.R;
import sheyko.aleksey.mapmytrip.utils.SaveTripTask;

public class InputActivity extends Activity {

    private String mTripId;
    private SharedPreferences sharedPrefs;

    private String testVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        sharedPrefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_APPEND);

        mTripId = sharedPrefs.getString("TripId", "Unspecified");
        Log.i("InputActivity", mTripId);

        TextView discardButton = (TextView) findViewById(R.id.discardButton);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                builder.setTitle(R.string.discard_trip_dialog_title);
                builder.setMessage(R.string.discard_trip_dialog_message);
                builder.setIcon(R.drawable.ic_action_discard);
                // Add the buttons
                builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        new SaveTripTask().execute(mTripId, "false");

                        startActivity(new Intent(InputActivity.this, MainActivity.class));
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

    public void saveTrip(View view) {
        new SaveTripTask().execute(mTripId, "true");
        startActivity(new Intent(this, MainActivity.class));
    }

}
