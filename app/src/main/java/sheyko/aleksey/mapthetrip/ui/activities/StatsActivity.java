package sheyko.aleksey.mapthetrip.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.ui.adapters.StatsListAdapter;

public class StatsActivity extends Activity {
    public static final String FIRST_COLUMN = "First";
    public static final String SECOND_COLUMN = "Second";

    private ArrayList<HashMap> list;

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

            TextView mTotalDistanceLabel = (TextView) findViewById(R.id.total_distance_value);
            mTotalDistanceLabel.setText(mTotalDistance);
        }

        if (!mStateCodes.equals("0")) {

            String[] separatedCodes = mStateCodes.split(",");
            String[] separatedDistances = mStateDistances.split(",");
            populateList(separatedCodes, separatedDistances);

            StatsListAdapter adapter = new StatsListAdapter(this, list);
            ListView mListView = (ListView) findViewById(R.id.stats_list);
            mListView.setAdapter(adapter);
        }
    }

    private void populateList(String[] separatedCodes, String[] separatedDistances) {

        list = new ArrayList<HashMap>();

        for (int i = 0; i < separatedCodes.length; i++) {

            HashMap temp = new HashMap();
            temp.put(FIRST_COLUMN, separatedCodes[i]);
            temp.put(SECOND_COLUMN, String.format("%.2f",
                    Float.parseFloat(
                            separatedDistances[i].trim())));
            list.add(temp);

        }
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

        if (id == R.id.action_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
