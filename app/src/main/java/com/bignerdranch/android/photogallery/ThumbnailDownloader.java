package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
//import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static android.content.ContentValues.TAG;

/**
 * Created by bofcarbon1 on 7/26/2017.
 */

//Ch 24 Url for downloading photo thumbnail

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    //Setup for the request handler on the background thread
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    //Setup for the response handler on the main thread
    private Handler mResponsehandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    //Listener for response handler main thread
    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    //Listener for response handler main thread
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    //Background thread download gets main thread response handler
    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponsehandler = responseHandler;
    }

    //Looper action to handle message on background thread
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    //Load thumbnail into message queue for the background thread
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a url: " + url );
        if (url == null) {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    //Clean all requests from the message queue
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    //background request handler
    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }
            byte[] bitMapbytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitMapbytes, 0, bitMapbytes.length);
            Log.i(TAG, "Bitmap created ");
            //Implement callback Runnable for response hanlder post process
            mResponsehandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
            //Save the Bitmap photo to the photo cache
            PhotoCache photocache = new PhotoCache();
            photocache.addToLruPhotoCache(url, bitmap);
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error downloading image ", ioe);
        }
    }

}
