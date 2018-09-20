package com.lohika.morning.morningcalendar.controller;

import lombok.RequiredArgsConstructor;

import com.lohika.morning.morningcalendar.service.CalendarService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CalendarController {
  private final CalendarService calendarService;

  @GetMapping("/report")
  @ResponseBody
  public List<String> getReport() throws GeneralSecurityException, IOException {
    return calendarService.getEvents(7).stream()
        .map(event -> event.getStart().getDate() + " " + event.getDescription())
        .collect(Collectors.toList());
  }
}
