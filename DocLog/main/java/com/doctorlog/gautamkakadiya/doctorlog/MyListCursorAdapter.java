package com.doctorlog.gautamkakadiya.doctorlog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

/**
 * Created by skyfishjy on 10/31/14.
 */
public class MyListCursorAdapter extends CursorRecyclerViewAdapter<MyListCursorAdapter.ViewHolder>{

    MainActivity m;

    public MyListCursorAdapter(Context context,Cursor cursor,MainActivity m1){
       super(context,cursor);
       m=m1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView NTextView,DTextView;
        public ImageView image;
        public ViewHolder(View view) {
            super(view);
            NTextView =(TextView) view.findViewById(R.id.textViewName);
            DTextView =(TextView) view.findViewById(R.id.textViewDate);
            image = (ImageView) view.findViewById(R.id.imageView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            m.goToCanvas(view,getPosition());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_lay, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        String first_char;
        String name = cursor.getString(DBAdapter.COL_NAME);
        String date = cursor.getString(DBAdapter.COL_DATE);

        first_char=name.substring(0,1);
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
// generate random color
        int color1 = generator.getRandomColor();
        TextDrawable drawable1 = TextDrawable.builder()
                .buildRoundRect(first_char, color1,100);
        viewHolder.image.setImageDrawable(drawable1);
        viewHolder.NTextView.setText(name);
        viewHolder.DTextView.setText(date);

    }
}