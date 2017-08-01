package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.android.photogallery.ThumbnailDownloader.ThumbnailDownloadListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAB = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mPage = 1;
    private int mThumbNailWidth = 350;
    private int mColumnsPerLayout = 3;
    //Ch 24
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    //Track scrolling direction
    private static int firstVisibleInListview;

    private class PhotoHolder extends RecyclerView.ViewHolder {

        //Ch 24 private TextView mTitleTextView;
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            //CH 24 - mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
        }

        //public void bindGalleryItem(GalleryItem item) {
             //   mTitleTextView.setText(item.toString());
        //}

        //Ch 24 - Bind with an image
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        //Use Picasso to handle image loading
        public void bindGalleryItsm(GalleryItem galleryItem) {
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.monkey)
                    .into(mItemImageView);
        }

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        //Start an asyun task using a background threa to get urls and
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetcher().fetchItems(Integer.toString(mPage));
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //Start AsynTask (fetch items from Flickr)
        new FetchItemsTask().execute();

        //Ch 24 thumbnail photo download background thread
        //with main thread response handler combined

        //Picasso eliminates the need for the ThumbnailDownloader class
        //Handler responseHandler = new Handler();
        //mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        //mThumbnailDownloader.setThumbnailDownloadListener(
        //        new ThumbnailDownloadListener<PhotoHolder>() {
        //            @Override
        //            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
        //                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        //                photoHolder.bindDrawable(drawable);
        //            }
        //        }
        //    );
        //mThumbnailDownloader.start();
        //mThumbnailDownloader.getLooper();
        //Log.i(TAG, "Background thread started: " );

    }

    //Clean up and quite the message queue
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Picasso eliminated need for ThumbnailDownloader
        //mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed: " );
    }

    //Clean up view and attached message queue
    public void onDestroyView() {
        super.onDestroyView();
        //Picasso eliminated need for ThumbnailDownloader
        //mThumbnailDownloader.clearQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstnaceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
        //setColumn();
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public int mViewWidth;

                    @Override
                    public void onGlobalLayout() {
                        mViewWidth = mPhotoRecyclerView.getWidth();
                        Log.i(TAG, "setColumns: mViewWidth " + mViewWidth );
                        mColumnsPerLayout = (int) ( mViewWidth / mThumbNailWidth);
                        //This is a work around to reverse that columns calculations
                        //We have to find out when and where to invoke the columns calc
                        //on a listener so it is done prior to binding the value to the
                        //grid layout done below.
                        if (mColumnsPerLayout == 5) {
                            mColumnsPerLayout = 3;
                        }
                        else {
                            mColumnsPerLayout = 5;
                        }
                        Log.i(TAG, "mColumnsPerLayout: " + mColumnsPerLayout );
                        //getViewTreeObserver()
                        //        .removeGlobalOnLayoutListener(this);
                    }
                });
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mColumnsPerLayout));
        //Wire up Adapter
        setupAdapter();
        //recyclerView.setAdapter(new SoundAdapter(mBeatBox.getSounds()));

        //Add a listener for scrolling up and down through images, reloading and binding images
        mPhotoRecyclerView.addOnScrollListener(new ScrollWithPicassoListener());

        return view;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

     private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //Ch 24 using photos
            //TextView textView = new TextView(getActivity());
            //return new PhotoHolder(textView);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
       }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {

            GalleryItem galleryItem = mGalleryItems.get(position);

            //Ch 24 - useing a photo and getting thumbnails using queue and background thread
            //photoHolder.bindGalleryItem(galleryItem);

            //Picasso will handle the placeholder so we comment it out here
            //Drawable placeholder = getResources().getDrawable(R.drawable.monkey);
            //Picasso replaces the old adapter bind method to the class container object
            //photoHolder.bindDrawable(placeholder);
            photoHolder.bindGalleryItsm(galleryItem);
            //mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    //Handle scrolling in the Recycler View but with Picasso features wired up
    private class ScrollWithPicassoListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState){
            // your code there
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // When we scroll up increase the page otherwise decrease the page.
            if(dy > 0) {
                mPage = mPage + 1;
                Log.i(TAG, "RecyclerView scrolled: scroll up!");
            }
            else {
                mPage = mPage - 1;
                Log.i(TAG, "RecyclerView scrolled: scroll down!");
            }
            //Yuck now how do we get new items now?
            //The only class that gets them is below but you can't
            //instantiate a new object and do it from as below so
            //its a SNAFU a design that painted us into a corner.

            //FlickrFetcher ff = new FlickrFetcher();
            //ff.fetchItems(Integer.toString(mPage));
            //Start AsynTask (fetch items from Flickr)
            //new FetchItemsTask().execute();

        }

    }


}
