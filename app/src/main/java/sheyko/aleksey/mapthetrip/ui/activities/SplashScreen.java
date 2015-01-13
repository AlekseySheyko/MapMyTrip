package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import sheyko.aleksey.mapthetrip.R;

public class SplashScreen extends Activity {
    /**
     * Duration of wait *
     */
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
//        Parse.initialize(this, "w7h87LOw8fzK84g0noTS1b4nZhWYXBbRCendV756", "0uzaKEj3Q9R0kTRlq6pg4vawar1HkMTrWFeZ46Yb");

        setContentView(R.layout.splash_screen);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                != ConnectionResult.SUCCESS) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.install_google_services_title)
                    .setMessage(R.string.install_google_services_message)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Close the app
                            finish();
                        }
                    });
            // Create the AlertDialog object and return it
            builder.show();
            return;
        }


        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                ParseUser currentUser = ParseUser.getCurrentUser();
//                if (currentUser != null) {
                    /* Create an Intent that will start the Main-Activity. */
                    Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
//                } else {
//                    /* Start Login-Activity. */
//                    Intent loginIntent = new Intent(SplashScreen.this, LoginActivity.class);
//                    SplashScreen.this.startActivity(loginIntent);
//                    SplashScreen.this.finish();
//                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
