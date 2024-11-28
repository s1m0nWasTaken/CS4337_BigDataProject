package CS4337.Project.Config;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyInterceptor implements ClientHttpRequestInterceptor {
  private static final String API_KEY_HEADER = "X-API-KEY";

  // todo: figure out why @Value doesn't work here
  String apiKey =
      "fWOB5mj49fqAtLZhw7ejLnQYYyrft63LLK7cnmtGXL8EdGlSSrVKnea8ecoD_BSS_MSPQz5sJt5ATMQYj9nDdQ";

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().add(API_KEY_HEADER, apiKey);
    return execution.execute(request, body);
  }
}
