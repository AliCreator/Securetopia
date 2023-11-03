package com.advance.service;

import java.util.Collection;

import com.advance.domain.UserEvent;
import com.advance.enumeration.EventType;

public interface EventService {

	Collection<UserEvent> getEventByUserId(Long id); 
	
	void addUserEvent(String email, EventType eventType, String device, String ipAddress); 
	
	void addUserEvent(Long id, EventType eventType, String device, String ipAddress);
}
