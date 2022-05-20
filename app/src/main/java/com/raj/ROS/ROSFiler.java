package com.raj.ROS;

import android.icu.text.*;
import android.icu.util.*;
import android.nfc.*;
import java.io.*;

public class ROSFiler
{
	private FileWriter writer;
	private File file;
	public ROSFiler(File file) throws IOException{
		file.createNewFile();
		writer = new FileWriter(file);
	}
	public void out(String output) throws IOException{
		writer.write(output+"\n");
		writer.flush();
	}
	public String read() throws FileNotFoundException, IOException{
		StringBuilder fileContents = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			for(String line; (line =br.readLine())!=null;){
				fileContents.append(line);
			}
		}
		return fileContents.toString();
	}
	public void close()throws IOException{
		writer.close();
	}
}
