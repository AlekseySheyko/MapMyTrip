package sheyko.aleksey.mapthetrip.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;

public class SummaryListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] tripname;
    private final String[] tripduration;
    private final String[] tripdistance;

    public SummaryListAdapter(Activity context, String[] tripname, String[] tripduration, String[] tripdistance) {
        super(context, R.layout.list_item_trip, tripname);

        this.context = context;
        this.tripname = tripname;
        this.tripduration = tripduration;
        this.tripdistance = tripdistance;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_trip, null, true);
        TextView tripName = (TextView) rowView.findViewById(R.id.trip_name);
        tripName.setText(tripname[position]);
        TextView tripDuration = (TextView) rowView.findViewById(R.id.trip_duration);
        tripDuration.setText(tripduration[position]);
        TextView tripDistance = (TextView) rowView.findViewById(R.id.trip_distance);
        tripDistance.setText(tripdistance[position]);
        return rowView;
    }

}
