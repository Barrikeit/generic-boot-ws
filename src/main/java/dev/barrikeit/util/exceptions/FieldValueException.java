package dev.barrikeit.util.exceptions;

import dev.barrikeit.util.constants.ExceptionConstants;
import java.net.URI;
import org.springframework.http.HttpStatus;

public class FieldValueException extends GenericException {

  static final URI TYPE = URI.create("");

  public FieldValueException(String message, Object... messageArgs) {
    super(HttpStatus.BAD_REQUEST, TYPE, ExceptionConstants.BAD_REQUEST, message, messageArgs);
  }
}
