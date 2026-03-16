package dev.barrikeit.service.dto;

import dev.barrikeit.service.dto.base.BaseDto;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LocationDto extends BaseDto {

  private UUID code;

  private String name;

  private String country;

  private String city;

  @Override
  public String toString() {
    return "LocationDto{"
        + "name='"
        + name
        + '\''
        + ", country='"
        + country
        + '\''
        + ", city='"
        + city
        + '\''
        + '}';
  }
}
