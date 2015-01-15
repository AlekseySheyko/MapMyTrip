package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.ui.adapters.SummaryListAdapter;

public class StatsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        ListView list = (ListView) findViewById(R.id.trip_list);

        // For now we'll take trip info from hard-coded array
        String[] mTripTitles = getResources().getStringArray(R.array.test_trip_names);
        String[] mTripDurations = getResources().getStringArray(R.array.test_trip_durations);
        String[] mTripDistances = getResources().getStringArray(R.array.test_trip_distances);

        SummaryListAdapter adapter = new SummaryListAdapter(this, mTripTitles, mTripDurations, mTripDistances);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_stats_accept) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
