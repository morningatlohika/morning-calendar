package com.lohika.morning.morningcalendar;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "calendar")
public class CalendarPropertySource {
  private String id;
  private String credentials;
  private String tokens;
}
