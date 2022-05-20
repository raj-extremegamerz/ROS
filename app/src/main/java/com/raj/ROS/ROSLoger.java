package com.raj.ROS;
import android.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.text.*;
import java.util.*;

public class ROSLoger 
{
	private static FileWriter log;

	private String date;
	public ROSLoger() throws IOException{
			File logdir= new File(ROSService.EXTERNAL_DATA+"/log");
			logdir.mkdirs();
			File logfile = new File(logdir.getAbsolutePath()+"/log.txt");
			logfile.createNewFile();
			log = new FileWriter(logfile);
	}
	public void log(String Tag,String output,String Flag) throws IOException{
		log.write(loggerstr(Tag)+"STD"+Flag.toUpperCase()+"    >>    "+output+"\n\r");
		log.flush();
	}
	public void close()throws IOException{
		log.close();
	}
	private String loggerstr(String Tag){
		String output = "TAG    >>    "+ Tag+"      "+ROSService.LOG_SEPERATOR+"    "+"CURRENT TIME -: "+getTime()+"      "+ROSService.LOG_SEPERATOR+"    ";
		return output;
	}
	public String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm:ss aa");  //it will give you the date in the formate that is given in the image
		String datetime = dateformat.format(c.getTime()); // it will give you the date
		return datetime;
	}
}
