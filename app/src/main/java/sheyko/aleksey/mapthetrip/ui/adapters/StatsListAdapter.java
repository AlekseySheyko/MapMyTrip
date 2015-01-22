package sheyko.aleksey.mapthetrip.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;

public class StatsListAdapter extends ArrayAdapter<String> {
    private final String[] values;

    private String[] separatedCodes;
    private String[] separatedDistances;

    public StatsListAdapter(Context context, String[] values) {
        super(context, R.layout.stats_list_item, values);
        this.values = values;

        String stateCodes = values[0];
        String stateDistances = values[1];

        separatedCodes = stateCodes.split(",");
        separatedDistances = stateDistances.split(",");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.stats_list_item, null);
        }

        if (separatedCodes[0] != null
                && separatedDistances[0] != null) {

            try {
                TextView stateCodeTextView = (TextView) v.findViewById(R.id.stateCodeLabel);
                stateCodeTextView.setText(separatedCodes[position]);

                TextView stateDistanceTextView = (TextView) v.findViewById(R.id.stateDistanceLabel);
                stateDistanceTextView.setText(
                        String.format("%.2f",
                                Float.parseFloat(
                                        separatedDistances[position].trim())));
            } catch (Exception ignored) {
            }
        }

        return v;
    }
}
