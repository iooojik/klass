package iooogik.app.modelling;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class ScrollingArticle extends AppCompatActivity  implements View.OnClickListener{

    Cursor userCursor;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling_article);

        FloatingActionButton back = findViewById(R.id.back);
        Button showAR = findViewById(R.id.openAr);

        back.setOnClickListener(this);
        showAR.setOnClickListener(this);


        Bundle args = getIntent().getExtras();
        assert args != null;
        int id = args.getInt("_id");

        mDBHelper = new DatabaseHelper(this);
        mDBHelper.openDataBase();

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        mDb = mDBHelper.getReadableDatabase();
        userCursor =  mDb.rawQuery("Select * from Planets", null);

        userCursor.moveToPosition(id);

        TextView textView = findViewById(R.id.article_text);

        textView.setText(Html.fromHtml(getEditedText()));
        ImageView imageView = findViewById(R.id.sc_back);

        Bitmap bitmap;

        byte[] bytesImg = userCursor.getBlob(userCursor.getColumnIndex("images"));
        bitmap =  BitmapFactory.decodeByteArray(bytesImg, 0, bytesImg.length);

        imageView.setImageBitmap(bitmap);

    }

    private String getEditedText(){
        String tempText = userCursor.getString(userCursor.getColumnIndex("article"));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (int i = 0; i < tempText.length(); i++) {
                stringBuilder.append(tempText.charAt(i));
            }
        }catch (Exception e){
            Log.i("scrollingArticle", String.valueOf(e));
        }


        return stringBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.back){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.openAr){
            Intent intent = new Intent(getApplicationContext(), ARcamera.class);
            startActivity(intent);
        }
    }
}