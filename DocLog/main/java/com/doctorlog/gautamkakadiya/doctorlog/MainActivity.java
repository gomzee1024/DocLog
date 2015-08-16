package com.doctorlog.gautamkakadiya.doctorlog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.melnykov.fab.FloatingActionButton;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    int flag=0;
    int lastquery=0;
    SearchBox search;
    DBAdapter myDb;
    MyListCursorAdapter m_adap;
    RecyclerView r_view;
    LinearLayoutManager l_manage;
    FloatingActionButton fab;
    Uri outputFileUri;
    Cursor cursor;
    Switch switchAB;
    TextView switchStatus,resultview;

    protected String _path;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/DoctorLog/";
    private static final String TAG = "DoctorLog.java";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=100;

    String timeStamp,lang1="eng";;
    SharedPreferences pref;
    int cnt=0;
    Bitmap  bitmap,thePic,result;
    Uri picuri;
    private Toolbar toolbar;
    int day,month,year,day1,month1,year1;

    //matrix that changes picture into gray scale
    public static ColorMatrix createGreyMatrix() {
        ColorMatrix matrix = new ColorMatrix(new float[] {
                0.2989f, 0.5870f, 0.1140f, 0, 0,
                0.2989f, 0.5870f, 0.1140f, 0, 0,
                0.2989f, 0.5870f, 0.1140f, 0, 0,
                0, 0, 0, 1, 0
        });
        return matrix;
    }

    // matrix that changes gray scale picture into black and white at given threshold.
    // It works this way:
    // The matrix after multiplying returns negative values for colors darker than threshold
    // and values bigger than 255 for the ones higher.
    // Because the final result is always trimed to bounds (0..255) it will result in bitmap made of black and white pixels only
    public static ColorMatrix createThresholdMatrix(int threshold) {
        ColorMatrix matrix = new ColorMatrix(new float[] {
                85.f, 85.f, 85.f, 0.f, -255.f * threshold,
                85.f, 85.f, 85.f, 0.f, -255.f * threshold,
                85.f, 85.f, 85.f, 0.f, -255.f * threshold,
                0f, 0f, 0f, 1f, 0f
        });
        return matrix;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang1 + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang1 + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang1 + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang1 + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang1 + " traineddata " + e.toString());
            }
        }

        setContentView(R.layout.activity_main);
        toolbar=(Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        pref=getSharedPreferences("mypref", 0);
        cnt=pref.getInt("counter", 0);
        openDB();
        cursor = myDb.getAllRows();
        r_view = (RecyclerView) findViewById(R.id.myrecycle);
        l_manage = new LinearLayoutManager(this);
        r_view.setLayoutManager(l_manage);
        m_adap=new MyListCursorAdapter(this,cursor,this);
        r_view.setAdapter(m_adap);
        OvershootInRightAnimator ov = new OvershootInRightAnimator();
        ov.setAddDuration(500);
        ov.setRemoveDuration(500);
        r_view.setItemAnimator(ov);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(r_view);
        fab.setOnClickListener(this);
        switchStatus= (TextView) findViewById(R.id.tooglestateview);
        switchAB=(Switch) findViewById(R.id.switchAB);
        switchAB.setChecked(false);
        search = (SearchBox) findViewById(R.id.searchbox);
        resultview = (TextView) findViewById(R.id.resultview);
        // search = new SearchBox(this);
        //search.revealFromMenuItem(R.id.action_search,this);
        //cursor.moveToFirst();
        //search.setLogoText("My App");
        if(switchAB.isChecked()){
            switchStatus.setText("Search By Date");
            cursor = myDb.getAllRows();
            if(cursor.getCount()!=0) {
                do {
                    SearchResult option = new SearchResult(cursor.getString(DBAdapter.COL_DATE), getResources().getDrawable(R.drawable.ic_history));
                    search.addSearchable(option);
                } while (cursor.moveToNext());
            }
        } else {
            switchStatus.setText("Search By Name");
            cursor = myDb.getAllRows();
            if(cursor.getCount()!=0) {
                do {
                    SearchResult option = new SearchResult(cursor.getString(DBAdapter.COL_DATE), getResources().getDrawable(R.drawable.ic_history));
                    search.addSearchable(option);
                } while (cursor.moveToNext());
            }
        }
        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    switchStatus.setText("Search By Date");
                    cursor = myDb.getAllRows();
                    if(cursor.getCount()!=0) {
                        do {
                            SearchResult option = new SearchResult(cursor.getString(DBAdapter.COL_DATE), getResources().getDrawable(R.drawable.ic_history));
                            search.addSearchable(option);
                        } while (cursor.moveToNext());
                    }
                } else {
                    switchStatus.setText("Search By Name");
                    cursor = myDb.getAllRows();
                    if(cursor.getCount()!=0) {
                        do {
                            SearchResult option = new SearchResult(cursor.getString(DBAdapter.COL_NAME), getResources().getDrawable(R.drawable.ic_history));
                            search.addSearchable(option);
                        } while (cursor.moveToNext());
                    }
                }

            }
        });

        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                Toast.makeText(MainActivity.this, "Menu click", Toast.LENGTH_LONG).show();
                search.hideCircularly(MainActivity.this);
                cursor=myDb.getAllRows();
                m_adap.changeCursor(cursor);
                flag=0;
            }

        });

        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                search.setSearchString("");
                flag=1;

            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                flag=1;


            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results
                if(switchAB.isChecked())
                {
                    cursor = myDb.searchByDate(search.getSearchText());
                    m_adap.changeCursor(cursor);
                }
                else
                {
                    cursor = myDb.searchByName(search.getSearchText());
                    m_adap.changeCursor(cursor);
                }
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(MainActivity.this, searchTerm + " Searched", Toast.LENGTH_LONG).show();
                /*cursor = myDb.searchByDate(searchTerm);
                m_adap.changeCursor(cursor);*/
            }

            @Override
            public void onSearchCleared() {
                cursor = myDb.getAllRows();
                m_adap.changeCursor(cursor);
                flag=1;

            }

        });

        search.enableVoiceRecognition(this);

    }


    public void onResume(){
        super.onResume();
        // put your code here...

           cursor=myDb.getAllRows();
            m_adap.changeCursor(cursor);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void goToCanvas(View v1,int position)
    {
        Cursor c2 = cursor;
        c2.moveToPosition(position);
        Bundle b = new Bundle();
        Intent i = new Intent(this,CanvasAct.class);
        b.putString("PATH",c2.getString(DBAdapter.COL_URL));
        i.putExtras(b);
        Log.i("MainActivity PATH:",c2.getString(DBAdapter.COL_URL));
        startActivity(i);
        /*File imagefile= new File(c2.getString(DBAdapter.COL_URL));
        Uri imageuri = Uri.fromFile(imagefile);
        ImageFileObserver ifo = new ImageFileObserver(c2.getString(DBAdapter.COL_URL));
        ifo.startWatching();
        Intent i= new Intent();
        Intent i = new Intent(Intent.ACTION_VIEW,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        i.setClassName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
        i.setAction(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        i.setDataAndType(imageuri, "image/*");
        startActivityForResult(i, 0);
        startActivity(i);*/

    }

    public void deleteFromDB(String path)
    {
        myDb.DeleteRow(path);
        cursor = myDb.getAllRows();
        m_adap.changeCursor(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.clear_log1)
        {
            myDb.deleteAll();
            File root = new File(Environment.getExternalStorageDirectory(), "DoctorLog");
            File[] Files = root.listFiles();
            if(Files != null) {
                int j;
                for(j = 0; j < Files.length; j++) {
                    System.out.println(Files[j].getAbsolutePath());
                    System.out.println(Files[j].delete());
                }
            }
            cursor=myDb.getAllRows();
            m_adap.changeCursor(cursor);
        }
        if(id==R.id.action_search)
        {

            search.revealFromMenuItem(R.id.action_search,this);
        }
        if(id==R.id.clear_log2){
           showdatepicker();
        }
        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    public void showdatepicker()
    {
        Cursor c5;
        c5= myDb.getAllRowsReverse();
        if(c5.getCount()!=0) {
            String date2 = c5.getString(DBAdapter.COL_DATE);
            day = Integer.parseInt(date2.substring(0, 2));
            month = Integer.parseInt(date2.substring(3, 5))-1;
            year = Integer.parseInt(date2.substring(6, 10));
            Log.v("c5 is not null", " ");
            DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    day1 = i2;
                    month1 = i1;
                    year1 = i;
                    takefurtheractions();
                }
            }, year, month, day);
            dpd.show();
        }
    }

    public void takefurtheractions()
    {
        String oldstring = Integer.toString(year1)+"-"+Integer.toString(month1+1)+"-" +Integer.toString(day1);
        Log.v("oldtring i:",oldstring);
        String newstring,newstring1;
        Cursor c6;
        try {
             newstring1= new SimpleDateFormat("dd/MM/yyyy  23:59:59").format(new SimpleDateFormat("yyyy-MM-dd").parse(oldstring));
            //newstring=date8.toString();
            Log.v("newtring1 is:",newstring1);
            //newstring1= newstring.substring(8, 10)+"/" + newstring.substring(5, 7)+"/" + newstring.substring(0,4)+ "  " +"00:00:00";
            Log.v("Date is:",newstring1);
            c6 = myDb.getUptoDate(newstring1);
            if(c6.getCount()!=0){
                do{
                    String pathof = c6.getString(DBAdapter.COL_URL);
                    File filetodelete = new File(pathof);
                    myDb.DeleteRow(pathof);
                    filetodelete.delete();
                }while(c6.moveToNext());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        cursor=myDb.getAllRows();
        m_adap.changeCursor(cursor);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.fab :

                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                File file = new File(DATA_PATH, timeStamp + ".jpg");
                outputFileUri = Uri.fromFile(file);

                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;

                bitmap = BitmapFactory.decodeFile(DATA_PATH + timeStamp +".jpg", options);

                picuri =  getImageUri(this, bitmap);

                performCrop();

            }
            if (requestCode == SearchBox.VOICE_RECOGNITION_CODE) {
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                search.populateEditText(matches);
            }
            if(requestCode==Crop.REQUEST_CROP)
            {
                try {
                    thePic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //result = Bitmap.createBitmap(thePic.getWidth(), thePic.getHeight(),  Bitmap.Config.ARGB_8888);
                ProcessImage();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void performCrop(){
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(picuri, destination).withAspect(3, 1).start(this);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void ProcessImage()
    {

        result = Bitmap.createBitmap(thePic.getWidth(), thePic.getHeight(),  Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(result);

        Paint bitmapPaint = new Paint();
        //first convert bitmap to grey scale:
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(createGreyMatrix()));
        c.drawBitmap(thePic, 0, 0, bitmapPaint);

        //then convert the resulting bitmap to black and white using threshold matrix
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(createThresholdMatrix(90)));
        c.drawBitmap(result, 0, 0, bitmapPaint);

        //voila! You can now draw the result bitmap anywhere You want:
        bitmapPaint.setColorFilter(null);

        result = result.copy(Bitmap.Config.ARGB_8888, true);

        ByteArrayOutputStream stream9 = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, stream9);
        byte[] byteArray = stream9.toByteArray();

        //Intent in1 = new Intent(this, Imageviewact.class);
        //in1.putExtra("image",byteArray);
        //startActivity(in1);

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang1);

        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "A BCDEFGHIJKLMNOPQRSTUVWXYZ");
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{" + "1234567890" +
              "asdfghjkll;L:'\"\\|~`xcvbnm,./<>?");


        baseApi.setImage(result);

        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();
        Log.v("Recognised text is:",recognizedText);
        //switchStatus.setText(recognizedText);
        final String[] date2 = new String[1];
        String name1;
        final String name2;
        name1 = recognizedText.replaceAll(" ","");
        name2 = name1.replaceAll("\n"," ");
        final String[] name3 = new String[1];

        final Dialog d = new Dialog(this);
        d.setTitle("Name is:");
        d.setContentView(R.layout.customdialog);
        Button ok = (Button) d.findViewById(R.id.donebtn);
        Button cancel = (Button) d.findViewById(R.id.cancelbtn);
        final EditText edtname = (EditText)d.findViewById(R.id.dialogedittext);
        edtname.setText(name2);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              name3[0] = edtname.getText().toString();
                date2[0]= timeStamp.substring(6,8) + "/" + timeStamp.substring(4,6) +"/" + timeStamp.substring(0,4) + "  " + timeStamp.substring(9,11) + ":" + timeStamp.substring(11,13) + ":" + timeStamp.substring(13,15);
                myDb.insertRow(name3[0], date2[0], Environment.getExternalStorageDirectory().toString() + "/DoctorLog/" + timeStamp + ".jpg");
                cursor=myDb.getAllRows();
                m_adap.changeCursor(cursor);
                d.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name3[0] = name2;

                date2[0] = timeStamp.substring(6, 8) + "/" + timeStamp.substring(4, 6) + "/" + timeStamp.substring(0, 4) + "  " + timeStamp.substring(9, 11) + ":" + timeStamp.substring(11, 13) + ":" + timeStamp.substring(13, 15);
                myDb.insertRow(name3[0], date2[0], Environment.getExternalStorageDirectory().toString() + "/DoctorLog/" + timeStamp + ".jpg");
                cursor = myDb.getAllRows();
                m_adap.changeCursor(cursor);
                d.dismiss();
            }
        });

        d.show();

        //date2= timeStamp.substring(6,8) + "/" + timeStamp.substring(4,6) +"/" + timeStamp.substring(0,4) + "  " + timeStamp.substring(9,11) + ":" + timeStamp.substring(11,13) + ":" + timeStamp.substring(13,15);
        //myDb.insertRow(name3[0], date2, Environment.getExternalStorageDirectory().toString() + "/DoctorLog/" + timeStamp + ".jpg");
        //m_adap.notifyItemInserted(cnt);
        cnt++;

    }

    public void onBackPressed(){
        if(flag==1)
        {
            search.hideCircularly(MainActivity.this);
            flag=2;
        }
        else if(flag==2)
        {
            cursor=myDb.getAllRows();
            m_adap.changeCursor(cursor);
            flag=0;
        }
        else
        {
            myDb.close();
            finish();
        }
    }

}
