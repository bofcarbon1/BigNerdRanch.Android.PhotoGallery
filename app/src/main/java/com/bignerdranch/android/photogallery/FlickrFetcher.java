package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by bofcarbon1 on 7/20/2017.
 */

public class FlickrFetcher {

    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY = "4ba305ffed7eb9e063e1d16f5e928bb4";

    //Connect to Flicker via http with url
    //Get the url strings to retrieve phots images later
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            return out.toByteArray();

        }
        finally {
            connection.disconnect();
        }
    }

    //Gets json string with photo details to parse later
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(String page) {

        Log.i(TAG, "fetchItems: page: " + page );
        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    //We removed a previous challenge to page when we got Picasso
                    //for some reason starting a bunch of new async task threads
                    //caused Picasso to keep binding and endlessly change images in the view
                    //.appendQueryParameter("per_page", "15")
                    //.appendQueryParameter("page", page)
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Recived json: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        }
        catch (JSONException je) {
            Log.e(TAG, "Failed to parse json "+ je.getMessage());
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items "+ ioe.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to fetch items" + e.getMessage());
        }
        finally {
            return items;
        }
    }

    //Parse the JSON string that will get us the urls for the photo images later
    //We should convert this to Gson
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
        throws IOException, JSONException {

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photosJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photosJsonArray.length(); i++) {
            JSONObject photoJsonObject = photosJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if(!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

}
