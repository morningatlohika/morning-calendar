package com.lohika.morning.morningcalendar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackNotificationService {

  @Value("${slack.url}")
  private String slackUrl;

  private static String dateTimeSetEntryToString(Map.Entry<DateTime, Set<String>> dateTimeSetEntry) {
    return dateTimeSetEntry.getKey() + "\n" +
           dateTimeSetEntry.getValue().stream().collect(Collectors.joining(","));
  }

  public void send(String title, List<Event> events) {

    Map<DateTime, Set<String>> eventPerDay = events.stream()
        .collect(Collectors.groupingBy(event -> event.getStart().getDate(),
            Collectors.mapping(Event::getSummary, Collectors.toSet())));

    String message = eventPerDay.entrySet().stream()
        .map(SlackNotificationService::dateTimeSetEntryToString)
        .collect(Collectors.joining("\n"));

    if (!Strings.isBlank(message)) {
      sendSlackMessage(title + "\n" + message);
    }
  }

  private void sendSlackMessage(String message) {
    HttpClient client = new HttpClient();
    PostMethod post = new PostMethod(slackUrl);
    JSONObject json = new JSONObject();
    try {
      json.put("text", message);
      post.addParameter("payload", json.toString());
      post.getParams().setContentCharset("UTF-8");
      int responseCode = client.executeMethod(post);
      String response = post.getResponseBodyAsString();
      if (responseCode != HttpStatus.SC_OK) {
        log.info("Slack post may have failed. Response: {}", response);
      }
    } catch (JSONException e) {
      log.error("JSONException posting to Slack ", e);
    } catch (IllegalArgumentException e) {
      log.error("IllegalArgumentException posting to Slack ", e);
    } catch (IOException e) {
      log.error("IOException posting to Slack ", e);
    } finally {
      post.releaseConnection();
    }
  }
}
