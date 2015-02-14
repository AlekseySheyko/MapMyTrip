package sheyko.aleksey.mapthetrip.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sheyko.aleksey.mapthetrip.R;

public class StatsListAdapter extends BaseAdapter {
    public static final String FIRST_COLUMN = "First";
    public static final String SECOND_COLUMN = "Second";

    public ArrayList<HashMap> list;
    Activity activity;

    public StatsListAdapter(Activity activity, ArrayList<HashMap> list) {
        super();
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView txtFirst;
        TextView txtSecond;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.stats_list_item, null);
            holder = new ViewHolder();
            holder.txtFirst = (TextView) convertView.findViewById(R.id.stateCodeLabel);
            holder.txtSecond = (TextView) convertView.findViewById(R.id.stateDistanceLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap map = list.get(position);
        holder.txtFirst.setText(map.get(FIRST_COLUMN) + "");
        holder.txtSecond.setText(map.get(SECOND_COLUMN) + "");

        return convertView;
    }
}
