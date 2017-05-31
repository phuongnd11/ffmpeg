package com.fsoft.application;

import org.springframework.stereotype.Component;

import com.fsoft.model.VideoOperation;

@Component
public interface VideoOperationService {
	
	public String processVideo(VideoOperation videoOperation);
	
}
