package net.movievault.ranking_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para la ejecución asíncrona del microservicio.
 * <p>
 * Esta clase habilita la ejecución de métodos marcados con {@code @Async} y define un {@link Executor} personalizado
 * que gestiona el pool de hilos para tareas en segundo plano (por ejemplo, envío de eventos a Kafka).
 * </p>
 * <p>
 * La configuración del executor permite controlar la concurrencia y la capacidad de cola, mejorando el comportamiento
 * bajo carga y facilitando el seguimiento mediante nombres de hilo con prefijo configurable.
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Define un executor personalizado para la ejecución asíncrona.
     * <p>
     * Este bean controla cómo se gestionan los hilos utilizados para ejecutar métodos marcados con {@code @Async}.
     * Configura un pool con parámetros ajustables para optimizar el rendimiento y el control de concurrencia del
     * microservicio.
     * </p>
     *
     * <ul>
     *     <li><b>CorePoolSize:</b> Número mínimo de hilos que estarán siempre disponibles (4).</li>
     *     <li><b>MaxPoolSize:</b> Número máximo de hilos que pueden crearse si la carga lo requiere (10).</li>
     *     <li><b>QueueCapacity:</b> Cantidad máxima de tareas que pueden mantenerse en cola (100).</li>
     *     <li><b>ThreadNamePrefix:</b> Prefijo asignado a los hilos para facilitar el seguimiento en logs
     *     ("AsyncEvent-").</li>
     * </ul>
     *
     * <p>
     * Esta configuración ayuda a mantener la trazabilidad y el rendimiento del sistema en operaciones que no requieren
     * respuesta inmediata, como la publicación de eventos en Kafka o el procesamiento de tareas de dominio.
     * </p>
     *
     * @return un {@link java.util.concurrent.Executor} configurado para tareas asíncronas.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncEvent-");
        executor.initialize();
        return executor;
    }
}
