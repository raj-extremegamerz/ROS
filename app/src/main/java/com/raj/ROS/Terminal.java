package com.raj.ROS;
import android.annotation.*;
import android.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;

public class Terminal 
{
	private static Process p;
	private static OutputStream writer;
	private static InputStream outputreader, errorreader;
	private static boolean autoFlush, autoClose;
	public Terminal()throws Exception{
		close();
		try{
			p = Runtime.getRuntime().exec("/data/data/com.raj.ROS/bin/sh", ROSService.buildEnvironment(), new File(ROSService.HOME_PATH));
			outputreader = p.getInputStream();
			writer = p.getOutputStream();
			autoFlush = false;
			autoClose = false;
			Log.wtf("TERMINAL","TERMINAL CREATION SUCCESS");
		}
		catch (IOException e)
		{throw new Exception(e);}
	}
	public static void useAutoFlush(@NonNull boolean useautoflush){
		autoFlush = useautoflush;
	}
	public static void autoClose(@NonNull boolean autoclose){
		autoClose = autoclose;
	}
	public static InputStream getInputStream(){
		return outputreader;
	}
	public static OutputStream getOutputStream(){
		return writer;
	}
	public static InputStream getErrorStream(){
		return errorreader;
	}
	public static void close() throws IOException{
		if(writer != null) writer.close(); writer =null;
		if(errorreader != null) errorreader.close(); errorreader =null;
		if(outputreader != null) outputreader.close(); outputreader = null;
	}
	public static  void flush() throws IOException{
		if(!autoFlush) writer.flush();
	}
	public static  boolean isrunning(){
		return writer != null && outputreader != null&&errorreader !=null;
	}
	public static int read(byte[] buffer, int start, int len) throws IOException {
		if (outputreader == null){
			throw new IOException("Terminal Closed Or Terminal Didn't started ever");
		}
		return outputreader.read(buffer, start, len);
	}
	public static int read(byte[] buffer) throws IOException {
		if (outputreader == null){
			throw new IOException("Terminal Closed Or Terminal Didn't started ever");
		}
		return outputreader.read(buffer);
	}
	public static int readerror(byte[] buffer, int start, int len) throws IOException {
		if (outputreader == null){
			throw new IOException("Terminal Closed Or Terminal Didn't started ever");
		}
		return outputreader.read(buffer, start, len);
	}
	public static int readerror(byte[] buffer) throws IOException {
		if (outputreader == null){
			throw new IOException("Terminal Closed Or Terminal Didn't started ever");
		}
		return outputreader.read(buffer);
	}
	public static void writeBuffer(byte[] buffer) throws IOException {
		if (writer != null)
			writer.write(buffer);
		 	//writer.write("echo --END--\n".getBytes());
		
		if(autoFlush) flush();
		//readTerm();
	}
	public static void writeString(String buffer) throws IOException {
		if (writer != null)
			writer.write((buffer+"\n").getBytes());
			//writer.write("echo --END--\n".getBytes());
			//closeOnExit(buffer);
		if(autoFlush) flush();
		//readTerm();
	}
	public static void writeInt(int c) throws IOException {
		if (writer != null)
			writer.write(c);
		  	//writer.write("echo --END--\n".getBytes());

		if(autoFlush) flush();
	//	readTerm();
	}
	public static void write(byte[] b,int off,int len) throws IOException{
		writer.write(b,off,len);
		//writer.write("echo --END--\n".getBytes());
		//closeOnExit(b.toString());
		if(autoFlush) flush();
		//readTerm();
	}
	public static int getShellPid(){
		return getPid(p);
	}
	public static String readTerm(){
		StringBuilder out = new StringBuilder();
		String LOG_TAG = "Reader";
		BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
		String line;
		try {
			// FIXME: Long lines.
			while ((line = reader.readLine()) != null ) {
				out.append(line);
				if(line.contains("--EOF--")|line.equals("--EOF--")|line.endsWith("--EOF--")) break;
			}
			reader.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error reading output", e);
		}/*finally{
			reader.close();
		}*/
		return out.toString();
	}
	private static void closeOnExit(String cmd) throws IOException{
		if(cmd == "exit"){
			if(autoClose){
				close();
			}
		}
	}
	/*public String readOutputFile() throws IOException{
		//readTerm();
		StringBuilder fileContents = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(ROSService.EXTERNAL_DATA+"/.out"))){
			for(String line; (line =br.readLine())!=null;){
				fileContents.append(line+"/n");
			}
		}
		return fileContents.toString();
	}*/
	private static int getPid(Process p) {
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            try {
                return f.getInt(p);
            } finally {
                f.setAccessible(false);
            }
        } catch (Throwable e) {
            return -1;
        }
    }
	
	private interface Killer {
		void killProcess(int pid);
	}

	private static class AndroidKiller implements Killer {
		@Override
		public void killProcess(int pid) {
			android.os.Process.killProcess(pid);
		}
	}
}
