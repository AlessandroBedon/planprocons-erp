package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.RegistroAcceso;
import ec.planprocons.backend.repository.projection.DashboardResumenProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {

    boolean existsByDispositivoIdAndCodigoEvento(Long dispositivoId, String codigoEvento);

    @Query(value = """
            SELECT COUNT(*) AS "totalAccesos",
                   COUNT(*) FILTER (WHERE ra.tipo_evento = 'ENTRADA') AS "entradas",
                   COUNT(*) FILTER (WHERE ra.tipo_evento = 'SALIDA') AS "salidas",
                   COUNT(*) FILTER (WHERE ra.estado = 'PERMITIDO') AS "permitidos",
                   COUNT(*) FILTER (WHERE ra.estado = 'RECHAZADO') AS "rechazados",
                   COUNT(DISTINCT ra.persona_id) AS "personasUnicas"
            FROM registros_acceso ra
            WHERE ra.fecha_hora >= :inicio
              AND ra.fecha_hora < :fin
            """, nativeQuery = true)
    DashboardResumenProjection obtenerResumen(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = """
            SELECT EXTRACT(HOUR FROM ra.fecha_hora)::integer AS hora,
                   COUNT(*) AS cantidad
            FROM registros_acceso ra
            WHERE ra.fecha_hora >= :inicio
              AND ra.fecha_hora < :fin
            GROUP BY EXTRACT(HOUR FROM ra.fecha_hora)
            ORDER BY hora
            """, nativeQuery = true)
    List<Object[]> contarAccesosPorHora(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = """
            SELECT CAST(ra.fecha_hora AS date) AS fecha,
                   COUNT(*) AS cantidad
            FROM registros_acceso ra
            WHERE ra.fecha_hora >= :inicio
              AND ra.fecha_hora < :fin
            GROUP BY CAST(ra.fecha_hora AS date)
            ORDER BY fecha
            """, nativeQuery = true)
    List<Object[]> contarAccesosPorDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = """
            SELECT p.id,
                   p.codigo_biometrico,
                   p.nombres,
                   p.apellidos,
                   COUNT(*) AS cantidad
            FROM registros_acceso ra
            INNER JOIN personas p ON p.id = ra.persona_id
            WHERE ra.fecha_hora >= :inicio
              AND ra.fecha_hora < :fin
            GROUP BY p.id, p.codigo_biometrico, p.nombres, p.apellidos
            ORDER BY cantidad DESC, p.id
            LIMIT :limite
            """, nativeQuery = true)
    List<Object[]> encontrarPersonasFrecuentes(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("limite") int limite
    );
}
