package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.ui.adapters.StatsListAdapter;

public class StatsActivity extends Activity {

    private String mTotalDistance;
    private String mStateCodes;
    private String mStateDistances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        if (getIntent() != null) {
            mTotalDistance = getIntent().getStringExtra("total_distance");
            mStateCodes = getIntent().getStringExtra("state_codes");
            mStateDistances = getIntent().getStringExtra("state_distances");
        }

        TextView mTotalDistanceLabel = (TextView) findViewById(R.id.total_distance_label);
        mTotalDistanceLabel.setText(mTotalDistance);

        String[] values = new String[] { mStateCodes, mStateDistances };

        StatsListAdapter listAdapter = new StatsListAdapter(
                this, values);

        ListView mListView = (ListView) findViewById(R.id.stats_list);
        mListView.setAdapter(listAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
