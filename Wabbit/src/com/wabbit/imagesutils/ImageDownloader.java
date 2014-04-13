package com.wabbit.imagesutils;

import java.io.IOException;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.wabbit.imagesutils.ImageDownloader.ImageDownloaderPacker;

public class ImageDownloader extends AsyncTask<ImageDownloaderPacker, Integer, ImageDownloaderPacker> {

	    @Override
	    protected ImageDownloaderPacker doInBackground(ImageDownloaderPacker... packer) {
	    	if(isCancelled())
	    		return packer[0];
	    	
	    	try {
				packer[0].bmp = ImageUtils.downloadBitmap(packer[0].urlString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	        return packer[0];
	    }

	    @Override
	    protected void onProgressUpdate(Integer... values) {
	    }

	    @Override
	    protected void onPostExecute(ImageDownloaderPacker packer) {
	        if(packer.bmp == null)
	        	packer.listener.onImageDownloadCanceled();
	        else
	        	packer.listener.onImageDownloaded(packer.bmp);
	    }
	    
	    public static class ImageDownloaderPacker {
	    	public String urlString = null;
	    	public IOnImageDownloadListener listener = null;
	    	public Bitmap bmp = null;
	    	public ImageDownloaderPacker(final String urlString, final IOnImageDownloadListener listener){
	    		this.urlString = urlString;
	    		this.listener = listener;
	    	}
	    	
	    	public ImageDownloaderPacker(){
	    	}
	    }
	    public interface IOnImageDownloadListener{
	    	public void onImageDownloaded(final Bitmap pBitmap);
	    	public void onImageDownloadCanceled();
	    }
}
