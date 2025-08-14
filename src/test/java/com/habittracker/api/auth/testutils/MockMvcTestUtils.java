package com.habittracker.api.auth.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Component
public class MockMvcTestUtils {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;

  public MockMvcTestUtils(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  public <T> ResultActions performPostRequest(String endpoint, T body) throws Exception {
    return performPostRequest(endpoint, objectMapper.writeValueAsString(body));
  }

  public ResultActions performPostRequest(String endpoint, String jsonContent) throws Exception {
    return mockMvc.perform(
        post(endpoint).contentType(MediaType.APPLICATION_JSON).content(jsonContent));
  }
}
