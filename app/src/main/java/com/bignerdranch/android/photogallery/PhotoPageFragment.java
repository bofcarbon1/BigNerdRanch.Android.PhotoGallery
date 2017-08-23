package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import static java.net.Proxy.Type.HTTP;

/**
 * Created by bofcarbon1 on 8/20/2017.
 */

//Ch 28 Webview

public class PhotoPageFragment extends VisibleFragment {

    private static final String TAG = "PhotoPageFragment";

    private static final String ARG_URI = "photo_page_uri";

    private Uri muri;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        muri = getArguments().getParcelable(ARG_URI);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mProgressBar =
                (ProgressBar)v.findViewById(R.id.fragment_photo_page_progressBar);
        mProgressBar.setMax(100); //WebChromeclient report in range 0-100

        mWebView = (WebView) v.findViewById(R.id.fragment_photo_page_webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //WebChromeClient
        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            public void onReceivedTitle(WebView webView, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        //Ch28 Challenge
        //Check scheme and decide on Intent.ACTION_VIEW
        //Log.i(TAG, "muri.getScheme(): " + muri.getScheme());
        if ( (muri.getScheme().equals("http")) ||
             (muri.getScheme().equals("https")) ) {
            //OK so now how do we set an intent  and tell the muri being loaded
            //Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
            //starActivity();
            //Log.i(TAG, "muri.getScheme() was http or https ");
        };

        mWebView.loadUrl(muri.toString());

        return v;
    }
}
