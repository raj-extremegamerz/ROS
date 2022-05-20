package com.raj.ROS;
import android.os.*;
import android.util.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

public class ROSDownloader extends AsyncTask
	{
		private String url;
		private String dest;
		public ROSDownloader(String url, String destination){
			this.url = url; this.dest = destination;
		}
		@Override
		protected Object doInBackground ( Object[] p1 )
			{
				try{
						URL url = new URL(this.url);
						InputStream in = url.openStream();
						Files.copy(in,Paths.get(dest));
					}
				catch (MalformedURLException | IOException e)
					{Log.e("ROSDOWNLOADER",e.toString()); return -1;}
				return null;
			}
		
}
