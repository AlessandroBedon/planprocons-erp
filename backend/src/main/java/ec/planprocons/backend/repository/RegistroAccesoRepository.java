package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.RegistroAcceso;
import ec.planprocons.backend.repository.projection.DashboardResumenProjection;
import ec.planprocons.backend.repository.projection.AnomaliaProjection;
import ec.planprocons.backend.repository.projection.PatronGeneralProjection;
import ec.planprocons.backend.repository.projection.PatronPersonaProjection;
import ec.planprocons.backend.repository.projection.ResumenAnomaliasProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {

    String ANOMALIAS_CTE = """
            WITH base AS (
                SELECT ra.id,
                       ra.persona_id,
                       ra.fecha_hora,
                       ra.tipo_evento,
                       ra.estado,
                       p.codigo_biometrico,
                       p.nombres,
                       p.apellidos,
                       EXTRACT(HOUR FROM ra.fecha_hora)::integer AS hora,
                       EXTRACT(EPOCH FROM ra.fecha_hora::time) / 60.0 AS minuto_dia
                FROM registros_acceso ra
                INNER JOIN personas p ON p.id = ra.persona_id
                WHERE ra.fecha_hora >= :inicio
                  AND ra.fecha_hora < :fin
            ),
            conteo_5 AS (
                SELECT id,
                       COUNT(*) OVER (
                           PARTITION BY persona_id
                           ORDER BY fecha_hora
                           RANGE BETWEEN INTERVAL '5 minutes' PRECEDING AND CURRENT ROW
                       ) AS cantidad
                FROM base
            ),
            rechazos_10 AS (
                SELECT id,
                       COUNT(*) OVER (
                           PARTITION BY persona_id
                           ORDER BY fecha_hora
                           RANGE BETWEEN INTERVAL '10 minutes' PRECEDING AND CURRENT ROW
                       ) AS cantidad
                FROM base
                WHERE estado = 'RECHAZADO'
            ),
            entradas_diarias AS (
                SELECT b.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY b.persona_id, CAST(b.fecha_hora AS date)
                           ORDER BY b.fecha_hora, b.id
                       ) AS posicion
                FROM base b
                WHERE b.tipo_evento = 'ENTRADA'
                  AND b.estado = 'PERMITIDO'
            ),
            baseline AS (
                SELECT persona_id,
                       AVG(minuto_dia) AS minuto_promedio,
                       COUNT(*) AS dias
                FROM entradas_diarias
                WHERE posicion = 1
                GROUP BY persona_id
                HAVING COUNT(*) >= 10
            ),
            anomalias AS (
                SELECT 'ACCESO_NOCTURNO'::varchar AS tipo, b.*
                FROM base b
                WHERE b.hora BETWEEN 0 AND 4

                UNION ALL

                SELECT 'ACCESO_TEMPRANO'::varchar AS tipo, b.*
                FROM base b
                WHERE b.hora = 5

                UNION ALL

                SELECT 'ACCESO_TARDIO'::varchar AS tipo, b.*
                FROM base b
                WHERE b.hora >= 22

                UNION ALL

                SELECT 'ACCESOS_REPETITIVOS'::varchar AS tipo, b.*
                FROM base b
                INNER JOIN conteo_5 c ON c.id = b.id
                WHERE c.cantidad >= 3

                UNION ALL

                SELECT 'RECHAZOS_REPETITIVOS'::varchar AS tipo, b.*
                FROM base b
                INNER JOIN rechazos_10 r ON r.id = b.id
                WHERE r.cantidad >= 3

                UNION ALL

                SELECT 'DESVIACION_HORARIO'::varchar AS tipo,
                       ed.id,
                       ed.persona_id,
                       ed.fecha_hora,
                       ed.tipo_evento,
                       ed.estado,
                       ed.codigo_biometrico,
                       ed.nombres,
                       ed.apellidos,
                       ed.hora,
                       ed.minuto_dia
                FROM entradas_diarias ed
                INNER JOIN baseline bl ON bl.persona_id = ed.persona_id
                WHERE ed.posicion = 1
                  AND ABS(ed.minuto_dia - bl.minuto_promedio) > 120
            )
            """;

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

    @Query(value = """
            WITH base AS (
                SELECT ra.fecha_hora, ra.tipo_evento
                FROM registros_acceso ra
                WHERE ra.fecha_hora >= :inicio
                  AND ra.fecha_hora < :fin
            ),
            por_hora AS (
                SELECT EXTRACT(HOUR FROM fecha_hora)::integer AS hora,
                       COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE tipo_evento = 'ENTRADA') AS entradas,
                       COUNT(*) FILTER (WHERE tipo_evento = 'SALIDA') AS salidas
                FROM base
                GROUP BY EXTRACT(HOUR FROM fecha_hora)
            ),
            por_dia AS (
                SELECT CAST(fecha_hora AS date) AS fecha,
                       EXTRACT(ISODOW FROM fecha_hora)::integer AS dia_semana,
                       COUNT(*) AS cantidad
                FROM base
                GROUP BY CAST(fecha_hora AS date), EXTRACT(ISODOW FROM fecha_hora)
            ),
            promedio_semana AS (
                SELECT dia_semana, AVG(cantidad) AS promedio
                FROM por_dia
                GROUP BY dia_semana
            )
            SELECT (SELECT COUNT(*) FROM base) AS "registrosAnalizados",
                   (SELECT hora FROM por_hora ORDER BY total DESC, hora LIMIT 1) AS "horaPicoGeneral",
                   COALESCE((SELECT total FROM por_hora ORDER BY total DESC, hora LIMIT 1), 0) AS "cantidadHoraPicoGeneral",
                   (SELECT hora FROM por_hora WHERE entradas > 0 ORDER BY entradas DESC, hora LIMIT 1) AS "horaPicoEntradas",
                   COALESCE((SELECT entradas FROM por_hora WHERE entradas > 0 ORDER BY entradas DESC, hora LIMIT 1), 0) AS "cantidadHoraPicoEntradas",
                   (SELECT hora FROM por_hora WHERE salidas > 0 ORDER BY salidas DESC, hora LIMIT 1) AS "horaPicoSalidas",
                   COALESCE((SELECT salidas FROM por_hora WHERE salidas > 0 ORDER BY salidas DESC, hora LIMIT 1), 0) AS "cantidadHoraPicoSalidas",
                   (SELECT fecha FROM por_dia ORDER BY cantidad DESC, fecha LIMIT 1) AS "fechaMayorActividad",
                   COALESCE((SELECT cantidad FROM por_dia ORDER BY cantidad DESC, fecha LIMIT 1), 0) AS "cantidadFechaMayorActividad",
                   (SELECT dia_semana FROM promedio_semana ORDER BY promedio DESC, dia_semana LIMIT 1) AS "diaSemanaMayorActividad"
            """, nativeQuery = true)
    PatronGeneralProjection obtenerPatronesGenerales(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = """
            WITH diario AS (
                SELECT CAST(ra.fecha_hora AS date) AS fecha,
                       MIN(ra.fecha_hora) FILTER (
                           WHERE ra.tipo_evento = 'ENTRADA'
                             AND ra.estado = 'PERMITIDO'
                       ) AS primera_entrada,
                       MAX(ra.fecha_hora) FILTER (
                           WHERE ra.tipo_evento = 'SALIDA'
                             AND ra.estado = 'PERMITIDO'
                       ) AS ultima_salida,
                       COUNT(*) AS total
                FROM registros_acceso ra
                WHERE ra.persona_id = :personaId
                  AND ra.fecha_hora >= :inicio
                  AND ra.fecha_hora < :fin
                GROUP BY CAST(ra.fecha_hora AS date)
            )
            SELECT AVG(EXTRACT(EPOCH FROM primera_entrada::time) / 60.0)
                       FILTER (WHERE primera_entrada IS NOT NULL) AS "minutoHabitualEntrada",
                   AVG(EXTRACT(EPOCH FROM ultima_salida::time) / 60.0)
                       FILTER (WHERE ultima_salida IS NOT NULL) AS "minutoHabitualSalida",
                   COALESCE(SUM(total)::double precision / NULLIF(COUNT(*), 0), 0) AS "promedioAccesosDiarios",
                   COUNT(*) AS "diasAnalizados",
                   COUNT(primera_entrada) AS "diasConEntrada"
            FROM diario
            """, nativeQuery = true)
    PatronPersonaProjection obtenerPatronPersona(
            @Param("personaId") Long personaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query(value = ANOMALIAS_CTE + """
            SELECT a.tipo AS "tipo",
                   a.persona_id AS "personaId",
                   a.codigo_biometrico AS "codigoPersona",
                   a.nombres AS "nombres",
                   a.apellidos AS "apellidos",
                   a.id AS "registroAccesoId",
                   a.fecha_hora AS "fechaHora"
            FROM anomalias a
            WHERE (CAST(:tipo AS varchar) IS NULL OR a.tipo = CAST(:tipo AS varchar))
              AND (CAST(:personaId AS bigint) IS NULL OR a.persona_id = CAST(:personaId AS bigint))
            ORDER BY a.fecha_hora DESC, a.id DESC, a.tipo
            LIMIT :limite OFFSET :desplazamiento
            """, nativeQuery = true)
    List<AnomaliaProjection> detectarAnomalias(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipo") String tipo,
            @Param("personaId") Long personaId,
            @Param("limite") int limite,
            @Param("desplazamiento") long desplazamiento
    );

    @Query(value = ANOMALIAS_CTE + """
            SELECT COUNT(*)
            FROM anomalias a
            WHERE (CAST(:tipo AS varchar) IS NULL OR a.tipo = CAST(:tipo AS varchar))
              AND (CAST(:personaId AS bigint) IS NULL OR a.persona_id = CAST(:personaId AS bigint))
            """, nativeQuery = true)
    long contarAnomalias(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipo") String tipo,
            @Param("personaId") Long personaId
    );

    @Query(value = ANOMALIAS_CTE + """
            SELECT (SELECT COUNT(*) FROM base) AS "registrosAnalizados",
                   COUNT(*) AS "totalAnomalias",
                   COUNT(*) FILTER (WHERE tipo = 'ACCESO_NOCTURNO') AS "accesosNocturnos",
                   COUNT(*) FILTER (WHERE tipo = 'ACCESO_TEMPRANO') AS "accesosTempranos",
                   COUNT(*) FILTER (WHERE tipo = 'ACCESO_TARDIO') AS "accesosTardios",
                   COUNT(*) FILTER (WHERE tipo = 'ACCESOS_REPETITIVOS') AS "accesosRepetitivos",
                   COUNT(*) FILTER (WHERE tipo = 'RECHAZOS_REPETITIVOS') AS "rechazosRepetitivos",
                   COUNT(*) FILTER (WHERE tipo = 'DESVIACION_HORARIO') AS "desviacionesHorario"
            FROM anomalias
            """, nativeQuery = true)
    ResumenAnomaliasProjection resumirAnomalias(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
