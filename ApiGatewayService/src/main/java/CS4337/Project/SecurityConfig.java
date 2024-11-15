package CS4337.Project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
    http.authorizeExchange(
            exchange -> {
              exchange.pathMatchers("/auth").permitAll();
              exchange.anyExchange().authenticated();
            })
        .csrf()
        .disable()
        .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

    return http.build();
  }
}
