package com.wabbit.imagesutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.wabbit.imagesutils.ImageDownloader.IOnImageDownloadListener;
import com.wabbit.imagesutils.ImageDownloader.ImageDownloaderPacker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class ImageUtils {
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	public static Bitmap drawableToBitmap (Drawable drawable, int pWidth, int pHeight) {
	    if (drawable instanceof BitmapDrawable) {
	        return resizeBitmap(((BitmapDrawable)drawable).getBitmap(), pWidth, pHeight, true);
	    }

	    Bitmap bitmap = Bitmap.createBitmap(pWidth, pHeight, Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	public static void drawBitmapToCanvas(final Canvas pCanvas, final Bitmap pBitmap){
		final Paint paint = new Paint();
		pCanvas.drawBitmap(pBitmap, 0, 0, paint);
	}
	
	public static Drawable getDrawableFromResources(final Context pContext, final int pId){
		return pContext.getResources().getDrawable(pId);
	}
	
	public static Bitmap downloadBitmap(String url) throws IOException {
        HttpUriRequest request = new HttpGet(url.toString());
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
 
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
 
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                    bytes.length);
            return bitmap;
        } else {
            throw new IOException("Download failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
    }
	
	public static Bitmap downloadBitmap2(String url){
		URL img_value = null;
		try {
			img_value = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			return BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return null;
	}
	
	public static Bitmap resizeBitmap(final Bitmap pBitmap, final int pWIdth, final int pHeight, final boolean pRecycle){
		/*final Bitmap scaledBitmap = Bitmap.createBitmap(pWIdth, pHeight, Config.ARGB_8888);

		float ratioX = pWIdth / (float) pBitmap.getWidth();
		float ratioY = pHeight / (float) pBitmap.getHeight();

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, pBitmap.getWidth() * 0.5f, pBitmap.getHeight() * 0.5f);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(pBitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
		*/
		//Bitmap result = Bitmap.createScaledBitmap(pBitmap, pWIdth, pHeight, false);
		//if(pRecycle)
		//	pBitmap.recycle();
		
		return Bitmap.createScaledBitmap(pBitmap, pWIdth, pHeight, false);
	}
	
	private static HashMap<String, Bitmap> images = new HashMap<String, Bitmap>();
	public static void downloadProfilePicture(final String url, final IOnImageDownloadListener pListener){
		final IOnImageDownloadListener listner = new IOnImageDownloadListener() { 
			@Override
			public void onImageDownloaded(Bitmap pBitmap) { 
				images.put(url, pBitmap);
				if(pListener != null)
					pListener.onImageDownloaded(pBitmap);
			}
			@Override
			public void onImageDownloadCanceled() { 
				if(pListener != null)
					pListener.onImageDownloadCanceled();
			}
		};
		if(images.containsKey(url)){
			if(pListener != null)
				pListener.onImageDownloaded(images.get(url));
		}
		else
			//Try to download the desired picture
			(new ImageDownloader()).execute(
					new ImageDownloaderPacker(
							url, listner));
	}
	
}
