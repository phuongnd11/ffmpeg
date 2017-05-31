package com.fsoft.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fsoft.model.Action;
import com.fsoft.model.Video;
import com.fsoft.model.VideoOperation;
import com.fsoft.utils.StringUtils;

@Component
public class VideoOperationServiceImpl implements VideoOperationService{

	private static final Logger logger = LoggerFactory.getLogger(VideoOperationServiceImpl.class);
	
	@Override
	public String processVideo(VideoOperation videoOperation) {
		Video video = videoOperation.getVideo();
		
		List<Action> actions = videoOperation.getActions();
		
		if(video == null || video.getLink() == null || actions == null || actions.size() == 0){
			return "";
		}
		
		String link = video.getLink();
		String uploadFileName = "";
		try {
			String fileName = "video_" + System.currentTimeMillis();
			String fullFileName = download(fileName, link, "D://data//");
			for(Action action : actions){
				if(action.getType().equals("cut")){
					String fileNameAfterCut = cutVideo(fullFileName, StringUtils.stringToSecond(action.getStart()), 
							StringUtils.stringToSecond(action.getEnd()));
					fullFileName = "D://data//" + fileNameAfterCut;
					//uploadFileName = uploadVideo(new File("D://data//" + fileNameAfterCut), fileNameAfterCut);
				}
				if(action.getType().equals("fadeaudio")){
					String fileNameFade = fadeAudio(fullFileName, StringUtils.stringToSecond(action.getStart()), 
							StringUtils.stringToSecond(action.getEnd()));
					fullFileName = "D://data//" + fileNameFade;
					//uploadFileName = uploadVideo(new File("D://data//" + fileNameFade), fileNameFade);
				}
				if(action.getType().equals("fadevideo")){
					String fileNameFade = fadeVideo(fullFileName, StringUtils.stringToSecond(action.getStart()), 
							StringUtils.stringToSecond(action.getEnd()));
					fullFileName = "D://data//" + fileNameFade;
					//uploadFileName = uploadVideo(new File("D://data//" + fileNameFade), fileNameFade);
				}
			}
			uploadFileName = uploadVideo(new File(fullFileName), link.substring(link.lastIndexOf('/')+1));
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
		
		return uploadFileName;
	}
	
	private String uploadVideo(File file, String fileName) {
//		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials("AKIAJSEVCWIE52NVVQYQ", "Cp4nkN3m5QsnGRq4TzGRbhLI2gRIFXAU6nG/qspO"));
		String bucketName = "fsoft-phuongnd2-video";
        try {
			logger.info("Start upload video to S3 bucket: " + bucketName);
			PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            s3client.putObject(request);
            logger.info("Uploaded successfully!!" + bucketName);
         } catch (Exception e) {
        	logger.info(e.toString());
        	return "";
         }
        return "https://s3-us-west-1.amazonaws.com/fsoft-phuongnd2-video/" + fileName;
	}
	
	private String cutVideo(String fileName, int fromSecond, int toSecond) throws IOException{
		
		FFmpeg ffmpeg = new FFmpeg("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffmpeg.exe");
		FFprobe ffprobe = new FFprobe("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffprobe.exe");
		String newFileName = "video_" + System.currentTimeMillis();
		FFmpegBuilder builder = new FFmpegBuilder()

		  .setInput(fileName)     // Filename, or a FFmpegProbeResult
		  .overrideOutputFiles(true) // Override the output if it exists
		  
		  .addOutput("D:\\data\\" + newFileName + "1" + ".mp4")   // Filename for the destination
		    .setFormat("mp4")        // Format is inferred from filename, or can be set
		    .setStartOffset(0, TimeUnit.SECONDS)
		    .addExtraArgs("-t")
		    .addExtraArgs("" + fromSecond)
		    //.addExtraArgs("-c")
		    //.addExtraArgs("copy")
		    
		    //.setTargetSize(250_000)  // Aim for a 250KB file
		   // .setVideoBitRate(640)
		    
		    .done();

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
		
		// Run a one-pass encode
		executor.createJob(builder).run();
		
		FFmpegProbeResult probeResult = ffprobe.probe(fileName);
		int duration = (int) probeResult.format.duration;
		System.out.println("duration: " + duration);
		boolean cutTwoFiles = false;
		if(duration > toSecond){
			
			FFmpegBuilder builder2 = new FFmpegBuilder()
	
			  .setInput(fileName)     // Filename, or a FFmpegProbeResult
			  .overrideOutputFiles(true) // Override the output if it exists
			  
			  .addOutput("D:\\data\\" + newFileName + "2" + ".mp4")   // Filename for the destination
			    .setFormat("mp4")        // Format is inferred from filename, or can be set
			    .setStartOffset(toSecond, TimeUnit.SECONDS)
			    /*.addExtraArgs("-t")
			    .addExtraArgs("" + toSecond)*/
			    /*.addExtraArgs("-c")
			    .addExtraArgs("copy")*/
			    
			    //.setTargetSize(250_000)  // Aim for a 250KB file
			   // .setVideoBitRate(640)
			    
			    .done();
			executor.createJob(builder2).run();
			cutTwoFiles = true;
		}
		if(cutTwoFiles && new File("D:\\data\\" + newFileName + "2" + ".mp4").exists()){
			List<String> lines = Arrays.asList("file D:\\\\data\\\\" + newFileName + "1" + ".mp4", "file D:\\\\data\\\\" + newFileName + "2" + ".mp4");
			Path file = Paths.get("D:\\data\\" + newFileName + ".txt");
			Files.write(file, lines, Charset.forName("UTF-8"));
			
			FFmpegBuilder builder3 = new FFmpegBuilder()
	
			  .setInput("D:\\data\\" + newFileName + ".txt")
			  .setFormat("concat")        // Format is inferred from filename, or can be set
			  .addExtraArgs("-safe")
			  .addExtraArgs("0")
			  .addOutput("D:\\data\\" + newFileName + ".mp4")   // Filename for the destination
			  /*			  
			  .addExtraArgs("-c")
			  .addExtraArgs("copy")*/
			   
			  .done();
			executor.createJob(builder3).run();
			
			//		return new File("D:\\data\\" + newFileName + ".mp4");
			return newFileName + ".mp4";
		}
		else return newFileName + "1" + ".mp4";
	}
	
	private String fadeVideo(String fileName, int fromSecond, int toSecond) throws IOException{
		FFmpeg ffmpeg = new FFmpeg("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffmpeg.exe");
		FFprobe ffprobe = new FFprobe("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffprobe.exe");
		String newFileName = "video_" + System.currentTimeMillis();
		FFmpegBuilder builder = new FFmpegBuilder()

		  .setInput(fileName)     // Filename, or a FFmpegProbeResult
		  .overrideOutputFiles(true) // Override the output if it exists
		  
		  .addOutput("D:\\data\\" + newFileName + ".mp4")   // Filename for the destination
		    .addExtraArgs("-vf")
		    .addExtraArgs("\"fade=t=out:st=" + fromSecond + ":d=" + (toSecond - fromSecond) + "\"")

		    //.setTargetSize(250_000)  // Aim for a 250KB file
		   // .setVideoBitRate(640)
		    
		    .done();

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

		// Run a one-pass encode
		executor.createJob(builder).run();
		
		return newFileName + ".mp4";
	}
	
	private String fadeAudio(String fileName, int fromSecond, int toSecond) throws IOException{
		FFmpeg ffmpeg = new FFmpeg("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffmpeg.exe");
		FFprobe ffprobe = new FFprobe("D:\\data\\ffmpeg-20170404-1229007-win64-static\\ffmpeg-20170404-1229007-win64-static\\bin\\ffprobe.exe");
		String newFileName = "video_" + System.currentTimeMillis();
		FFmpegBuilder builder = new FFmpegBuilder()

		  .setInput(fileName)     // Filename, or a FFmpegProbeResult
		  .overrideOutputFiles(true) // Override the output if it exists
		  
		  .addOutput("D:\\data\\" + newFileName + ".mp4")   // Filename for the destination
		    .addExtraArgs("-af")
		    .addExtraArgs("\"afade=t=out:st=" + fromSecond + ":d=" + (toSecond - fromSecond) + "\"")
		    
		    //.setTargetSize(250_000)  // Aim for a 250KB file
		   // .setVideoBitRate(640)
		    
		    .done();

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

		// Run a one-pass encode
		executor.createJob(builder).run();
		
		return newFileName + ".mp4";
	}
	
	private String cutVideo(File file, List<Action> actions){
		Collections.sort(actions);
		for(Action action : actions){
			if(action.getType().equals("cut")){
				
			}
		}
		
		return "";
	}
	
	
	
	public String download(String downloadedFileName, String fileURL, String destinationDirectory) throws IOException{
        // Open connection to the file
    	try {
    		downloadedFileName += ".mp4";
        	logger.info("Start download video " + downloadedFileName + "From: " + fileURL);
        	logger.info("To: " + destinationDirectory + "/" + downloadedFileName);
        	
        	System.setProperty("http.proxyHost", "fsoft-proxy");
        	System.setProperty("http.proxyPort", "8080");
        	System.setProperty("https.proxyHost", "fsoft-proxy");
        	System.setProperty("https.proxyPort", "8080");
        	
    		URL url = new URL(fileURL);
            InputStream is = url.openStream();
            
            URLConnection conn = url.openConnection();
            //conn.setConnectTimeout(600000);
            //conn.setReadTimeout(600000);
            int size = conn.getContentLength();
            String type = conn.getContentType();
            
            logger.info("File size: " + size + ", type: " + type);
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
            if(file.length() == size) {
            	return destinationDirectory + "/" + downloadedFileName;
            } else {
            	logger.info("Download is interupted");
            	return null;
            }
		} catch (Exception e) {
			logger.info(e.toString());
			return null;
		}
    	
    }   
}
