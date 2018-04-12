package com.example.android.trainscheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<String>{

    private Context ctx;
    private ArrayList<String> arrayList;

    public ListAdapter(Context ctx, int resource, ArrayList<String> obj){
        super(ctx,resource,obj);
        this.ctx = ctx;
        this.arrayList = obj;
    }

    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        return getCustomView(position, cnvtView, prnt);
    }
    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {
        return getCustomView(pos, cnvtView, prnt);
    }

    public View getCustomView(int i, View view, ViewGroup viewGroup) {
        View mySpinner = LayoutInflater.from(ctx).inflate(R.layout.spinner_item, viewGroup,false);
        TextView item = mySpinner.findViewById(R.id.item);
        item.setText(arrayList.get(i));
        item.setSelected(true);
        mySpinner.setTag(arrayList.get(i));
        return mySpinner;
    }
}
