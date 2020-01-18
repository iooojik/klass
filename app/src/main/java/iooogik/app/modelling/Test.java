package iooogik.app.modelling;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class Test extends Fragment {

    View view;
    private ArrayList<String> testTitles;


    public Test() {
        testTitles = new ArrayList<String>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_test, container, false);
        setTestTitles();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        testTitles.add("Тест по Солнечной системе");
    }

    private void setTestTitles(){
        ArrayAdapter<String> adapterThemes = new ArrayAdapter<>(view.getContext(),
                R.layout.item_project, testTitles);

        ListView listView = view.findViewById(R.id.testThemes);
        listView.setAdapter(adapterThemes);
        TestFrame testFrame = new TestFrame();
        listView.setOnItemClickListener((parent, view, position, id) ->
                showFragment(testFrame));
    }

    private void showFragment(Fragment fragment){
        FrameLayout frameLayout = view.findViewById(R.id.test_frame);
        frameLayout.setVisibility(View.VISIBLE);
        MainActivity.currFragmeLayout = frameLayout;

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
        addTransaction.add(R.id.test_frame, fragment,
                "testFrame").commitAllowingStateLoss();
    }
}
