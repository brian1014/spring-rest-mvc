package guru.springframework.spring7restmvc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonDeserialize(builder = CustomerDTO.CustomerDTOBuilder.class)
@Data
@Builder
public class CustomerDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("email")
    private String email;

    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
