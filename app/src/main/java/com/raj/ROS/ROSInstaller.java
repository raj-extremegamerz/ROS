package com.raj.ROS;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.system.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;
//import android.support.annotation.*;

public class ROSInstaller extends Activity
{
	private static Button instStart , selnext, downnext,usrdone;
	CheckBox linux,windows;
	TextView totaldown,progTextview;
	LinearLayout startll,secllayout,downlayout,usrlayout;
	EditText username,password;
	ProgressBar progress;
	boolean startDownload,downloadlinux;
	public static boolean internet;
	public static int exitCode;
	public static boolean downDone;
	boolean torf;
	downloadSystem dS;
	//rivate static ROSLoger log;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		startDownload = false;
		downloadlinux = false;
		final Context context = this;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.installer);
		progress = findViewById(R.id.installerProgressBar);
		instStart = findViewById(R.id.installerButton1next);
		selnext = findViewById(R.id.installerButtonSelectornext);
		downnext = findViewById(R.id.installerButtonDownDone);
		usrdone = findViewById(R.id.installerButtonInstallerDone);
		linux = findViewById(R.id.installerCheckBoxlinux);
		windows = findViewById(R.id.installerCheckBoxwindows);
		windows.setEnabled(false);
		totaldown = findViewById(R.id.installerTextViewDSize);
		progTextview = findViewById(R.id.installerTextViewDownload);
		startll = findViewById(R.id.installerLinearLayout1);
		secllayout = findViewById(R.id.installerLinearLayoutseclector);
		downlayout = findViewById(R.id.installerLinearLayoutdownller);
		usrlayout = findViewById(R.id.installerLinearLayoutCUser);
		username = findViewById(R.id.installerEditTextUsername);
		password = findViewById(R.id.installerEditTextPassword);
		startll.setVisibility(View.VISIBLE);
		secllayout.setVisibility(View.GONE);
		downlayout.setVisibility(View.GONE);
		usrlayout.setVisibility(View.GONE);
		downnext.setEnabled(false);
		progress.setIndeterminate(false);
		progress.setMax(100);
		checkinternetconnection ob = new checkinternetconnection();
		ob.execute();
		instStart.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					startll.setVisibility(View.GONE);
					secllayout.setVisibility(View.VISIBLE);
					downlayout.setVisibility(View.GONE);
					usrlayout.setVisibility(View.GONE);
					progress.setProgress(progress.getProgress()+25);
				}
		});
		selnext.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if(!downloadlinux){
						startll.setVisibility(View.GONE);
						secllayout.setVisibility(View.GONE);
						downlayout.setVisibility(View.GONE);
						usrlayout.setVisibility(View.VISIBLE);
						progress.setProgress(progress.getProgress()+50);
						}else{
							startll.setVisibility(View.GONE);
							secllayout.setVisibility(View.GONE);
							downlayout.setVisibility(View.VISIBLE);
							usrlayout.setVisibility(View.GONE);
					 dS = new downloadSystem();
					 dS.execute();
					progress.setProgress(progress.getProgress()+25);
					}
					
				}
		});
		downnext.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					startll.setVisibility(View.GONE);
					secllayout.setVisibility(View.GONE);
					downlayout.setVisibility(View.GONE);
					usrlayout.setVisibility(View.VISIBLE);
					progress.setProgress(progress.getProgress()+25);
				}
		});
		usrdone.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					startll.setVisibility(View.GONE);
					secllayout.setVisibility(View.GONE);
					downlayout.setVisibility(View.GONE);
					if(!username.getText().equals("")| !username.getText().toString().isEmpty()| !password.getText().equals("")| !password.getText().toString().isEmpty()){
					try {
						JSONObject jsonwrite = new JSONObject();
						jsonwrite.put("name",username.getText());
						jsonwrite.put("password",password.getText());
						FileWriter file = new FileWriter("/data/data/com.raj.ROS/.u");
						file.write(jsonwrite.toString());
						file.flush();
						file.close();
						Toast.makeText(context,"EveryThing Was Done \nNow\n Enjoy", Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(context,"Error While Setting Up System", Toast.LENGTH_LONG).show();
					}catch(JSONException je){
						Toast.makeText(context,"Error While Setting Up System", Toast.LENGTH_LONG).show();
						System.out.println(je);
					}finally{
						progress.setProgress(progress.getProgress()+25);
						Intent i = new Intent(ROSInstaller.this,MainActivity.class);
						startActivity(i);
						ROSInstaller.this.finish();
					}
					}else Toast.makeText(ROSInstaller.this,"Fill all details",Toast.LENGTH_SHORT).show();
				}
		});
		linux.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					if(linux.isChecked()){
						downloadlinux = true;
						totaldown.setText("TOTAL DOWNLOAD SIZE ~ 500MB");
					}else{
						downloadlinux = false;
						totaldown.setText("NOTHING TO DOWNLOAD ADDLIONAL PACKAGE");
					}
				}
		});

	}
	private class downloadSystem extends AsyncTask
	{
		public int returni = 0;
		private String curentFile;
		public downloadSystem(){
		}
		@Override
		//@WorkerThread
		protected Object doInBackground(Object[] p1)
		{
					try {
						final String STAGING_PREFIX_PATH = ROSService.INSTALLERPATH+"/files-staging";
						final File STAGING_PREFIX_FILE = new File(STAGING_PREFIX_PATH);
						final byte[] buffer = new byte[8096];
						final URL zipUrl = determineZipUrl();
						try (ZipInputStream zipInput = new ZipInputStream(zipUrl.openStream())) {
							ZipEntry zipEntry;
							while ((zipEntry = zipInput.getNextEntry()) != null) {
                                String zipEntryName = zipEntry.getName();
								MainActivity.service.getDefaultLogger().log("Zip Output",zipEntryName,"INFO");
                                File targetFile = new File(STAGING_PREFIX_PATH, zipEntryName);
                                boolean isDirectory = zipEntry.isDirectory();
                                ensureDirectoryExists(isDirectory ? targetFile : targetFile.getParentFile());
                                if (!isDirectory) {
                                    try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
                                        int readBytes;
                                        while ((readBytes = zipInput.read(buffer)) != -1)
                                            outStream.write(buffer, 0, readBytes);
                                    }
                                    if (zipEntryName.startsWith("bin/") || zipEntryName.startsWith("libexec") || zipEntryName.startsWith("lib/apt/methods") || zipEntryName.startsWith("startdebian")) {
                                        //noinspection OctalInteger
                                        Os.chmod(targetFile.getAbsolutePath(), 0700);
                                    }
                               }
                            }
						}
						if (!STAGING_PREFIX_FILE.renameTo(new File("/data/data/com.raj.ROS/files"))) {
							System.out.println("Unable to rename staging folder");
							throw new RuntimeException("Unable to rename staging folder");
						}else{
							returni = 0;
							ROSInstaller.downnext.setEnabled(true);
						}
					} catch (final Exception e) {
						e.printStackTrace();
						System.out.println(e);
						returni = -1;
					}
					curentFile = "exit";
					return returni;
		}
		private void ensureDirectoryExists(File directory) {
			if (!directory.isDirectory() && !directory.mkdirs()) {
				throw new RuntimeException("Unable to create directory: " + directory.getAbsolutePath());
			}
		}
		
		/** Get bootstrap zip url for this systems cpu architecture. */
		private URL determineZipUrl() throws MalformedURLException {
			String arch = determineArchName();
			String url = "http://192.168.43.242:12345/default-"+arch+".zip";
			return new URL(url);
		}
		private String determineArchName() {
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
		public String progress(){
			return curentFile;
		}
		 void deleteFolder(File fileOrDirectory) throws IOException {
			if (fileOrDirectory.getCanonicalPath().equals(fileOrDirectory.getAbsolutePath()) && fileOrDirectory.isDirectory()) {
				File[] children = fileOrDirectory.listFiles();

				if (children != null) {
					for (File child : children) {
						deleteFolder(child);
					}
				}
			}

			if (!fileOrDirectory.delete()) {
				throw new RuntimeException("Unable to delete " + (fileOrDirectory.isDirectory() ? "directory " : "file ") + fileOrDirectory.getAbsolutePath());
			}
		}
			
	}
	private class checkinternetconnection extends AsyncTask
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
					ROSInstaller.internet = false;
				}
			}catch (IOException e){
				ROSInstaller.internet = false;
			}
			return null;
		}}
}
