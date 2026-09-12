package guru.springframework.spring7restmvc.model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeerOrderLineDTO {
    private UUID id;

    private Long version;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;

    private BeerDTO beer;

    @Min(value = 1, message = "Quantity On Hand must be greater than 0")
    private Integer orderQuantity;
    private Integer quantityAllocated;
    private BeerOrderLineStatus orderLineStatus;
}
