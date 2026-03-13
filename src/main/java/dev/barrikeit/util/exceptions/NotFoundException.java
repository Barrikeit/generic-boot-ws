package dev.barrikeit.util.exceptions;

import dev.barrikeit.util.constants.ExceptionConstants;
import java.net.URI;
import org.springframework.http.HttpStatus;

public class NotFoundException extends GenericException {

  static final URI TYPE = URI.create("");

  public NotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }

  public NotFoundException(String message, Object... messageArgs) {
    super(HttpStatus.NOT_FOUND, TYPE, ExceptionConstants.NOT_FOUND, message, messageArgs);
  }
}
