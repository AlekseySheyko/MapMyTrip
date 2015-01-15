package sheyko.aleksey.mapthetrip.utils.recievers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sheyko.aleksey.mapthetrip.utils.services.SendLocationService;


public class SendLocationAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent sendLocationIntent = new Intent(context, SendLocationService.class);
        sendLocationIntent.putExtra("action", "sendLocation");
        context.startService(sendLocationIntent);

    }

    public void SetAlarm(Context context) {



    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, SendLocationAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}