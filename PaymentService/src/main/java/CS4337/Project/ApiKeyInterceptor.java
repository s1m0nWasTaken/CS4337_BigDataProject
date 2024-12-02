package CS4337.Project;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyInterceptor implements ClientHttpRequestInterceptor {
  private static final String API_KEY_HEADER = "X-API-KEY";

  @Value("${api-key}")
  private String apiKey;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().add(API_KEY_HEADER, apiKey);
    return execution.execute(request, body);
  }
}
