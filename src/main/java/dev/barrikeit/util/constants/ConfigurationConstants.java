package dev.barrikeit.util.constants;

public class ConfigurationConstants {
  private ConfigurationConstants() {
    throw new IllegalStateException("Constants class");
  }

  public static final String DEFAULT_SERVLET_NAME = "/generic-ws";

  public static final String SPRING_PROFILE_TEST = "test";
  public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
  public static final String SPRING_PROFILE_PRODUCTION = "prod";
}
