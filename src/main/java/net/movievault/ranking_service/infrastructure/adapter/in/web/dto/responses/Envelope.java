package net.movievault.ranking_service.infrastructure.adapter.in.web.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Esta clase es el DTO envoltorio para todas las respuestas. Es una manera de estandarizar en cierto modo las
 * respuestas del API REST y de además incluir información extra que irá contenida en el atributo meta (clase Meta)
 * que envolverá esta clase.
 * <p>
 * Como se puede apreciar, tendremos únicamente dos atributos en la clase, la clase Meta y la clase T que es un
 * genérico para los DTO's de respuesta (es decir, puede contener cualquier DTO de respuesta).
 *
 * @param <T> El DTO de respuesta que queremos envolver.
 */
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Envelope<T> {

    private T data;
    private Meta meta;

}
