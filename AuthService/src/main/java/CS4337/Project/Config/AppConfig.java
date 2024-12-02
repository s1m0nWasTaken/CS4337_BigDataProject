package CS4337.Project.Config;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Autowired ApiKeyInterceptor apiKeyInterceptor;

  @Bean("googleRestTemplate")
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean("msRestTemplate")
  @LoadBalanced
  public RestTemplate getRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setInterceptors(List.of(apiKeyInterceptor));
    return restTemplate;
  }

  @Bean("authRestTemplate")
  @LoadBalanced
  public RestTemplate getAuthRestTemplate() {
    return new RestTemplate();
  }
}
