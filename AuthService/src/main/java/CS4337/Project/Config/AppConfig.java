package CS4337.Project.Config;

import java.util.List;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Bean("googleRestTemplate")
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean("msRestTemplate")
  @LoadBalanced
  public RestTemplate getRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setInterceptors(List.of(new ApiKeyInterceptor()));
    return restTemplate;
  }
}
