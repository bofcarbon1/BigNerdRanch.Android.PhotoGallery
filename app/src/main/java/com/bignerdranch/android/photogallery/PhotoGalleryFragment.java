package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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

public class PhotoGalleryFragment extends VisibleFragment {

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

    private class PhotoHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        //Ch 24 private TextView mTitleTextView;
        private ImageView mItemImageView;
        //Ch 28 Web
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            //CH 24 - mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
            //Ch 28 Web
            itemView.setOnClickListener(this);
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
            //Ch 28 Web link
            mGalleryItem = galleryItem;
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.monkey)
                    .into(mItemImageView);
        }

        @Override
        public void onClick(View v) {
            //Ch 28 WebViews now using newly created activity
            //Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        //Start 25 Search feature persist
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }
        //Start an asyun task using a background threa to get urls and
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            //Ch 25 add search feature
            //return new FlickrFetcher().fetchItems(Integer.toString(mPage));
            //String query = "robot"; // just for test

            if(mQuery == null) {
                return new FlickrFetcher().fetchRecentPhotos();
            }
            else {
                return new FlickrFetcher().searchPhotos(mQuery);
            }
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
        //Ch 25 add search feature in menu
        setHasOptionsMenu(true);
        //Start AsynTask (fetch items from Flickr)
        //Ch25 add search feature
        //new FetchItemsTask().execute();
        updateItems();

        //Ch26 Intents
        //Intent i = PollService.newIntent(getActivity());
        //getActivity().startService(i);

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

    //Ch25 Added search feature
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_itesm_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                //Ch25 search feature persist search text
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
                //Ch25 Challenge 1 (This one was a bust)
                //the challenge seems to be aimed at deprecated code techniques.
                //Besides which making these changes would not make the photo
                //loading look faster
                searchView.setIconified(true);
                //searchItem.collapseActionView();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String query = QueryPreferences.getStoredQuery(getActivity());
            searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_itesm_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        }
        else {
            toggleItem.setTitle(R.string.start_polling);
        }

    }

    //Ch25 Added search feature persist search string using shared preference
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_itesm_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_itesm_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                //Tell PhotogalleryActivity to update its toolbar options
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Ch25 Added search feature
    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
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
