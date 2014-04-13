package com.wabbit.libraries.remoting;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.facebook.widget.WebDialog.RequestsDialogBuilder;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.abstracts.ParseMemCache;
import com.wabbit.imagesutils.ImageDownloader;
import com.wabbit.imagesutils.ImageDownloader.IOnImageDownloadListener;
import com.wabbit.imagesutils.ImageDownloader.ImageDownloaderPacker;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FBMgr {
	public static String TYPE_SMALL = "square";
	public static String TYPE_MEDIUM = "normal";
	public static String TYPE_LARGE = "large";
	
	private static FBMgr instance;
	
	private GraphUser mUser;
	private List <GraphUser> mFriends;
	
	private HashMap <String, Bitmap> mProfilePictures = new HashMap<String, Bitmap>();
	
	public static synchronized FBMgr gi() {
        if (instance == null) {
            instance = new FBMgr();
        }

        return instance;
    }
	
	private FBMgr() {
	}
	 
	public void destroy() {
        instance = null;
    }
	
	public void writeStringAsFile(final String fileContents, String fileName) {
        //Context context = App.instance.getApplicationContext();
        try {
            FileWriter out = new FileWriter(new File(fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            //Logger.logError(TAG, e);
        }
    }

    public void generetaKey(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.wabbit.messenger",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
	public void fbLogin(Activity activity, final Runnable pCallback){
		Session.openActiveSession(activity, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					// make request to the /me API
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							mUser = user;
							
							if(pCallback != null)
								pCallback.run();
						}
					});
				}
			}
		});
	}

    private boolean hasPhotoPermissions() {
        Session session = Session.getActiveSession();
        return session.getPermissions().contains("user_photos");
    }
    public void requestPhotoPermissions(Activity act, Session session){
        session.requestNewPublishPermissions(new Session.NewPermissionsRequest(act, "user_photos"));
    }

    public void meRequest(final Runnable pCallback){
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                mUser = user;
                if(pCallback != null)
                    pCallback.run();
            }
        }).executeAsync();
    }

    public void getPhotos(){
        new Request(
                ParseFacebookUtils.getSession(),
                "/me/photos",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        GraphObject obj = response.getGraphObject();
                        JSONArray array =(JSONArray) obj.getProperty("data");
                        //Only save valid photo array
                        if(array != null && array.length() > 0) {
                            ParseUser.getCurrentUser().put(Enums.ParseKey.USER_FB_PHOTOS, array.toString());
                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    }
                }
        ).executeAsync();
    }
	
	public void fbLogout(){
		Session.getActiveSession().closeAndClearTokenInformation();
	}
	
	public void downloadListOfFriends(final Runnable pCallback){
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                //mFriends = users;
                mFriends = new ArrayList<GraphUser> ();
                mFriends.add(getUser());
                mFriends.addAll(users);

                if(pCallback != null)
                    pCallback.run();
            }
        });
	}
	
	public void downloadProfilePicture(final String pUserId, final String pType, final boolean forceDownload, final IOnImageDownloadListener pListener){
		if(forceDownload == true || (forceDownload == false && mProfilePictures.get(pUserId + pType) == null)){
			//Try to put a lower resolution picture there, before the download ends
			if(pType.equals(TYPE_MEDIUM)){
				if(pListener != null && mProfilePictures.get(pUserId + TYPE_SMALL) != null)
					pListener.onImageDownloaded(mProfilePictures.get(pUserId + TYPE_SMALL));
			}
			else
				if(pType.equals(TYPE_LARGE)){
					if(pListener != null && mProfilePictures.get(pUserId + TYPE_MEDIUM) != null)
						pListener.onImageDownloaded(mProfilePictures.get(pUserId + TYPE_MEDIUM));
					else
						if(pListener != null && mProfilePictures.get(pUserId + TYPE_SMALL) != null)
							pListener.onImageDownloaded(mProfilePictures.get(pUserId + TYPE_SMALL));
				}
			//Try to download the desired picture
			(new ImageDownloader()).execute(
					new ImageDownloaderPacker(
							"https://graph.facebook.com/" + pUserId + "/picture?type=" + pType,
							new IOnImageDownloadListener() {
								@Override
								public void onImageDownloaded(Bitmap pBitmap) {
									//Store picture for later use
									mProfilePictures.put(pUserId + pType, pBitmap);
									
									if(pListener != null)
										pListener.onImageDownloaded(pBitmap);
								}
								@Override
								public void onImageDownloadCanceled() {
									if(pListener != null)
										pListener.onImageDownloadCanceled();
								}
							}));
		}
		else{
			if(pListener != null)
				pListener.onImageDownloaded(mProfilePictures.get(pUserId + pType));
		}
	}
	
	public void downloadProfilePicture(final String pUserId, final String pType){
		downloadProfilePicture(pUserId, pType, true, null);
	}
	

	public  void sendRequestDialog(final Activity activity, final String pToUserId) {
        activity.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Bundle params = new Bundle();
			    params.putString("to", pToUserId);
			    
			    RequestsDialogBuilder dialog = new RequestsDialogBuilder(activity,
			            Session.getActiveSession(),
			            params);
			    /*dialog.setLink("https://play.google.com/store/apps/details?id=com.yoero.puzzle.arukone.flow.free");
			    dialog.setDescription("descriptie");
			    dialog.setCaption("captie");
			    dialog.setName("Bingo Free name");
			    */
			    dialog.setTitle("Bingo Run Free");
			    //dialog.setData("Bogdan tirca");
			    dialog.setMessage("Join me on Bingo Free! I'm having a great time playing it!");
			    
			    WebDialog requestsDialog = dialog.build();
			    requestsDialog.setOnCompleteListener(new OnCompleteListener() {
	                @Override
	                public void onComplete(final Bundle values, final FacebookException error) {
							if (error != null) {
		                        if (error instanceof FacebookOperationCanceledException) {
		                            Toast.makeText(activity.getApplicationContext(),
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(activity.getApplicationContext(),
		                                "Network Error", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    } else {
		                        final String requestId = values.getString("request");
		                        if (requestId != null) {
		                            Toast.makeText(activity.getApplicationContext(),
		                                "Request sent",  
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(activity.getApplicationContext(),
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    }
	                }
              });
			                    
			    requestsDialog.show();
			}
			
		});
	    
	}
	
	public GraphUser getUser() {
		return mUser;
	}
	
	//Not to be used many times. O(N)
	public GraphUser getFriendById(final String pId){
		for(GraphUser user : mFriends)
			if(user.getId().equals(pId))
				return user;
		return null;
	}
	
	public List <GraphUser> getListOfFriends(){
		return mFriends;
	}
	
	public int getNumberOfFriends(){
		return mFriends == null ? 0 : mFriends.size();
	}
	
	public String [] getFriendsIdFrom(final int pIndex, final int pCount){
		if(pCount < 1 || mFriends == null)
			return null;
		String res[] = new String[ Math.min(pCount, mFriends.size() - pIndex) ];
		for(int i = pIndex; i < pIndex + Math.min(pCount, mFriends.size() - pIndex); i ++){
			res[i - pIndex] = mFriends.get(i).getId();
		}
		return res;	
	}
//	public Bitmap getProfilePicture(final String pUserId, final String pType){
//		return mProfilePictures.get(pUserId + pType);
//	}
	
	public boolean isLogedIn(){
		return mUser != null;
	}

    public String getLinkToProfilePictureByFbId(String userid, int width, int height){
        return "https://graph.facebook.com/"  + userid + "/picture?width=" + width + "&height=" + height;
    }
    public String getLinkToProfilePictureByParse(ParseUser user, int width, int height){
        String fbid = user.getString(Enums.ParseKey.USER_FBID);
        return FBMgr.gi().getLinkToProfilePictureByFbId(fbid, width, height);
    }
    public String getLinkToProfilePictureByFbId(String userid){
        return "https://graph.facebook.com/"  + userid + "/picture?type=large";
    }
    public String getLinkToMyProfilePicture(){
        return "https://graph.facebook.com/"  + getUser().getId() + "/picture?type=large";
    }

    public String getLinkToProfilePictureByParseId(String parseId){
        final ParseUser user = ParseMemCache.gi().getUser(parseId, false);
        if(user == null)
            return "";
        final String fbid = user.getString(Enums.ParseKey.USER_FBID);
        return FBMgr.gi().getLinkToProfilePictureByFbId(fbid);
    }

    public String getLinkToProfilePictureByParse(ParseUser user){
        String fbid = user.getString(Enums.ParseKey.USER_FBID);
        return FBMgr.gi().getLinkToProfilePictureByFbId(fbid);
    }


}
