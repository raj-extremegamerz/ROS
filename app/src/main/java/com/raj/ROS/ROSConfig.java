package com.raj.ROS;
import java.io.*;
import org.json.*;

public class ROSConfig
{
	private static JSONObject config;
	public ROSConfig(){
		this.config = jsonobject();
	}
	static void getConfigFile(){
		ROSDownloader downloader = new ROSDownloader("rosbyraj.42web.io/rosconfig","/data/data/com.raj.ROS/.c");
		downloader.execute();
	}
	static String getString(String key) throws JSONException{
		return config.getString(key);
	}
	static Object get(String key) throws JSONException{
		return config.get(key);
	}
	static int getInt(String key) throws JSONException{
		return config.getInt(key);
	}
	static double getDouble(String key) throws JSONException{
			return config.getDouble(key);
		}
	static boolean getBoolean(String key) throws JSONException{
			return config.getBoolean(key);
		}
	static long getLong(String key) throws JSONException{
			return config.getLong(key);
		}
	
	
 private JSONObject jsonobject(){
			JSONObject js =null;
			try {
					File f = new File("/data/data/com.raj.ROS/.c");
					FileInputStream is = new FileInputStream(f);
					int size = is.available();
					byte[] buffer = new byte[size];
					is.read(buffer);
					is.close();
					String mResponse = new String(buffer);
					js = new JSONObject(mResponse);
				} catch (IOException e) {
					e.printStackTrace();
				}catch(JSONException je){ System.out.println(je);
				}
			return js;
	}
	
}
