package com.raj.ROS;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.io.*;
import java.nio.charset.*;

import android.view.View.OnClickListener;
public class MainActivityTerminal extends Activity 
{
	public static Bundle user;
	private EditText command;
	static TextView commandoutput;
	public static String cmdout, LOG_TAG;
	public static boolean internet;
	private static String packn;
	//private static ROSLoger logger;
	/*private static ROSService service;
	private static Terminal Terminal;*/
	private static java.lang.Process p;
	private static OutputStream writer;
	private static InputStream outputreader, errorreader;
	private static boolean autoFlush, autoClose;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		packn = getPackageName();
        setContentView(R.layout.mainterminal);
		StartTerminal();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		findlayout(); setll(); 
		user =getIntent().getExtras();
		keydone();
		LOG_TAG = "TERMINAL";
			//service = desktop.service;
			//logger = service.getDefaultLogger();
			//Terminal = service.getDefaultTerm();
			commandoutput.append("\n"+"["+getJSOnUser.getusername()+"@"+getJSOnUser.getBrand()+"]"+"\r");
	}
	private void keydone(){
		command.setOnEditorActionListener(new TextView.OnEditorActionListener(){
				@Override
				public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
				{
					if(p2 == EditorInfo.IME_ACTION_DONE){
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(command.getWindowToken(),0);
						final String cmd = command.getText().toString();
						try{
						//logger.log(ROSService.TAG_TERMINAL,cmd,"INFO");
						if(cmd == null | cmd.length() ==0){consoleWrite("");}else{
						cmdput(cmd);
						writeToTerminal(cmd);
						read();
						writeToTerminal("echo --EOF--");
						consoleWrite("");
						}
						command.setText("");
						}catch(Exception e){outputWrite(e.toString());}
						return true;
					}
					return false;
					
				}
		});
		commandoutput.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(command,0);
				}
		});
		/*command.addTextChangedListener(new TextWatcher(){

				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
				{
					// TODO: Implement this method
				}

				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
				{
					commandoutput.append(p1);
				}

				@Override
				public void afterTextChanged(Editable p1)
				{
					// TODO: Implement this method
				}		
		});*/
	}
	
	public void consoleWrite(String output){
		commandoutput.append("\n"+"["+getJSOnUser.getusername()+"@"+getJSOnUser.getBrand()+"]"+"\r"+output);
		final int scrollAmount = commandoutput.getLayout().getLineTop(commandoutput.getLineCount()) - commandoutput.getHeight();
		if (scrollAmount > 0)
			commandoutput.scrollTo(0, scrollAmount);
		else
			commandoutput.scrollTo(0, 0);
	}
	private void outputWrite(String output){
		commandoutput.append("\n"+output);
		final int scrollAmount = commandoutput.getLayout().getLineTop(commandoutput.getLineCount()) - commandoutput.getHeight();
		if (scrollAmount > 0)
			commandoutput.scrollTo(0, scrollAmount-1);
		else
			commandoutput.scrollTo(0, 0);
	}
	private void cmdput(String p){
		commandoutput.append(p);
	}
	private void findlayout(){
		command = findViewById(R.id.mainEditTextcommand);
		commandoutput = findViewById(R.id.mainTextViewoutput);
		commandoutput.setTypeface(new Typeface().createFromAsset(getAssets(),"font.ttf"));
		command.setTypeface(new Typeface().createFromAsset(getAssets(),"font.ttf"));
		commandoutput.append("WELCOME TO ROS "+"\nFOR BUGS, REPORT ON GITHUB PAGE");
		commandoutput.setMovementMethod(new ScrollingMovementMethod());
	}
	private void setll(){
		Display d = getWindowManager().getDefaultDisplay(); Point size = new Point(); d.getSize(size);
		LinearLayout ll =findViewById(R.id.mainLinearLayouttermoutput);
		LayoutParams params = (LayoutParams) ll.getLayoutParams();
		params.height = size.x / 2;
		ll.setLayoutParams(params);
	}
	
	void StartTerminal(){
		try{
			/*ProcessBuilder pb = new ProcessBuilder("/system/bin/sh -");
			pb.directory(new File(ROSService.HOME_PATH));
			//File file = new File(ROSService.EXTERNAL_DATA+".out");
			pb.redirectErrorStream(true);*/
			p = Runtime.getRuntime().exec("/system/bin/sh -", BackgroundJob.buildEnvironment(true, ROSService.HOME_PATH), new File(ROSService.HOME_PATH));
			outputreader = p.getInputStream();
			writer = p.getOutputStream();
			writer.write(("echo TerminalStarted\n").getBytes());
			writer.flush();
		}
		catch (IOException e)
		{Log.e("TERMINAL",e.toString());}
	}
	public String reader() throws FileNotFoundException, IOException{
		StringBuilder fileContents = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader((ROSService.EXTERNAL_DATA)+"/.out"))){
			for(String line; (line =br.readLine())!=null;){
				fileContents.append(line);
			}
		}
		return fileContents.toString();
	}
	void writeToTerminal(String cmd){
		try{
		p.getOutputStream();
		writer.write((cmd+"\n").getBytes());
			writer.flush();
		}
		catch (IOException e)
		{Log.e("Terminal",e.toString());}
	}
	void read(){
		new Thread(){
			@Override
			public void run(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
		String line;
		try {
			File f = new File(ROSService.EXTERNAL_DATA+"/.out");
			FileWriter writer = new FileWriter(f);
			// FIXME: Long lines.
			while ((line = reader.readLine()) != null) {
				writer.append(line+"\n"); writer.flush();
				//if(line.contains("--EOF--")|line.equals("--EOF--")|line.endsWith("--EOF--")) break;
			}
			reader.close();
			/*File f = new File(ROSService.EXTERNAL_DATA+"/.out");
			FileWriter writer = new FileWriter(f);
			writer.write(outputFiler.toString());
			writer.flush();*/
			writer.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error reading output", e);
		}
				 reader = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
				String lline;
				try {
					File f = new File(ROSService.EXTERNAL_DATA+"/.outt");
					FileWriter wwriter = new FileWriter(f);
					// FIXME: Long lines.
					while ((lline = reader.readLine()) != null) {
						wwriter.append(lline+"\n"); wwriter.flush();
						//if(lline.contains("--EOF--")|lline.equals("--EOF--")|lline.endsWith("--EOF--")) break;
					}
					reader.close();
					/*File f = new File(ROSService.EXTERNAL_DATA+"/.out");
					 FileWriter writer = new FileWriter(f);
					 writer.write(outputFiler.toString());
					 writer.flush();*/
					writer.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error reading output", e);
				}
		}
		}.start();
	}
}
