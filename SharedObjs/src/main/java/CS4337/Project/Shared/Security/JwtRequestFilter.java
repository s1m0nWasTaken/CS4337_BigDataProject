package CS4337.Project.Shared.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  @Autowired private JwtUtils jwtUtil;

  @Autowired
  @Qualifier("authRestTemplate") private RestTemplate restTemplate;

  private static final String REFRESH_TOKEN_URL = "http://AUTHSERVICE/refresh";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String jwt = getJwtFromRequest(request);

    if (jwt != null) {
      String userId = jwtUtil.extractUserId(jwt);

      if (jwtUtil.validateToken(jwt, userId)) {
        setAuthentication(userId, jwt, request);
      } else {
        String refreshToken = getRefreshTokenFromRequest(request);
        if (refreshToken != null) {
          String newJwt = refreshJwt(refreshToken);
          if (newJwt != null) {
            setAuthentication(userId, newJwt, request);
            response.setHeader("Authorization", "Bearer " + newJwt);
          } else {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED, "Cannot refresh token, please reauthenticate");
            return;
          }
        } else {
          response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "JWT expired and refresh token missing");
          return;
        }
      }
    }

    chain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private String getRefreshTokenFromRequest(HttpServletRequest request) {
    return request.getHeader("Refresh-Token");
  }

  private void setAuthentication(String userId, String jwt, HttpServletRequest request) {
    String role = jwtUtil.extractRole(jwt);
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String refreshJwt(String refreshToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"refreshToken\": \"" + refreshToken + "\"}";

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(REFRESH_TOKEN_URL, HttpMethod.POST, entity, String.class);

    if (response.getStatusCode() == HttpStatus.CREATED) {
      return response.getBody();
    }
    return null;
  }
}
