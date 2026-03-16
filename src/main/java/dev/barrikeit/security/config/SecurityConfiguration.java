package dev.barrikeit.security.config;

import dev.barrikeit.config.ApplicationProperties;
import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Main security configuration for the application.
 *
 * <p>This configuration is enabled when {@code application.security.enabled=true} or when the
 * property is missing (default secure-by-default behavior).
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>JWT-based stateless authentication
 *   <li>CORS and CSRF configuration
 *   <li>HTTP security headers
 *   <li>Authorization rules per endpoint
 *   <li>Custom exception handling
 * </ul>
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize on @MessageMapping methods
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
@Import(SecurityExceptionHandler.class)
public class SecurityConfiguration {

  private final ApplicationProperties.ServerProperties serverProperties;
  private final SecurityProperties securityProperties;
  private final SecurityExceptionHandler exceptionHandler;
  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  /**
   * Defines the main {@link SecurityFilterChain} for the application.
   *
   * <p>This interceptor chain:
   *
   * <ul>
   *   <li>Applies CORS and CSRF protections
   *   <li>Uses stateless session management (JWT)
   *   <li>Registers JWT and header validation filters
   *   <li>Defines authorization rules for endpoints
   * </ul>
   *
   * @param http the {@link HttpSecurity} to configure
   * @return the configured security interceptor chain
   * @throws Exception if configuration fails
   */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.warn("Security Configuration active");
    String apiPath = serverProperties.getServlet().getApiPath();
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            headers ->
                headers.frameOptions(
                    options ->
                        options
                            .sameOrigin()
                            .addHeaderWriter(
                                new StaticHeadersWriter(
                                    "X-Content-Security-Policy", "default-src 'self'"))
                            .addHeaderWriter(
                                new StaticHeadersWriter("X-WebKit-CSP", "default-src 'self'"))))
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(exceptionHandler)
                    .accessDeniedHandler(exceptionHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        apiPath + "/ws/**",
                        apiPath + "/public/**",
                        apiPath + "/error/**",
                        apiPath + "/error",
                        apiPath + "/version/**",
                        apiPath + "/version")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    SecurityProperties.CorsProperties cors = securityProperties.getCors();

    if (Boolean.TRUE.equals(cors.getEnabled())) {
      configuration.setAllowedOriginPatterns(
          Arrays.asList(cors.getAllowed().getOrigins().split(",")));
      configuration.setAllowedMethods(Arrays.asList(cors.getAllowed().getMethods().split(",")));
      configuration.setAllowedHeaders(Arrays.asList(cors.getAllowed().getHeaders().split(",")));
      configuration.setAllowCredentials(true);
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(cors.getPath().getPattern(), configuration);
    return source;
  }
}
