package com.habittracker.api.auth.testutils;

import static com.habittracker.api.config.constants.JwtTestConstant.AUTHORIZATION_HEADER;
import static com.habittracker.api.config.constants.JwtTestConstant.BEARER_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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

  public static MockHttpServletRequestBuilder addJwt(
      String jwt, MockHttpServletRequestBuilder builder) {
    return builder.header(AUTHORIZATION_HEADER, BEARER_PREFIX + jwt);
  }

  public <T> ResultActions performAuthenticatedPostRequest(
      String endpoint, T body, String authToken) throws Exception {
    return mockMvc.perform(
        post(endpoint)
            .header("Authorization", authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
  }

  public ResultActions performAuthenticatedGetRequest(String endpoint, String authToken)
      throws Exception {
    return mockMvc.perform(get(endpoint).header("Authorization", authToken));
  }

  public <T> ResultActions performUnauthenticatedPostRequest(String endpoint, T body)
      throws Exception {
    return mockMvc.perform(
        post(endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
  }

  public ResultActions performUnauthenticatedGetRequest(String endpoint) throws Exception {
    return mockMvc.perform(get(endpoint));
  }
}
