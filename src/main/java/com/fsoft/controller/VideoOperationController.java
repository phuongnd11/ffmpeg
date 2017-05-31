package com.fsoft.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fsoft.application.VideoOperationService;
import com.fsoft.model.Action;
import com.fsoft.model.Video;
import com.fsoft.model.VideoOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class VideoOperationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VideoOperationController.class);
	
	@Autowired
	private VideoOperationService videoOperationService;
	
	@RequestMapping(value = "/video/add", method = RequestMethod.POST)
	public @ResponseBody String addOperation(@RequestBody VideoOperation videoOperation){
		//return videoOperation.getVideo().getLink();
		return videoOperationService.processVideo(videoOperation);
	}
	
	@RequestMapping(value = "/video/get", method = RequestMethod.GET)
	public ResponseEntity<VideoOperation> getOperation(){
		
		VideoOperation vo = new VideoOperation();
		Video video = new Video();
		video.setLink("");
		vo.setVideo(video);
		Action act = new Action();
		act.setType("cut");
		act.setStart("00:30");
		act.setEnd("00:50");
		List<Action> actions = new ArrayList<Action>();
		actions.add(act);
		
		vo.setActions(actions);
		
		return new ResponseEntity<VideoOperation>(vo, HttpStatus.OK);
	}
	
}
