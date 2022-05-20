package com.raj.ROS;

import android.annotation.*;
import android.os.*;
import android.util.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import android.app.*;

public class ROSService
{
	@SuppressLint("SdCardPath")
    public static final String FILES_PATH = "/data/data/com.raj.ROS/files";
	public static final String INSTALLERPATH = "/data/data/com.raj.ROS";
    public static final String PREFIX_PATH = FILES_PATH + "/usr";
    public static final String HOME_PATH = FILES_PATH + "/home";
	public static final String TAG_TERMINAL = "ROSTERMINAL";
	public static final String APP_PREFIX = "ROS";
	public static final String PACKAGE_PREFIX = "com.raj.ROS";
	public static final String EXTERNAL_DATA = Environment.getExternalStorageDirectory()+"/Android/data/"+ROSService.PACKAGE_PREFIX+"/files";
   	public static final String LOG_SEPERATOR = "|";
	public static final String BUSY_BIN=INSTALLERPATH+"/bin";
	public static final String BUSY_TMP=INSTALLERPATH+"/tmp";
	public static final String BUSY_LD_LIB=INSTALLERPATH+"/lib";
	public static final String BUSY_EXTRACTION_PATH=BUSY_LD_LIB;
	public static final String BUSY_INSTALLED_PATH=BUSY_BIN;
    private static Terminal defaultTerm;
	private static ROSFiler outputFiler;
	private static ROSLoger logger;
	public ROSService() throws Exception{
	}
	public static Terminal getDefaultTerm(){
		return defaultTerm;
	}
	public static ROSFiler getDefaultOutputFiler(){
		return outputFiler;
	}
	public static ROSLoger getDefaultLogger(){
		return logger;
	}
	public static void setDefaultTerm(Terminal terminal){
		defaultTerm = terminal;
	}
	public static void setDefaultOutputFiler(ROSFiler filer){
		outputFiler = filer;
	}
	public static void setDefaultLogger(ROSLoger loger){
		 logger = loger;
	}
	
	public void readTerm() throws IOException{
		String LOG_TAG = "Reader";
		BufferedReader reader = new BufferedReader(new InputStreamReader(getDefaultTerm().getInputStream(), StandardCharsets.UTF_8));
		BufferedReader erreader = new BufferedReader(new InputStreamReader(getDefaultTerm().getErrorStream(), StandardCharsets.UTF_8));
		String line;
		ROSFiler filer = new ROSFiler(new File(EXTERNAL_DATA+"/.outted"));
		try {
			// FIXME: Long lines.
			while ((line = reader.readLine()) != null) {
				filer.out(line+"\n");
				if(line.contains("--EOF--")|line.equals("--EOF--")|line.endsWith("--EOF--")) break;
			}
			while ((line = erreader.readLine()) != null) {
				filer.out(line+"\n");
				if(line.contains("--EOF--")|line.contains("1")|  line.equals("--EOF--")|line.endsWith("--EOF--")) break;
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error reading output", e);
		}finally{
			reader.close();
		}
	}
	static String[] buildEnvironment(){
		new File("/data/data/com.raj.ROS/localfiles/usr").mkdirs();
		new File("/data/data/com.raj.ROS/localfiles/home").mkdirs();
		List<String> environment =  new ArrayList<>();
		environment.add("SHELL=/data/data/com.raj.ROS/bin/bash");
		environment.add("COLORTERM=truecolor");
		environment.add("PREFIX=/data/data/com.raj.ROS/localfiles/usr");
		environment.add("PWD=/data/data/com.raj.ROS/home");
		environment.add("EXTERNAL_STORAGE="+System.getenv("EXTERNAL_STORAGE"));
		environment.add("LD_PRELOAD=/data/data/com.raj.ROS/localfiles/usr/lib/libros.so");
		environment.add("HOME=/data/data/com.raj.ROS/localfiles/home");
		environment.add("LANG=en_US.UTF-8");
		environment.add("TMPDIR=/data/data/com.raj.ROS/tmp");
		environment.add("BOOTCLASSPATH" + System.getenv("BOOTCLASSPATH"));
		environment.add("ANDROID_ROOT=" + System.getenv("ANDROID_ROOT"));
		environment.add("ANDROID_DATA=" + System.getenv("ANDROID_DATA"));
		environment.add("TERM=xterm-256color");
		environment.add("LD_LIBRARY_PATH=/data/data/com.raj.ROS/localfiles/usr/lib");
		environment.add("PATH=/data/data/com.raj.ROS/bin:/data/data/com.raj.ROS/localfiles/usr/bin:/data/data/com.raj.ROS/localfiles/usr/applets");
		return environment.toArray(new String[0]);
	}
	static String[] buildBusybox(){
			new File(ROSService.HOME_PATH).mkdirs();
			String cwd = ROSService.HOME_PATH;
			new File(BUSY_TMP).mkdirs();
			List<String> environment = new ArrayList<>();
			environment.add("TERM=xterm-256color");
			environment.add("ROS_VERSION=1.0_git-21");
			environment.add("HOME=" + ROSService.HOME_PATH);
			environment.add("PREFIX=" + INSTALLERPATH);
			environment.add("BOOTCLASSPATH" + System.getenv("BOOTCLASSPATH"));
			environment.add("ANDROID_ROOT=" + System.getenv("ANDROID_ROOT"));
			environment.add("ANDROID_DATA=" + System.getenv("ANDROID_DATA"));
			environment.add("EXTERNAL_STORAGE=" + System.getenv("EXTERNAL_STORAGE"));
			addToEnvIfPresent(environment, "ANDROID_RUNTIME_ROOT");
			addToEnvIfPresent(environment, "ANDROID_TZDATA_ROOT");
			environment.add("LD_LIBRARY_PATH=" + BUSY_LD_LIB );
			environment.add("LANG=en_US.UTF-8");
			environment.add("PATH=" + BUSY_BIN + ":" + BUSY_BIN + "/applets");
			environment.add("PWD=" + cwd);
			environment.add("TMPDIR=" + BUSY_TMP);
			return environment.toArray(new String[0]);
	}
		private static void addToEnvIfPresent(List<String> environment, String name) {
				String value = System.getenv(name);
				if (value != null) {
						environment.add(name + "=" + value);
					}
			}
}
