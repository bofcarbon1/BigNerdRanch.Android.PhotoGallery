package com.bignerdranch.android.photogallery;

import android.support.annotation.LayoutRes;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class SingleFragmentActivity extends AppCompatActivity  {

    private static final String TAG = "SingleFragmentActivity";
    protected  abstract Fragment createFragment();

    //Ch 17 Tablet/Phone support
    @LayoutRes
    protected int getLayoutResId() {
        Log.d(TAG, "getLayoutResId started");
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ch 17 Table/Phone support
        Log.d(TAG, "onCreate setContentView started");
        setContentView(getLayoutResId());

        //Work with the fragment hosted by this activity via the fragment manager
        FragmentManager fm = getSupportFragmentManager();
        //Get the fragment refereneced in activity_fragmentent.xml from the fragment manager
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        //If the fragment does not exist add it to the fragment manager
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

    }

}
