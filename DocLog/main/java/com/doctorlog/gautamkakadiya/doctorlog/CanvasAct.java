package com.doctorlog.gautamkakadiya.doctorlog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;

import java.io.File;


public class CanvasAct extends AppCompatActivity {
    TouchImageView canvasview;
    Bundle b1;
    Bitmap image;
    ScaleGestureDetector scaleGestureDetector;
    DBAdapter myDb;
    String pathoff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDB();
        b1 = getIntent().getExtras();
        pathoff=b1.getString("PATH");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=4;
        image = BitmapFactory.decodeFile(pathoff, options);
        canvasview=new TouchImageView(this);
        canvasview.setImageBitmap(image);
        setContentView(canvasview);


    }

    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }


    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void closeDB() {
        myDb.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_canvas, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.deletelog) {
            File filetodelete = new File(pathoff);
            myDb.DeleteRow(pathoff);
            filetodelete.delete();
            finish();
        }
        else if(id==R.id.share){
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            Uri imageUri = Uri.parse(pathoff);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(sharingIntent);
        }
        return super.onOptionsItemSelected(item);
    }

}
