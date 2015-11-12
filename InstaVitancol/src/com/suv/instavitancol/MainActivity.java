package com.suv.instavitancol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.suv.instavitancollibrary.*;
import com.suv.instavitancollibrary.InstaVit.OAuthAuthenticationListener;

public class MainActivity extends Activity {

	private InstaVit vitApp;
	private Button connect,logout;
	private TextView usernameTextView;
	private LinearLayout connectedLayout;
	String[] thumbnail,imagecaption,username;
	int feedcount = 5;
	ListView list;
	     
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.main);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
     // UNIVERSAL IMAGE LOADER SETUP
     	DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
     				.cacheOnDisc(true).cacheInMemory(true)
     				.imageScaleType(ImageScaleType.EXACTLY)
     				.displayer(new FadeInBitmapDisplayer(300)).build();

     	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
     				getApplicationContext())
     				.defaultDisplayImageOptions(defaultOptions)
     				.memoryCache(new WeakMemoryCache())
     				.discCacheSize(100 * 1024 * 1024).build();

     	ImageLoader.getInstance().init(config);
     		// END - UNIVERSAL IMAGE LOADER SETUP
        
        
		vitApp = new InstaVit(this, ApplicationData.CLIENT_ID,ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
		vitApp.setListener(listener);
		connectedLayout = (LinearLayout)findViewById(R.id.connectedLayout);
		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
		connect = (Button) findViewById(R.id.connect);
		logout = (Button) findViewById(R.id.logout);
		list = (ListView) findViewById(R.id.list);
		/*list.setOnItemClickListener(new OnItemClickListener() {
 
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String Slecteditem= itemname[+position];
				Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();	
			}
		});
		*/
		
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//if (mApp.hasAccessToken()) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
							builder.setMessage("Disconnect from Instagram?")
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
											DialogInterface dialog, int id) {
											vitApp.resetAccessToken(); //delete access token
											connect.setText("Connect");
											connect.setVisibility(View.VISIBLE);
											//hide logout button, username and list
											logout.setVisibility(View.INVISIBLE);
											usernameTextView.setVisibility(View.INVISIBLE);
											list.setVisibility(View.INVISIBLE);
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					final AlertDialog alert = builder.create();
					alert.show();
			}
		});

		connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isNetworkAvailable()){
					vitApp.authorize(); //authourize user on instagram
					logout.setVisibility(View.VISIBLE);
					usernameTextView.setVisibility(View.VISIBLE);
					connect.setVisibility(View.INVISIBLE);
				}
				else Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
			}
		});
		
		
        if (isNetworkAvailable()){
        	if (vitApp.hasAccessToken()) { //load previous login credentials
        		usernameTextView.setText(vitApp.getUserName());
        		PreferenceManager.getDefaultSharedPreferences(this).edit().putString("usersname", vitApp.getUserName()).commit(); 
        		loadNewsFeed(); //access newsfeed on instagram, initially, load only 5 images/feed.
        		connect.setText("Disconnect");
        	}
        	
        	if (connect.getText().equals("Disconnect")){
    			logout.setVisibility(View.VISIBLE);
    			usernameTextView.setVisibility(View.VISIBLE);
    			connect.setVisibility(View.INVISIBLE);	
    		}else if (connect.getText().equals("Connect")){
    			logout.setVisibility(View.INVISIBLE);
    			usernameTextView.setVisibility(View.INVISIBLE);
    			connect.setVisibility(View.VISIBLE);
    		}
        	
        }else {
        	Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
        	String usersname = PreferenceManager.getDefaultSharedPreferences(this).getString("usersname", ""); //getting saved username
        	usernameTextView.setText(usersname);
        	loadImages(); //loading cache images from previous logged in
        	logout.setVisibility(View.INVISIBLE);
			usernameTextView.setVisibility(View.VISIBLE);
			connect.setVisibility(View.INVISIBLE);
        	
        }
	}

	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {
		@Override
		public void onSuccess() {
			usernameTextView.setText(vitApp.getUserName());
			connect.setText("Disconnect");
		}

		@Override
		public void onFail(String error) {
			Intent intent = new Intent(MainActivity.this,MainActivity.class);
		    startActivity(intent);
		}
	};
	
 public void loadImages(){
	 //load cache images and saved caption and usernames
	 //getting saved thumbnail, imagecaption, username from shared preferences
	 thumbnail =  loadArray("stringthumbnail", this); 
	 imagecaption = loadArray("stringimagecaption", this); 
	 username = loadArray("stringusername", this); 
	 
	 Point size = new Point();
	 WindowManager w = getWindowManager();
	 w.getDefaultDisplay().getSize(size);
	 int width = size.x;
	 ListView listView = (ListView)this.findViewById(R.id.list);
	 final ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.imagelist, thumbnail,imagecaption,width,username);
	 listView.setAdapter(imageAdapter);
}
 
 public void loadNewsFeed(){
	 //request for newsfeed
	 try {
			JSONObject jsonObject = new JSONObject(vitApp.newsfeedString(feedcount));
			JSONArray data = jsonObject.getJSONArray("data");
			thumbnail = new String[data.length()];
			imagecaption = new String[data.length()];
			username = new String[data.length()];
			for (int i = 0; i < data.length(); i++) {
			  JSONObject object = data.getJSONObject(i);
			  JSONObject images = object.getJSONObject("images");
			  JSONObject thumbnailobj = images.getJSONObject("low_resolution"); //getting low resolution images for mobile
			  String thumbnailURL = thumbnailobj.getString("url");
			  thumbnail[i] = thumbnailURL;
			  JSONObject caption = object.getJSONObject("caption"); //getting image caption
			  String captiontext = caption.getString("text"); 
			  JSONObject user = object.getJSONObject("user"); 
			  String usernameobj = user.getString("username"); //getting username
			  imagecaption[i] = captiontext;
			  username[i]= usernameobj;
			}
			//saving thumbnail, caption and username for local/cache images
			saveArray(thumbnail, "stringthumbnail", this);
			saveArray(imagecaption,"stringimagecaption", this); 
			saveArray(username,"stringusername", this); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
	 	Point size = new Point();
		WindowManager w = getWindowManager();
		w.getDefaultDisplay().getSize(size);
		int width = size.x;
		ListView listView = (ListView)this.findViewById(R.id.list);
		final ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.imagelist, thumbnail,imagecaption,width,username);
		listView.setAdapter(imageAdapter);
		listView.setSelectionFromTop(feedcount-5, 0);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
	        public void onScroll(AbsListView view,
	        		int firstVisibleItem, int visibleItemCount,
	        		int totalItemCount) {
				//Algorithm to check if the last item is visible or not
				final int lastItem = firstVisibleItem + visibleItemCount;
				if(lastItem == totalItemCount){                 
					// you have reached end of list, load more data  
						feedcount = feedcount+5;
						loadNewsFeed();
				}
			}	
		});
 }
 
 private boolean isNetworkAvailable() {
	 //check for network availability
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
}
 
//SAVE ARRAY in sharedpref
 public boolean saveArray(String[] array, String arrayName, Context mContext) {   
     SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);  
     SharedPreferences.Editor editor = prefs.edit();  
     editor.putInt(arrayName +"_size", array.length);  
     for(int i=0;i<array.length;i++)  
         editor.putString(arrayName + "_" + i, array[i]);  
     return editor.commit();  
 } 
 //LOAD ARRAY from sharedpref
 public String[] loadArray(String arrayName, Context mContext) {  
     SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);  
     int size = prefs.getInt(arrayName + "_size", 0);  
     String array[] = new String[size];  
     for(int i=0;i<size;i++)  
         array[i] = prefs.getString(arrayName + "_" + i, null);  
     return array;  
 } 

}