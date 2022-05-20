package com.raj.ROS;

import android.util.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class Proot
{
		private static final String LOG_TAG = "ROSBackground";
		final Process mProcess;
		Runtime runtime;
		String output = "";
	public Proot(String[] args){
			runtime = Runtime.getRuntime();
			String[] env = BackgroundJob.buildEnvironment(false, ROSService.HOME_PATH);
			String[] progArray = null;
			final String processDescription = "PROOT";
			Process process;
			try {
					process = runtime.exec(args, env, new File(ROSService.HOME_PATH));
				} catch (IOException e) {
					mProcess = null;
					// TODO: Visible error message?
					Log.e(LOG_TAG, "Failed running background job: " + this.toString(), e);
					return;
				}
			mProcess = process;
			final int pid = BackgroundJob.getPid(mProcess);
			new Thread() {
					@Override
					public void run() {
					Log.i(LOG_TAG, "[" + pid + "] starting: " + processDescription);
							InputStream stdout = mProcess.getInputStream();
							BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
							String line;
							try {
									final FileWriter writer = new FileWriter(new File(ROSService.EXTERNAL_DATA+"/.outted"));
									// FIXME: Long lines.
									while ((line = reader.readLine()) != null) {
											Log.i(LOG_TAG, "[" + pid + "] stdout: " + line);
											writer.append("[" + pid + "] stdout: " + line+"\n");
										}
								} catch (IOException e) {
									Log.e(LOG_TAG, "Error reading output", e);
								}
							try {
									int exitCode = mProcess.waitFor();
									if (exitCode == 0) {
											Log.i(LOG_TAG, "[" + pid + "] exited normally");
										} else {
											Log.w(LOG_TAG, "[" + pid + "] exited with code: " + exitCode);
										}
								} catch (InterruptedException e) {
									// Ignore.
								}
						}
				}.start();
			new Thread() {
					@Override
					public void run() {
							InputStream stderr = mProcess.getErrorStream();
							BufferedReader reader = new BufferedReader(new InputStreamReader(stderr, StandardCharsets.UTF_8));
							String line;
							try {
									final FileWriter writer = new FileWriter(new File(ROSService.EXTERNAL_DATA+"/.outtederr"));
									// FIXME: Long lines.
									while ((line = reader.readLine()) != null) {
											Log.i(LOG_TAG, "[" + pid + "] stderr: " + line);
											writer.append(LOG_TAG+ " [" + pid + "] stderr: " + line +"\n");
										}
								} catch (IOException e) {
									// Ignore.
								}
						}
				};
	}
}
