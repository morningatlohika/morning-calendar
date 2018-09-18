package com.lohika.morning.morningcalendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MorningCalendarApplication {

  public static void main(String[] args) {
    SpringApplication.run(MorningCalendarApplication.class, args);
  }
}
