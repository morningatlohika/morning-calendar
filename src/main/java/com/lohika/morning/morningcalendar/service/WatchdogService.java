package com.lohika.morning.morningcalendar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.api.services.calendar.model.Event;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchdogService {

  private final CalendarService calendarService;
  private final SlackNotificationService slackNotificationService;

  @Scheduled(cron = "${watchdog.daily}")
  public void daily() throws GeneralSecurityException, IOException {
    log.info("Daily watchdog");
    List<Event> events = calendarService.getEvents(1);
    slackNotificationService.send("Events:", events);
  }
}
