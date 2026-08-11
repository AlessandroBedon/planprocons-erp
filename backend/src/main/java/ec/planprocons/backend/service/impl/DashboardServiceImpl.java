package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.response.AccesoPorDiaResponse;
import ec.planprocons.backend.dto.response.AccesoPorHoraResponse;
import ec.planprocons.backend.dto.response.DashboardResumenResponse;
import ec.planprocons.backend.dto.response.PersonaFrecuenteResponse;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.repository.RegistroAccesoRepository;
import ec.planprocons.backend.repository.projection.DashboardResumenProjection;
import ec.planprocons.backend.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DIAS_PREDETERMINADOS = 7;
    private static final int LIMITE_MAXIMO_PERSONAS = 100;

    private final RegistroAccesoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponse obtenerResumen(LocalDate fecha) {

        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        RangoFechaHora rango = rangoDeUnDia(fechaConsulta);
        DashboardResumenProjection resumen = repository.obtenerResumen(
                rango.inicio(),
                rango.fin()
        );
        List<AccesoPorHoraResponse> accesosPorHora = consultarAccesosPorHora(rango);

        AccesoPorHoraResponse pico = accesosPorHora.stream()
                .min(Comparator
                        .comparingLong(AccesoPorHoraResponse::getCantidad)
                        .reversed()
                        .thenComparingInt(AccesoPorHoraResponse::getHora))
                .orElse(null);

        return DashboardResumenResponse.builder()
                .totalAccesos(resumen.getTotalAccesos())
                .entradas(resumen.getEntradas())
                .salidas(resumen.getSalidas())
                .permitidos(resumen.getPermitidos())
                .rechazados(resumen.getRechazados())
                .personasUnicas(resumen.getPersonasUnicas())
                .horaPico(pico != null ? pico.getHora() : null)
                .cantidadHoraPico(pico != null ? pico.getCantidad() : 0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccesoPorHoraResponse> obtenerAccesosPorHora(LocalDate fecha) {

        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();

        return consultarAccesosPorHora(rangoDeUnDia(fechaConsulta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccesoPorDiaResponse> obtenerAccesosPorDia(
            LocalDate desde,
            LocalDate hasta
    ) {

        RangoFechaHora rango = resolverRango(desde, hasta);

        return repository.contarAccesosPorDia(rango.inicio(), rango.fin())
                .stream()
                .map(fila -> new AccesoPorDiaResponse(
                        aLocalDate(fila[0]),
                        aLong(fila[1])
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaFrecuenteResponse> obtenerPersonasFrecuentes(
            LocalDate desde,
            LocalDate hasta,
            int limite
    ) {

        if (limite < 1 || limite > LIMITE_MAXIMO_PERSONAS) {
            throw new BusinessException("El límite debe estar entre 1 y 100");
        }

        RangoFechaHora rango = resolverRango(desde, hasta);

        return repository.encontrarPersonasFrecuentes(
                        rango.inicio(),
                        rango.fin(),
                        limite
                )
                .stream()
                .map(fila -> PersonaFrecuenteResponse.builder()
                        .personaId(aLong(fila[0]))
                        .codigoBiometrico((String) fila[1])
                        .nombre(fila[2] + " " + fila[3])
                        .cantidadAccesos(aLong(fila[4]))
                        .build())
                .toList();
    }

    private List<AccesoPorHoraResponse> consultarAccesosPorHora(RangoFechaHora rango) {

        return repository.contarAccesosPorHora(rango.inicio(), rango.fin())
                .stream()
                .map(fila -> new AccesoPorHoraResponse(
                        ((Number) fila[0]).intValue(),
                        aLong(fila[1])
                ))
                .toList();
    }

    private RangoFechaHora resolverRango(LocalDate desde, LocalDate hasta) {

        LocalDate fechaHasta = hasta != null ? hasta : LocalDate.now();
        LocalDate fechaDesde = desde != null
                ? desde
                : fechaHasta.minusDays(DIAS_PREDETERMINADOS - 1L);

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new BusinessException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        return new RangoFechaHora(
                fechaDesde.atStartOfDay(),
                fechaHasta.plusDays(1).atStartOfDay()
        );
    }

    private RangoFechaHora rangoDeUnDia(LocalDate fecha) {

        return new RangoFechaHora(
                fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay()
        );
    }

    private long aLong(Object valor) {

        return valor == null ? 0L : ((Number) valor).longValue();
    }

    private LocalDate aLocalDate(Object valor) {

        if (valor instanceof LocalDate localDate) {
            return localDate;
        }

        if (valor instanceof Date fechaSql) {
            return fechaSql.toLocalDate();
        }

        return LocalDate.parse(valor.toString());
    }

    private record RangoFechaHora(LocalDateTime inicio, LocalDateTime fin) {
    }
}
