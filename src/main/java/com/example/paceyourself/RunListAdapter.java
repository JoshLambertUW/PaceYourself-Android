package com.example.paceyourself;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Admin on 6/5/2018.
 */
public class RunListAdapter extends ArrayAdapter<Run> {

    private Context context;
    List<Run> runHistory;
    runData rundata;

    public RunListAdapter(Context context, List<Run> runs) {
        super(context, R.layout.run_list_item, runs);
        this.context = context;
        this.runHistory = runs;
        rundata = new runData();
    }
    private class ViewHolder {
        TextView runDateTxt;
        TextView runTimeTxt;
        TextView runLengthTxt;
        ImageView runMapPrev;
    }

    @Override
    public int getCount() {
        return runHistory.size();
    }

    @Override
    public Run getItem(int position) {
        return runHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.run_list_item, null);
            holder = new ViewHolder();
            holder.runDateTxt = (TextView) convertView
                    .findViewById(R.id.run_date);
            holder.runTimeTxt = (TextView) convertView
                    .findViewById(R.id.run_time);
            holder.runLengthTxt = (TextView) convertView
                    .findViewById(R.id.run_length);
            holder.runMapPrev = (ImageView) convertView
                    .findViewById(R.id.run_map_preview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Run run = (Run) getItem(position);
        holder.runDateTxt.setText(run.getDateString());
        holder.runTimeTxt.setText(run.getTotalTimeText());
        holder.runLengthTxt.setText(run.getTotalDistanceText(context));
        holder.runMapPrev.setImageBitmap(run.getMapPreview());

        return convertView;
    }

    /*Checks whether a particular product exists in SharedPreferences*/
    public boolean checkRun(Run checkRun) {
        boolean check = false;
        List<Run> runHistory = rundata.getRunHistory(context); //Context needed?
        if (runHistory != null) {
            for (Run run : runHistory) {
                if (run.equals(checkRun)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    @Override
    public void add(Run run) {
        super.add(run);
        runHistory.add(run);
        notifyDataSetChanged();
    }

    @Override
    public void remove(Run run) {
        super.remove(run);
        runHistory.remove(run);
        notifyDataSetChanged();
    }

}
