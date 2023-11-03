package com.advance.repository.implementation;

import java.util.Collection;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.advance.domain.UserEvent;
import com.advance.enumeration.EventType;
import com.advance.repository.EventRepository;
import com.advance.rowmapper.UserEventRowMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.advance.query.EventQuery.*;


@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepositoryImpl implements EventRepository {

	private final NamedParameterJdbcTemplate jdbc;
	@Override
	public Collection<UserEvent> getEventByUserId(Long id) {
		return jdbc.query(SELECT_EVENTS_BY_USER_ID_QUERY, Map.of("id", id), new UserEventRowMapper());
	}

	@Override
	public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {

		jdbc.update(INSERT_EVENT_BY_USER_EMAIL_QUERY, Map.of("email", email, "type", eventType.name(), "device", device, "ipAddress", ipAddress)); 
	}

	@Override
	public void addUserEvent(Long id, EventType eventType, String device, String ipAddress) {

	}

}
