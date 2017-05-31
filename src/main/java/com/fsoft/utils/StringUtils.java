package com.fsoft.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fsoft.application.VideoOperationServiceImpl;

public class StringUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);
	
	public static int stringToSecond(String str){
		if(str == null || str.length() == 0){
			return 0;
		}
		String [] split = str.trim().split(":");
		
		int min = Integer.parseInt(split[0]);
		int sec = Integer.parseInt(split[1]);
		return min*60+sec;
	}
	
	public static void main(String[] args) throws IOException {
		String downloadedFileName = "video_" + System.currentTimeMillis(); 
		downloadedFileName += ".mp4";
		String destinationDirectory = "D:\\" + "data\\";
		String fileURL = "http://www.pdf995.com/samples/pdf.pdf";
    	logger.info("Start download video " + downloadedFileName + "From: " + fileURL);
    	logger.info("To: " + destinationDirectory + "/" + downloadedFileName);
    	System.setProperty("http.proxyHost", "fsoft-proxy");
    	System.setProperty("http.proxyPort", "8080");
		URL url = new URL(fileURL);
        InputStream is = url.openStream();
        
        //URLConnection conn = url.openConnection();
        //conn.setConnectTimeout(600000);
        //conn.setReadTimeout(600000);
        //int size = conn.getContentLength();
        //String type = conn.getContentType();
        
       // logger.info("File size: " + size + ", type: " + type);
		 // Stream to the destionation file
        FileOutputStream fos = new FileOutputStream(destinationDirectory + "/" + downloadedFileName);
  
        // Read bytes from URL to the local file
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
         
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer,0,bytesRead);
        }
        logger.info("Closing fos: " + fileURL);
        // Close destination stream
        fos.close();
        logger.info("Closing inputstream: " + fileURL);
        // Close URL stream
        is.close();
        File file = new File(destinationDirectory + "/" + downloadedFileName);
        logger.info("download successful: " + file.length());
     //   if(file.length() == size) {
        	//return destinationDirectory + "/" + downloadedFileName;
       // } else {
       // 	logger.info("Download is interupted");
        	//return null;
        }
	
}
