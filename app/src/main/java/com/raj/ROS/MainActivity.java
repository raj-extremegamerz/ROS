package com.raj.ROS;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.wifi.*;
import android.os.*;
import android.system.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;

public class MainActivity extends Activity
{
	public static boolean internet;
	public static ProgressDialog filedownloaddialog;
	EditText name ,password;
	Button login;
	public static String un;
	private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
	private static ROSLoger log;
	public static ROSService service;
	private static ROSFiler filer;
	private static Terminal term;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
       setTitle("ROS Login");
		setContentView(R.layout.main);
		if(isuempty()){
				try{
						InstallProot();
						Intent i = new Intent(MainActivity.this,ROSInstaller.class);
						startActivity(i);
						MainActivity.this.finish();
					}
				catch (Exception e)
					{makeText(e.toString(),Toast.LENGTH_LONG);}

			}
		try{
			if(!isuempty()){
			 service = new ROSService();
			 term = new Terminal();
			 term.writeString("touch "+ROSService.EXTERNAL_DATA+"created");
			 term.flush();
			 term.writeString("/data/data/com.raj.ROS/proot/proot --help >> " + ROSService.EXTERNAL_DATA+"/.out");
			 term.flush();
			 new Terminal().writeString("/system/bin/logcat -f "+ROSService.EXTERNAL_DATA+"/log/log.txt");
			ROSConfig config = new ROSConfig();
			config.getConfigFile();
			 new File(ROSService.EXTERNAL_DATA).mkdirs();
			 new File(ROSService.EXTERNAL_DATA+"/.out").createNewFile();
			 filer = new ROSFiler(new File(ROSService.EXTERNAL_DATA+"/.out"));
			 log = new ROSLoger();
			 service.setDefaultOutputFiler(filer);
			 service.setDefaultTerm(term);
			 service.setDefaultLogger(log);
			// Proot  p= new Proot(new String[]{"/data/data/com.raj.ROS/sh/busybox","sh"," -c"," ./data/data/com.raj.ROS/sh/proot >> /storage/emulated/0/Android/data/com.raj.ROS/p"});
			// exec();
			}
		}
		catch (IOException | Exception /*| InterruptedException /*|ErrnoException*/ e)
		{Log.e("EXECMD",e.toString()); e.printStackTrace();}
		findlayout();
		checkinternetconnection ic = new checkinternetconnection();
		ic.execute();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ROS");
		mWakeLock.acquire();
		// http://tools.android.com/tech-docs/lint-in-studio-2-3#TOC-WifiManager-Leak
		WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ROS");
		mWifiLock.acquire();
		login.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if(!(name.getText().length()==0)){
						if(!(password.getText().length()==0)){
					String n = name.getText().toString();
					String p = password.getText().toString();
							try{
							final JSONObject jo = getJSOnUser.jo(n);
							if(jo.getString("name").equals(n)){
								if(jo.getString("password").equals(p)){
							getJSOnUser.setUsername(n);
							//"--link2symlink"," -0"," -r debian-fs"," -b /dev " ,"-b /proc"," -b debian-fs/root:/dev/shm"," -b /sdcard","-b /storage/sdcard1/", "-w /root /usr/bin/env -i"  ,"HOME=/root", "PATH=/usr/local/sbin:/usr/local/bin:/bin:/usr/bin:/sbin:/usr/sbin:/usr/games:/usr/local/games"," TERM=$TERM"," LANG=C.UTF-8" ,"/bin/echo hellodebian"});
									Intent i = new Intent(MainActivity.this, desktop.class);
									startActivity(i);
						finish();
						}else{makeText("Worng Password",Toast.LENGTH_SHORT);}
					}else{makeText("Worng Username",Toast.LENGTH_SHORT);}
				}catch(JSONException|NullPointerException e){Toast.makeText(MainActivity.this,"WORNG USERNAME OR PASSWORD",Toast.LENGTH_SHORT).show();}
							
				}else{
					Toast.makeText(MainActivity.this,"PLEASE FILL PASSWORD",Toast.LENGTH_SHORT).show();
				}
				}else{
					Toast.makeText(MainActivity.this,"PLEASE FILL USERNAME",Toast.LENGTH_SHORT).show();
				}
			}
		});
		}
		private Boolean isuempty(){
			Boolean isempty = true;
				File f = new File("/data/data/com.raj.ROS/.u");
				isempty = !f.exists();
			return isempty;
		}
		private void findlayout(){
			name = findViewById(R.id.mainEditTextName);
			password = findViewById(R.id.mainEditTextpassword);
			login = findViewById(R.id.mainButtonlogin);
		}
		public  void makeText(String message,int ToastLength){
			Toast.makeText(this,message,ToastLength).show();
		}
	/*public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}*/
	private void InstallProot() throws IOException, ErrnoException, Exception{
		//if(getAssets().open("ROS_aarch64.zip")> ){
		new File(service.EXTERNAL_DATA).mkdirs();
		new File(service.FILES_PATH).mkdirs();
		new File(service.BUSY_BIN).mkdirs();
		new File(service.BUSY_EXTRACTION_PATH).mkdirs();
		final String STAGING_PREFIX_PATH = "/data/data/";
		final byte[] buffer = new byte[8096];
		try {
		ZipInputStream zipInput = new ZipInputStream(getAssets().open("ROS_aarch64.zip"));
			ZipEntry zipEntry;
			while ((zipEntry = zipInput.getNextEntry()) != null) {
				String zipEntryName = zipEntry.getName();
				File targetFile = new File(STAGING_PREFIX_PATH, zipEntryName);
				boolean isDirectory = zipEntry.isDirectory();
				ensureDirectoryExists(isDirectory ? targetFile : targetFile.getParentFile());
				if (!isDirectory) {
					try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
						int readBytes;
						while ((readBytes = zipInput.read(buffer)) != -1)
							outStream.write(buffer, 0, readBytes);
					}
						Os.chmod(targetFile.getAbsolutePath(), 0755);
				}
			}
			java.lang.Process p = Runtime.getRuntime().exec("/data/data/com.raj.ROS/bin/busybox --install -s /data/data/com.raj.ROS/bin",service.buildBusybox(),new File(service.FILES_PATH));
			Log.d("BUSYBOX INSTALATION","p.waitFor(); "+p.waitFor());
			for(File f : new File("/data/data/com.raj.ROS/bin").listFiles()){
				Log.d("ROS BUSY",f.getName() +" can Excute -" +f.canExecute()+" can Read -" +f.canRead()+" can Write - "+f.canWrite());
			}
			}catch(Exception e){
			e.printStackTrace();
			throw new Exception(e); 
		}
	}
	private void ensureDirectoryExists(File directory) {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new RuntimeException("Unable to create directory: " + directory.getAbsolutePath());
		}
	}
	private String archName() {
		for (String androidArch : Build.SUPPORTED_ABIS) {
			switch (androidArch) {
				case "arm64-v8a": return "aarch64";
				case "armeabi-v7a": return "arm";
				case "x86_64": return "x86_64";
				case "x86": return "i686";
			}
		}
		throw new RuntimeException("Unable to determine arch from Build.SUPPORTED_ABIS =  " +
								   Arrays.toString(Build.SUPPORTED_ABIS));
	}
	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
	private void exec() throws IOException{
		term.writeString("mkdir /storage/emulated/0/test");
		term.flush();
		term.writeString("echo hello from ROS");
		term.flush();
		term.writeString("echo 'helloros' >> /storage/emulated/0/foo.txt");
		term.flush();
		term.writeString("touch /storage/emulated/0/tes.txt");
		term.flush();
		term.writeString("./data/data/com.raj.ROS/sh/proot");
		term.flush();
		/*term.writeString("echo --EOF--");
		term.flush();*/
		service.readTerm();
	}
	public class checkinternetconnection extends AsyncTask
	{
		@Override
		protected Object doInBackground(Object[] p1)
		{
			HttpURLConnection testurlconntion = null;
			try		{
				try
				{
					URL url = new URL("https://google.com");
					testurlconntion = (HttpURLConnection) url.openConnection();
					testurlconntion.connect();
					MainActivity.internet = true;
					testurlconntion.disconnect();
				}
				catch (MalformedURLException e)
				{
					System.out.println(e);
					MainActivity.internet = false;
				}
				}catch (IOException e){
				System.out.println(e);
				MainActivity.internet = false;
		}
			return null;
		}}}
		
	
