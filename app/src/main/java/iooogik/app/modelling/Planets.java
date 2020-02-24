package iooogik.app.modelling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class Planets extends Fragment implements View.OnClickListener {

    View view;
    private Database mDBHelper;
    static Typeface standartFont;
    private Cursor userCursor;
    private SQLiteDatabase mDb;


    public Planets(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_planets, container ,false);
        Button showAR = view.findViewById(R.id.openAr);
        showAR.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        standartFont = Typeface.createFromAsset
                (Objects.requireNonNull(getContext()).getAssets(), "rostelekom.otf");

        mDBHelper = new Database(getContext());
        mDBHelper.openDataBase();
        mDBHelper.updateDataBase();
        mDb = mDBHelper.getReadableDatabase();
        userCursor = mDb.rawQuery("Select * from Planets", null);
        getPlanets();

    }

    private void getPlanets(){
        userCursor.moveToLast();
        String name, description;
        Bitmap bitmap = null;
        int max = userCursor.getInt(userCursor.getColumnIndex("_id"));
        userCursor.moveToFirst();
        for (int i = 0; i < max; i++) {
            name = userCursor.getString(userCursor.getColumnIndex("name"));
            description = userCursor.getString(userCursor.getColumnIndex("description"));
            byte[] bytesImg = userCursor.getBlob(userCursor.getColumnIndex("images"));
            if(bytesImg!=null)
            bitmap = BitmapFactory.decodeByteArray(bytesImg, 0, bytesImg.length);
            setInformation(name, description, bitmap, i);
            userCursor.moveToNext();
        }
    }

    private void setInformation(String name, String description, Bitmap bitmap, int id){
        LinearLayout linearLayout = view.findViewById(R.id.linear);
        @SuppressLint("InflateParams")
        View view1 = getLayoutInflater().inflate(R.layout.item_planet, null, false);
        FrameLayout frameLayout = view1.findViewById(R.id.frame_formulae);
        ImageView imageView = frameLayout.findViewById(R.id.formulae);
        TextView desc = frameLayout.findViewById(R.id.description);
        TextView nameTv = frameLayout.findViewById(R.id.namePlanet);
        nameTv.setText(name);
        int width = 300;
        int height = 300;
        if(bitmap != null)
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));

        desc.setText(description);

        view1.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("_id", id);
            ScrollingArticle scrollingArticle = new ScrollingArticle();
            scrollingArticle.setArguments(args);
            showPlanetInfo(scrollingArticle);
            MainActivity.FAB.setVisibility(View.VISIBLE);

            MainActivity.FAB.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    R.drawable.baseline_arrow_back_white_24dp));

            MainActivity.FAB.setOnClickListener(v1 -> {
                FrameLayout frameLayout1 = view.findViewById(R.id.planets_frame);
                frameLayout1.setVisibility(View.GONE);
                frameLayout1.removeAllViews();
                MainActivity.FAB.setVisibility(View.GONE);
            });
        });

        linearLayout.addView(view1);

    }

    private void showPlanetInfo(Fragment fragment){
        FrameLayout frameLayout = view.findViewById(R.id.planets_frame);
        frameLayout.setVisibility(View.VISIBLE);


        FragmentManager fm = getFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();

        if (fragment != null) {
            ft.remove(fragment).commitAllowingStateLoss();
        }

        FragmentTransaction addTransaction = fm.beginTransaction();
        addTransaction.setCustomAnimations
                (R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
        addTransaction.addToBackStack(null);
        assert fragment != null;
        addTransaction.add(R.id.planets_frame, fragment,
                "planets_frame").commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.openAr){
            Intent intent = new Intent(getContext(), ARcamera.class);
            intent.putExtra("TYPE", "SolarSystem");
            startActivity(intent);
        }
    }
}
