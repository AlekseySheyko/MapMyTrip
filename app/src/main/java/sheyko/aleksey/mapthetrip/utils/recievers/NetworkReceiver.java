package sheyko.aleksey.mapthetrip.utils.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        boolean isNetworkConnected = activeInfo != null && activeInfo.isConnected();
        if (isNetworkConnected) {
            Log.i("Trip.java", "Network connected. Attempting to register trip.");
        }
    }
}