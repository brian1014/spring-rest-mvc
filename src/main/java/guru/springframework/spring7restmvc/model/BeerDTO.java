package guru.springframework.spring7restmvc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonDeserialize(builder = BeerDTO.BeerDTOBuilder.class)
@Builder
@Data
public class BeerDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("beerName")
    @NotBlank
    @NotNull
    private String beerName;

    @JsonProperty("beerStyle")
    @NotNull
    private BeerStyle beerStyle;

    @JsonProperty("upc")
    @NotNull
    @NotBlank
    private String upc;

    @JsonProperty("quantityOnHand")
    private Integer quantityOnHand;

    @JsonProperty("price")

    @NotNull
    private BigDecimal price;

    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
