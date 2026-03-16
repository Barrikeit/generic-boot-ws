package dev.barrikeit.security.util;

import dev.barrikeit.security.config.SecurityProperties;
import dev.barrikeit.util.TimeUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final String issuer;
  private final long accessTokenExpirationSec;
  private final long refreshTokenExpirationSec;

  public JwtUtil(SecurityProperties securityProperties) {
    SecurityProperties.JwtProperties jwt = securityProperties.getJwt();
    this.secretKey = Keys.hmacShaKeyFor(HashEncodeUtil.sha256Bytes(jwt.getSecret()));
    this.issuer = jwt.getIssuer();
    this.accessTokenExpirationSec = jwt.getExpiration();
    this.refreshTokenExpirationSec = jwt.getExpirationRefresh();
  }

  /** Parse token claims */
  public Claims parseToken(String token) {
    return Jwts.parser()
        .requireIssuer(issuer)
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractJti(String token) {
    return parseToken(token).getId();
  }

  public String extractUsername(String token) {
    return parseToken(token).getSubject();
  }

  public UUID extractUserCode(String token) {
    return UUID.fromString(parseToken(token).get(JwtConstants.USER_CODE, String.class));
  }

  public Date extractIssuedDate(String token) {
    return parseToken(token).getIssuedAt();
  }

  public Date extractExpirationDate(String token) {
    return parseToken(token).getExpiration();
  }

  public boolean isRefreshableToken(String token) {
    return Boolean.TRUE.equals(parseToken(token).get(JwtConstants.REFRESHABLE, Boolean.class));
  }

  public List<String> extractRoles(String token) {
    List<String> roles = parseToken(token).get(JwtConstants.ROLES, List.class);
    return roles != null ? roles : Collections.emptyList();
  }

  public List<SimpleGrantedAuthority> extractAuthorities(String token) {
    return extractRoles(token).stream().map(SimpleGrantedAuthority::new).toList();
  }
}
