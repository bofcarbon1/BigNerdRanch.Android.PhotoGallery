package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by bofcarbon1 on 7/29/2017.
 */

//Ch24 Challgenge - Use cache memory to store photos

public class PhotoCache {

    //Cache setup
    private static final String TAG = "PhotoCache";
    int maxMemory = (int) Runtime.getRuntime().maxMemory();
    private LruCache<String, Bitmap> mLruPhotoCache;
    private static PhotoCache mPhotoCache;

    //Cache constructor instantiate and return PhotoCache object
    public static PhotoCache getInstance() {
        if (mPhotoCache == null) {
            mPhotoCache = new PhotoCache();
        }
        return mPhotoCache;
    }

    //Cache constructor calculate LruCache size
    public PhotoCache() {
        int maxSize = (int) Runtime.getRuntime().maxMemory() / 1024;
        mLruPhotoCache = new LruCache<String, Bitmap>(maxSize / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public void addToLruPhotoCache(String key, Bitmap value) {
        Log.i(TAG, "key: " + key);
        if (mLruPhotoCache.get(key) == null)
            mLruPhotoCache.put(key, value);
        //String strKey = mLruPhotoCache.get(key).toString();
        //Log.i(TAG, "Key saved to LruCache: " + strKey);
    }

    public Bitmap getBitmapFromCache(String key) {
        return mLruPhotoCache.get(key);
    }

    //Notes
    //The action of caching is with ThumbnailDownloader.
    //When a page is requested from the main thread url strings
    //are obtained for the page As each page is requested photos are
    //stored in cache. Pages increase as scrolling down is done.
    //When scrolling up we will go to a previous page. If we go to
    //a previous page we get the list of urls then we check to see if
    // we have that photos in chache by passing the url as the key.
    // if it iexists in cache we use the Bitmpa from cache instead of
    // getting it from Flicker.
    // So ThumbnailDownloader will always handle putting the Bitmap in
    // the response connected to the main thread UI.
    //


}
