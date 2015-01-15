package sheyko.aleksey.mapthetrip.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sheyko.aleksey.mapthetrip.R;

public class NavigationAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    public NavigationAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.navigation_list_item, itemname);

        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.navigation_list_item, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text1);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image1);
        txtTitle.setText(itemname[position]);
        imageView.setImageResource(imgid[position]);
        return rowView;
    };
}
