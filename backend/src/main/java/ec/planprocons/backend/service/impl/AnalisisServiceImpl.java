package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.analysis.enums.NivelAnomalia;
import ec.planprocons.backend.analysis.enums.TipoAnomalia;
import ec.planprocons.backend.dto.response.AnomaliaResponse;
import ec.planprocons.backend.dto.response.PatronGeneralResponse;
import ec.planprocons.backend.dto.response.PatronPersonaResponse;
import ec.planprocons.backend.dto.response.ResumenAnomaliasResponse;
import ec.planprocons.backend.entity.Persona;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.exception.ResourceNotFoundException;
import ec.planprocons.backend.repository.PersonaRepository;
import ec.planprocons.backend.repository.RegistroAccesoRepository;
import ec.planprocons.backend.repository.projection.AnomaliaProjection;
import ec.planprocons.backend.repository.projection.PatronGeneralProjection;
import ec.planprocons.backend.repository.projection.PatronPersonaProjection;
import ec.planprocons.backend.repository.projection.ResumenAnomaliasProjection;
import ec.planprocons.backend.service.interfaces.AnalisisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalisisServiceImpl implements AnalisisService {

    private static final int MINIMO_DIAS_HISTORICO = 10;
    private static final int TAMANO_MAXIMO_PAGINA = 100;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistroAccesoRepository registroRepository;
    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public PatronGeneralResponse obtenerPatrones(LocalDate desde, LocalDate hasta) {

        Rango rango = validarRango(desde, hasta);
        PatronGeneralProjection patron = registroRepository.obtenerPatronesGenerales(
                rango.inicio(),
                rango.fin()
        );

        return PatronGeneralResponse.builder()
                .desde(desde)
                .hasta(hasta)
                .registrosAnalizados(aLong(patron.getRegistrosAnalizados()))
                .horaPicoGeneral(patron.getHoraPicoGeneral())
                .cantidadHoraPicoGeneral(aLong(patron.getCantidadHoraPicoGeneral()))
                .horaPicoEntradas(patron.getHoraPicoEntradas())
                .cantidadHoraPicoEntradas(aLong(patron.getCantidadHoraPicoEntradas()))
                .horaPicoSalidas(patron.getHoraPicoSalidas())
                .cantidadHoraPicoSalidas(aLong(patron.getCantidadHoraPicoSalidas()))
                .fechaMayorActividad(patron.getFechaMayorActividad())
                .cantidadFechaMayorActividad(aLong(patron.getCantidadFechaMayorActividad()))
                .diaSemanaMayorActividad(nombreDia(patron.getDiaSemanaMayorActividad()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PatronPersonaResponse obtenerPatronPersona(
            Long personaId,
            LocalDate desde,
            LocalDate hasta
    ) {

        Rango rango = validarRango(desde, hasta);
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));
        PatronPersonaProjection patron = registroRepository.obtenerPatronPersona(
                personaId,
                rango.inicio(),
                rango.fin()
        );

        long diasConEntrada = aLong(patron.getDiasConEntrada());

        return PatronPersonaResponse.builder()
                .personaId(persona.getId())
                .codigoBiometrico(persona.getCodigoBiometrico())
                .nombre(persona.getNombres() + " " + persona.getApellidos())
                .desde(desde)
                .hasta(hasta)
                .horaHabitualEntrada(formatearMinutos(patron.getMinutoHabitualEntrada()))
                .horaHabitualSalida(formatearMinutos(patron.getMinutoHabitualSalida()))
                .promedioAccesosDiarios(redondear(patron.getPromedioAccesosDiarios()))
                .diasAnalizados(aLong(patron.getDiasAnalizados()))
                .diasConEntrada(diasConEntrada)
                .informacionSuficiente(diasConEntrada >= MINIMO_DIAS_HISTORICO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnomaliaResponse> obtenerAnomalias(
            LocalDate desde,
            LocalDate hasta,
            TipoAnomalia tipo,
            Long personaId,
            int pagina,
            int tamano
    ) {

        Rango rango = validarRango(desde, hasta);
        validarPaginacion(pagina, tamano);

        String tipoConsulta = tipo != null ? tipo.name() : null;
        long desplazamiento = (long) pagina * tamano;
        List<AnomaliaResponse> contenido = registroRepository.detectarAnomalias(
                        rango.inicio(),
                        rango.fin(),
                        tipoConsulta,
                        personaId,
                        tamano,
                        desplazamiento
                )
                .stream()
                .map(this::aResponse)
                .toList();
        long total = registroRepository.contarAnomalias(
                rango.inicio(),
                rango.fin(),
                tipoConsulta,
                personaId
        );

        return new PageImpl<>(contenido, PageRequest.of(pagina, tamano), total);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenAnomaliasResponse obtenerResumenAnomalias(
            LocalDate desde,
            LocalDate hasta
    ) {

        Rango rango = validarRango(desde, hasta);
        ResumenAnomaliasProjection resumen = registroRepository.resumirAnomalias(
                rango.inicio(),
                rango.fin()
        );

        return ResumenAnomaliasResponse.builder()
                .desde(desde)
                .hasta(hasta)
                .registrosAnalizados(aLong(resumen.getRegistrosAnalizados()))
                .totalAnomalias(aLong(resumen.getTotalAnomalias()))
                .accesosNocturnos(aLong(resumen.getAccesosNocturnos()))
                .accesosTempranos(aLong(resumen.getAccesosTempranos()))
                .accesosTardios(aLong(resumen.getAccesosTardios()))
                .accesosRepetitivos(aLong(resumen.getAccesosRepetitivos()))
                .rechazosRepetitivos(aLong(resumen.getRechazosRepetitivos()))
                .desviacionesHorario(aLong(resumen.getDesviacionesHorario()))
                .build();
    }

    private AnomaliaResponse aResponse(AnomaliaProjection fila) {

        TipoAnomalia tipo = TipoAnomalia.valueOf(fila.getTipo());

        return AnomaliaResponse.builder()
                .tipo(tipo)
                .nivel(nivel(tipo))
                .personaId(fila.getPersonaId())
                .codigoPersona(fila.getCodigoPersona())
                .nombrePersona(fila.getNombres() + " " + fila.getApellidos())
                .registroAccesoId(fila.getRegistroAccesoId())
                .fechaHora(fila.getFechaHora())
                .descripcion(descripcion(tipo))
                .build();
    }

    private NivelAnomalia nivel(TipoAnomalia tipo) {

        return switch (tipo) {
            case ACCESO_NOCTURNO, ACCESO_TARDIO, RECHAZOS_REPETITIVOS -> NivelAnomalia.ALTO;
            case ACCESO_TEMPRANO, ACCESOS_REPETITIVOS, DESVIACION_HORARIO -> NivelAnomalia.MEDIO;
        };
    }

    private String descripcion(TipoAnomalia tipo) {

        return switch (tipo) {
            case ACCESO_NOCTURNO -> "Acceso registrado entre las 00:00 y las 04:59";
            case ACCESO_TEMPRANO -> "Acceso registrado entre las 05:00 y las 05:59";
            case ACCESO_TARDIO -> "Acceso registrado entre las 22:00 y las 23:59";
            case ACCESOS_REPETITIVOS -> "Tercer o posterior evento dentro de una ventana de 5 minutos";
            case RECHAZOS_REPETITIVOS -> "Tercer o posterior rechazo dentro de una ventana de 10 minutos";
            case DESVIACION_HORARIO -> "Primera entrada diaria desviada más de 120 minutos del horario habitual";
        };
    }

    private Rango validarRango(LocalDate desde, LocalDate hasta) {

        if (desde == null || hasta == null) {
            throw new BusinessException("Las fechas desde y hasta son obligatorias");
        }

        if (desde.isAfter(hasta)) {
            throw new BusinessException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        return new Rango(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    private void validarPaginacion(int pagina, int tamano) {

        if (pagina < 0) {
            throw new BusinessException("La página no puede ser negativa");
        }

        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new BusinessException("El tamaño de página debe estar entre 1 y 100");
        }
    }

    private String formatearMinutos(Double minutos) {

        if (minutos == null) {
            return null;
        }

        int minutoDia = Math.floorMod((int) Math.round(minutos), 24 * 60);
        return LocalTime.of(minutoDia / 60, minutoDia % 60).format(FORMATO_HORA);
    }

    private String nombreDia(Integer numeroDia) {

        if (numeroDia == null) {
            return null;
        }

        DayOfWeek dia = DayOfWeek.of(numeroDia);
        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    private long aLong(Long valor) {
        return valor != null ? valor : 0L;
    }

    private double redondear(Double valor) {
        return valor == null ? 0.0 : Math.round(valor * 100.0) / 100.0;
    }

    private record Rango(LocalDateTime inicio, LocalDateTime fin) {
    }
}
