package com.suv.instavitancol;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageAdapter extends ArrayAdapter<String> {
	private String[] thumbnailURL;
	private LayoutInflater inflater;
	private String[] imagecaption;
	private String[] username;
	Context context;
    int screenwidth;
	public ImageAdapter(Context context, int textViewResourceId, String[] thumbnailURL2,
			String[] imagecaption2,int width,String[] username2) {
		super(context, textViewResourceId, thumbnailURL2);
		// TODO Auto-generated constructor stub
		inflater = ((Activity)context).getLayoutInflater();
		thumbnailURL = thumbnailURL2;
		imagecaption = imagecaption2;
		screenwidth = width;
		username = username2;
	}

	private static class ViewHolder {
		ImageView imageView;
		TextView captionView;
		TextView userView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.imagelist, null);
			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView)convertView.findViewById(R.id.image);
			LinearLayout.LayoutParams layoutParams  = new LinearLayout.LayoutParams(screenwidth, screenwidth);
			viewHolder.imageView.setLayoutParams(layoutParams);
			viewHolder.captionView = (TextView)convertView.findViewById(R.id.caption);
			viewHolder.userView = (TextView)convertView.findViewById(R.id.username);
			convertView.setTag(viewHolder);
		}

		viewHolder = (ViewHolder)convertView.getTag();
		//set image cache settings
		ImageLoader imageLoader = ImageLoader.getInstance();
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
							.cacheOnDisc(true).resetViewBeforeLoading(true)
							.showImageForEmptyUri(R.drawable.instavita)
							.showImageOnFail(R.drawable.instavita)
							.showImageOnLoading(R.drawable.instavita).build();	

		//download and display image from url
		imageLoader.displayImage(thumbnailURL[position],viewHolder.imageView, options);
		viewHolder.captionView.setText(imagecaption[position]);
		viewHolder.userView.setText(username[position]);
		
		return convertView;
	}
	
}