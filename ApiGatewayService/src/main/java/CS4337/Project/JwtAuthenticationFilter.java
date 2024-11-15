package CS4337.Project;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter implements Filter {

  @Autowired private JwtUtils jwtUtils;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String jwt = authHeader.substring(7);
      String email = jwtUtils.extractUserEmail(jwt);

      try {
        if (jwtUtils.validateToken(jwt, email)) {
          filterChain.doFilter(request, response); // Token is expired, continue with the chain
          return;
        }

        // Set authentication context (this can be a custom UserDetails)
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

      } catch (Exception e) {
        // Handle exception (invalid token, etc.)
        filterChain.doFilter(
            request, response); // Continue with the chain even if the token is invalid
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}

// @Component
// public class JwtAuthenticationFilter implements WebFilter {
//
//  @Autowired private JwtUtils jwtUtils;
//  @Autowired private WebClient.Builder webClientBuilder;
//  private static final String REFRESH_TOKEN_URL = "http://auth-service/refresh";
//
//  @Override
//  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//    String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//
//    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//      return Mono.error(new BadCredentialsException("Invalid Authorization Header"));
//    }
//
//    String jwt = authorizationHeader.substring(7);
//    String email = jwtUtils.extractUserEmail(jwt);
//
//    if (jwtUtils.validateToken(jwt, email)) {
//      String role = jwtUtils.extractRole(jwt);
//
//      UsernamePasswordAuthenticationToken authentication =
//          new UsernamePasswordAuthenticationToken(
//              email, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//    } else {
//      String refreshToken = exchange.getRequest().getHeaders().getFirst("RefreshToken");
//
//      return webClientBuilder
//          .build()
//          .method(HttpMethod.POST)
//          .uri(REFRESH_TOKEN_URL)
//          .bodyValue(Map.of("refreshToken", refreshToken))
//          .retrieve()
//          .onStatus(
//              status -> status.is4xxClientError() || status.is5xxServerError(),
//              clientResponse -> Mono.error(new AccessDeniedException("Failed to refresh token")))
//          .bodyToMono(String.class)
//          .flatMap(
//              newJwt -> {
//                exchange.getRequest().mutate().header("Authorization", "Bearer " +
// newJwt).build();
//
//                return chain.filter(exchange);
//              })
//          .onErrorResume(
//              error -> {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//              });
//    }
//
//    return chain.filter(exchange);
//  }
// }
