package dev.barrikeit.security.config;

import dev.barrikeit.config.ApplicationProperties;
import dev.barrikeit.security.config.file.AppHeaderValidatorFilter;
import dev.barrikeit.security.config.file.JwtFilter;
import dev.barrikeit.security.service.UserSessionService;
import dev.barrikeit.security.util.JwtUtil;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Log4j2
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final ApplicationProperties.ServerProperties serverProperties;
  private final SecurityProperties securityProperties;
  private final SecurityExceptionHandler exceptionHandler;
  private final JwtUtil jwtUtil;
  private final UserSessionService userSessionService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.warn("Security Configuration active");
    String apiPath = serverProperties.getServlet().getApiPath();
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(exceptionHandler)
                    .accessDeniedHandler(exceptionHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        apiPath + "/ws/**", // WebSocket handshake
                        apiPath + "/public/**",
                        apiPath + "/error/**",
                        apiPath + "/error",
                        apiPath + "/version/**",
                        apiPath + "/version")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(org.springframework.security.config.Customizer.withDefaults());

    http.addFilterBefore(appHeaderValidatorFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterAfter(jwtFilter(), AppHeaderValidatorFilter.class);

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

  @Bean
  public AppHeaderValidatorFilter appHeaderValidatorFilter() {
    return new AppHeaderValidatorFilter(
        serverProperties.getServlet(), securityProperties.getAppValidatorFilter());
  }

  @Bean
  public JwtFilter jwtFilter() {
    return new JwtFilter(jwtUtil, userSessionService);
  }
}
