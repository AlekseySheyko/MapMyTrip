package sheyko.aleksey.mapthetrip.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;

public class StatsListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public StatsListAdapter(Context context, String[] values) {
        super(context, R.layout.stats_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.stats_list_item, parent, false);

        TextView stateCodeTextView = (TextView) rowView.findViewById(R.id.stateCodeLabel);
        stateCodeTextView.setText(values[0]);

        TextView stateDistanceTextView = (TextView) rowView.findViewById(R.id.stateDistanceLabel);
        stateDistanceTextView.setText(values[1]);


        return rowView;
    }
}
