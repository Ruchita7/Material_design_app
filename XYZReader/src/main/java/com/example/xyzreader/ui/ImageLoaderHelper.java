package com.example.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

//This class is used to load bitmaps for photo image
public class ImageLoaderHelper {
    private static ImageLoaderHelper sInstance;
    private RequestQueue mRequestQueue;
    private Context context;

    /**
     *
     * @param context
     * @return
     */
    public static synchronized ImageLoaderHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    private final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(20);
    private ImageLoader mImageLoader;

    /**
     *
     * @param applicationContext
     */
    private ImageLoaderHelper(Context applicationContext) {
        context = applicationContext;
        //RequestQueue queue = Volley.newRequestQueue(applicationContext);
        mRequestQueue = getRequestQueue();
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                mImageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return mImageCache.get(key);
            }
        };
        mImageLoader = new ImageLoader(mRequestQueue, imageCache);
    }

    /**
     *
     * @return
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    public RequestQueue getRequestQueue()   {
        if(mRequestQueue==null) {
            mRequestQueue= Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }



}
