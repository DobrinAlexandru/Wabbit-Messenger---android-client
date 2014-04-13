package com.wabbit.libraries.asyncview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.libraries.cache.CacheableBitmapDrawable;
import com.wabbit.libraries.remoting.HttpHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;


public class BitmapWorkerTask extends AsyncTask<Object, Void, CacheableBitmapDrawable> {

    private WeakReference<AsyncImageView> mImageViewReference;
    public String url;
    private boolean fromMemory;
    public BitmapWorkerTask(AsyncImageView iv) {
        mImageViewReference = new WeakReference<AsyncImageView>(iv);
    }

    @Override
    protected CacheableBitmapDrawable doInBackground(Object... params) {
        url = (String) params[0];
        BitmapLruCache cacheMgr = (BitmapLruCache) params[1];
        if(!cacheMgr.containsInMemoryCache(url)){
            cacheMgr.put(url, HttpHelper.loadImage(url));
            fromMemory = false;
        }
        else{
            fromMemory = true;
        }

        return cacheMgr.getFromMemoryCache(url);
    }

    @Override
    protected void onPostExecute(CacheableBitmapDrawable bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (mImageViewReference != null && bitmap != null) {
            final AsyncImageView imageView = mImageViewReference.get();

            if (imageView != null) {
                final BitmapWorkerTask imageViewTask = imageView.getBitmapWorkerTask();

                if (imageViewTask == this) {
                    imageView.setImageDrawable(bitmap, fromMemory);
                }
            }
        }
    }

    private static Bitmap decodeSampledBitmapFromUrl(String url, int reqWidth, int reqHeight) throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;

        InputStream stream = fetchStream(url);
        BitmapFactory.decodeStream(stream, null, options);
        stream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        stream = fetchStream(url);
        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        stream.close();

        return bitmap;
    }

    private static InputStream fetchStream(String urlString) throws IllegalStateException, IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
    }

    private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}