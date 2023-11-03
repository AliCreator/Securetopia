package com.advance.service.implementation;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.advance.domain.UserEvent;
import com.advance.enumeration.EventType;
import com.advance.repository.EventRepository;
import com.advance.service.EventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
	
	private final EventRepository eventRepository; 

	@Override
	public Collection<UserEvent> getEventByUserId(Long id) {
		return eventRepository.getEventByUserId(id);
	}

	@Override
	public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
		eventRepository.addUserEvent(email, eventType, device, ipAddress);
	}

	@Override
	public void addUserEvent(Long id, EventType eventType, String device, String ipAddress) {

	}

}
