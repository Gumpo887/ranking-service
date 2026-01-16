package net.movievault.ranking_service.domain.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.MDC;

/**
 * Clase base abstracta para todos los eventos de dominio del sistema.
 * <p>
 * Cada evento que herede de esta clase representará un suceso relevante dentro del dominio de la aplicación,
 * por ejemplo la creación o actualización de un {@code Item}. Los eventos de dominio se usan para:
 * <ul>
 *     <li>Ser propagados a través de la arquitectura interna o a otros microservicios vía Kafka.</li>
 *     <li>Mantener trazabilidad del flujo de negocio mediante {@code requestId} y {@code eventType}.</li>
 * </ul>
 * <p>
 * Al instanciarse, cada evento captura automáticamente el {@code requestId} desde el contexto de logging {@link MDC},
 * lo que permite correlacionar logs y eventos de manera consistente.
 * <p>
 * Las subclases deben definir datos específicos del evento (por ejemplo {@link ItemCreatedEvent}) y se espera que
 * mantengan la inmutabilidad de sus propiedades relevantes.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public abstract class DomainEvent {

    private String requestId;
    private String eventType;

    public DomainEvent(String eventType) {
        this.requestId = MDC.get("requestId");
        this.eventType = eventType;
    }

}
