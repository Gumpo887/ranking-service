package net.movievault.ranking_service.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Esta clase sirve para insertar el contexto de logging de {@link MDC} para cada petición. Esta clase se ejecutará
 * cada vez que una petición a nuestra API REST sea realizada. Se generará un {@code timestamp} y un {@code requestId},
 * así como se recogerá la IP y el User Agent del cliente que ha realizado la petición.
 * <p>
 * Esta clase actúa como un filtro de Spring, en concreto extendiendo la clase de filtros {@link OncePerRequestFilter}.
 */
@Slf4j
@Component
public class LoggingContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put("requestId", UUID.randomUUID().toString()); // Genera UUIDs para cada petición
            MDC.put("timestamp", LocalDateTime.now().toString()); // Genera el timestamp
            MDC.put("ip", request.getRemoteAddr());
            MDC.put("userAgent", request.getHeader("User-Agent"));
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}