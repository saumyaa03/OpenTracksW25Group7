package de.dennisguse.opentracks.markers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.Marker;

public class MarkerListAdapter extends BaseAdapter {
    private final Context context;
    private List<Marker> markers;

    public MarkerListAdapter(Context context, List<Marker> markers) {
        this.context = context;
        this.markers = markers;
    }

    public void swapCursor(List<Marker> newMarkers) {
        this.markers = newMarkers;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return markers != null ? markers.size() : 0;
    }

    @Override
    public Marker getItem(int position) {
        return markers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.marker_list_item, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.marker_list_item_name);
            holder.description = convertView.findViewById(R.id.marker_list_item_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Marker marker = getItem(position);
        holder.name.setText(marker.getName());
        holder.description.setText(marker.getDescription());

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }
} 