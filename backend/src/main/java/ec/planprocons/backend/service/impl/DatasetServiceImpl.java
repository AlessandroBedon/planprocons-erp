package ec.planprocons.backend.service.impl;

import ec.planprocons.backend.dto.request.DatasetGenerationRequest;
import ec.planprocons.backend.dto.response.DatasetGenerationResponse;
import ec.planprocons.backend.exception.BusinessException;
import ec.planprocons.backend.service.interfaces.DatasetService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.SplittableRandom;

@Service
public class DatasetServiceImpl implements DatasetService {

    private static final int TAMANO_LOTE = 5_000;
    private static final int MAXIMO_PERSONAS = 2_000;
    private static final int NUMERO_DISPOSITIVOS = 5;
    private static final int FRECUENCIA_ANOMALIAS = 100;

    private static final String[] NOMBRES = {
            "Andrea", "Carlos", "Daniela", "Diego", "Elena", "Fernando",
            "Gabriela", "Javier", "Laura", "Miguel", "Paola", "Santiago"
    };

    private static final String[] APELLIDOS = {
            "Almeida", "Benítez", "Cabrera", "Díaz", "Espinosa", "Flores",
            "García", "Herrera", "Ibarra", "Jiménez", "López", "Mendoza"
    };

    private static final String[] DEPARTAMENTOS = {
            "Administración", "Sistemas", "Ingeniería", "Secretaría",
            "Biblioteca", "Laboratorios", "Docencia", "Coordinación"
    };

    private static final String[] CARGOS = {
            "Estudiante", "Docente", "Administrativo",
            "Coordinador", "Técnico", "Auxiliar"
    };

    private static final String INSERTAR_REGISTRO = """
            INSERT INTO registros_acceso (
                activo, fecha_creacion, fecha_actualizacion,
                persona_id, dispositivo_id, fecha_hora,
                tipo_evento, metodo_verificacion, estado,
                codigo_evento, fecha_recepcion
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public DatasetServiceImpl(
            JdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public synchronized DatasetGenerationResponse generar(DatasetGenerationRequest request) {

        validarRango(request.getFechaInicio(), request.getFechaFin());

        long inicioNanos = System.nanoTime();
        String lote = crearIdentificadorLote(request);
        String prefijoEvento = "SYN-" + lote + "-";

        verificarLoteNoExistente(prefijoEvento);

        int cantidadPersonas = calcularCantidadPersonas(request.getCantidad());
        prepararDatosBase(cantidadPersonas);

        List<Long> personaIds = obtenerPersonasSinteticas(cantidadPersonas);
        List<Long> dispositivoIds = obtenerDispositivosSinteticos();

        if (personaIds.size() < cantidadPersonas) {
            throw new BusinessException("No fue posible preparar todas las personas sintéticas");
        }

        if (dispositivoIds.size() < NUMERO_DISPOSITIVOS) {
            throw new BusinessException("No fue posible preparar todos los dispositivos sintéticos");
        }

        SplittableRandom random = new SplittableRandom(request.getSeed());
        SelectorDias selectorDias = crearSelectorDias(
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getSeed()
        );

        int insertados = 0;

        try {
            while (insertados < request.getCantidad()) {
                int cantidadLote = Math.min(
                        TAMANO_LOTE,
                        request.getCantidad() - insertados
                );

                List<AccesoSintetico> loteRegistros = new ArrayList<>(cantidadLote);

                for (int desplazamiento = 0; desplazamiento < cantidadLote; desplazamiento++) {
                    int indiceGlobal = insertados + desplazamiento;
                    loteRegistros.add(generarAcceso(
                            indiceGlobal,
                            prefijoEvento,
                            personaIds,
                            dispositivoIds,
                            selectorDias,
                            random
                    ));
                }

                insertarLote(loteRegistros);
                insertados += cantidadLote;
            }
        } catch (RuntimeException ex) {
            limpiarLoteIncompleto(prefijoEvento);
            throw ex;
        }

        long tiempoMs = Math.max(
                1L,
                (System.nanoTime() - inicioNanos) / 1_000_000L
        );
        double registrosPorSegundo = redondear(
                insertados * 1_000.0 / tiempoMs
        );

        return DatasetGenerationResponse.builder()
                .lote(lote)
                .solicitados(request.getCantidad())
                .insertados(insertados)
                .personasDisponibles(personaIds.size())
                .dispositivosDisponibles(dispositivoIds.size())
                .tiempoMs(tiempoMs)
                .registrosPorSegundo(registrosPorSegundo)
                .build();
    }

    private void validarRango(LocalDate fechaInicio, LocalDate fechaFin) {

        if (fechaInicio.isAfter(fechaFin)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private void verificarLoteNoExistente(String prefijoEvento) {

        Long existentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM registros_acceso WHERE codigo_evento LIKE ?",
                Long.class,
                prefijoEvento + "%"
        );

        if (existentes != null && existentes > 0) {
            throw new BusinessException(
                    "El dataset ya fue generado para estos parámetros. Lote: "
                            + prefijoEvento.substring(4, prefijoEvento.length() - 1)
            );
        }
    }

    private int calcularCantidadPersonas(int cantidadRegistros) {

        if (cantidadRegistros <= 10_000) {
            return 500;
        }

        if (cantidadRegistros <= 100_000) {
            return 1_000;
        }

        return MAXIMO_PERSONAS;
    }

    private void prepararDatosBase(int cantidadPersonas) {

        transactionTemplate.executeWithoutResult(status -> {
            insertarPersonasSinteticas(cantidadPersonas);
            insertarDispositivosSinteticos();
        });
    }

    private void insertarPersonasSinteticas(int cantidadPersonas) {

        List<Integer> indices = new ArrayList<>(cantidadPersonas);
        for (int i = 1; i <= cantidadPersonas; i++) {
            indices.add(i);
        }

        LocalDateTime ahora = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO personas (
                    activo, fecha_creacion, fecha_actualizacion,
                    codigo_biometrico, cedula, nombres, apellidos,
                    departamento, cargo
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT DO NOTHING
                """,
                indices,
                TAMANO_LOTE,
                (ps, indice) -> {
                    ps.setBoolean(1, true);
                    ps.setTimestamp(2, Timestamp.valueOf(ahora));
                    ps.setTimestamp(3, Timestamp.valueOf(ahora));
                    ps.setString(4, "SYN-P-" + String.format("%06d", indice));
                    ps.setString(5, "9" + String.format("%09d", indice));
                    ps.setString(6, NOMBRES[(indice - 1) % NOMBRES.length]);
                    ps.setString(7, APELLIDOS[(indice * 5) % APELLIDOS.length]
                            + " " + APELLIDOS[(indice * 7 + 1) % APELLIDOS.length]);
                    ps.setString(8, DEPARTAMENTOS[(indice * 3) % DEPARTAMENTOS.length]);
                    ps.setString(9, CARGOS[(indice * 5) % CARGOS.length]);
                }
        );
    }

    private void insertarDispositivosSinteticos() {

        String[][] dispositivos = {
                {"SYN-ZK-01", "Entrada Principal", "ZKTeco Synthetic V1", "SYN-SERIAL-01", "192.0.2.11", "Entrada Principal"},
                {"SYN-ZK-02", "Salida Principal", "ZKTeco Synthetic V1", "SYN-SERIAL-02", "192.0.2.12", "Salida Principal"},
                {"SYN-ZK-03", "Biblioteca", "ZKTeco Synthetic V1", "SYN-SERIAL-03", "192.0.2.13", "Biblioteca"},
                {"SYN-ZK-04", "Laboratorio", "ZKTeco Synthetic V1", "SYN-SERIAL-04", "192.0.2.14", "Laboratorios"},
                {"SYN-ZK-05", "Edificio Administrativo", "ZKTeco Synthetic V1", "SYN-SERIAL-05", "192.0.2.15", "Administración"}
        };

        LocalDateTime ahora = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO dispositivos (
                    activo, fecha_creacion, fecha_actualizacion,
                    codigo, nombre, modelo, serial, ip, ubicacion, ultimo_contacto
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT DO NOTHING
                """,
                List.of(dispositivos),
                NUMERO_DISPOSITIVOS,
                (ps, dispositivo) -> {
                    ps.setBoolean(1, true);
                    ps.setTimestamp(2, Timestamp.valueOf(ahora));
                    ps.setTimestamp(3, Timestamp.valueOf(ahora));
                    ps.setString(4, dispositivo[0]);
                    ps.setString(5, dispositivo[1]);
                    ps.setString(6, dispositivo[2]);
                    ps.setString(7, dispositivo[3]);
                    ps.setString(8, dispositivo[4]);
                    ps.setString(9, dispositivo[5]);
                    ps.setTimestamp(10, Timestamp.valueOf(ahora));
                }
        );
    }

    private List<Long> obtenerPersonasSinteticas(int limite) {

        return jdbcTemplate.queryForList(
                """
                SELECT id
                FROM personas
                WHERE codigo_biometrico LIKE 'SYN-P-%'
                ORDER BY codigo_biometrico
                LIMIT ?
                """,
                Long.class,
                limite
        );
    }

    private List<Long> obtenerDispositivosSinteticos() {

        return jdbcTemplate.queryForList(
                """
                SELECT id
                FROM dispositivos
                WHERE codigo LIKE 'SYN-ZK-%'
                ORDER BY codigo
                LIMIT 5
                """,
                Long.class
        );
    }

    private AccesoSintetico generarAcceso(
            int indice,
            String prefijoEvento,
            List<Long> personaIds,
            List<Long> dispositivoIds,
            SelectorDias selectorDias,
            SplittableRandom random
    ) {

        boolean anomalo = indice % FRECUENCIA_ANOMALIAS == 0;
        int personaIndice = seleccionarPersona(personaIds.size(), random);
        LocalDate fecha = selectorDias.seleccionar(random);
        boolean entrada = random.nextBoolean();
        boolean rechazoForzado = false;
        int minutoDia;

        if (anomalo) {
            int numeroAnomalia = indice / FRECUENCIA_ANOMALIAS;
            int tipoAnomalia = numeroAnomalia % 5;

            switch (tipoAnomalia) {
                case 0 -> minutoDia = random.nextInt(0, 4 * 60);
                case 1 -> minutoDia = random.nextInt(4 * 60, 5 * 60 + 31);
                case 2 -> minutoDia = random.nextInt(22 * 60, 24 * 60);
                case 3 -> {
                    int ocurrencia = numeroAnomalia / 5;
                    int grupo = ocurrencia / 3;
                    personaIndice = grupo % personaIds.size();
                    fecha = selectorDias.fechaPorIndice(grupo);
                    minutoDia = 8 * 60 + ocurrencia % 3;
                }
                default -> {
                    int ocurrencia = numeroAnomalia / 5;
                    int grupo = ocurrencia / 3;
                    personaIndice = grupo % personaIds.size();
                    fecha = selectorDias.fechaPorIndice(grupo);
                    minutoDia = 10 * 60 + ocurrencia % 3;
                    rechazoForzado = true;
                }
            }
        } else {
            minutoDia = generarMinutoNormal(personaIndice, entrada, random);
        }

        LocalDateTime fechaHora = LocalDateTime.of(
                fecha,
                LocalTime.of(minutoDia / 60, minutoDia % 60, random.nextInt(60))
        );

        int dispositivoIndice = seleccionarDispositivo(entrada, dispositivoIds.size(), random);
        String estado = rechazoForzado || random.nextDouble() < 0.02
                ? "RECHAZADO"
                : "PERMITIDO";
        String metodo = seleccionarMetodo(random);
        String tipoEvento = entrada ? "ENTRADA" : "SALIDA";
        String codigoEvento = prefijoEvento
                + "ZK" + String.format("%02d", dispositivoIndice + 1)
                + "-" + String.format("%09d", indice + 1);

        long latenciaSegundos = random.nextDouble() < 0.95
                ? random.nextLong(6)
                : random.nextLong(60, 1_801);
        LocalDateTime fechaRecepcion = fechaHora.plusSeconds(latenciaSegundos);

        return new AccesoSintetico(
                personaIds.get(personaIndice),
                dispositivoIds.get(dispositivoIndice),
                fechaHora,
                tipoEvento,
                metodo,
                estado,
                codigoEvento,
                fechaRecepcion
        );
    }

    private int seleccionarPersona(int cantidadPersonas, SplittableRandom random) {

        int corteFrecuentes = Math.max(1, (int) (cantidadPersonas * 0.8));

        if (random.nextDouble() < 0.9) {
            return random.nextInt(corteFrecuentes);
        }

        return random.nextInt(corteFrecuentes, cantidadPersonas);
    }

    private int generarMinutoNormal(
            int personaIndice,
            boolean entrada,
            SplittableRandom random
    ) {

        int perfil = personaIndice % 10;
        int centro;
        int amplitud;

        if (perfil <= 1) {
            centro = entrada ? 6 * 60 + 50 : 16 * 60 + 30;
            amplitud = entrada ? 45 : 60;
        } else if (perfil <= 7) {
            centro = entrada ? 7 * 60 + 30 : 17 * 60 + 15;
            amplitud = entrada ? 75 : 90;
        } else if (perfil == 8) {
            centro = entrada ? 9 * 60 + 15 : 19 * 60;
            amplitud = 75;
        } else {
            centro = entrada ? 8 * 60 : 17 * 60 + 30;
            amplitud = 100;
        }

        double variacion = random.nextDouble()
                + random.nextDouble()
                + random.nextDouble()
                - 1.5;
        int minuto = centro + (int) Math.round(variacion * amplitud);

        return Math.max(5 * 60, Math.min(21 * 60 - 1, minuto));
    }

    private int seleccionarDispositivo(
            boolean entrada,
            int cantidadDispositivos,
            SplittableRandom random
    ) {

        if (random.nextDouble() < 0.75) {
            return entrada ? 0 : 1;
        }

        return random.nextInt(2, cantidadDispositivos);
    }

    private String seleccionarMetodo(SplittableRandom random) {

        double valor = random.nextDouble();

        if (valor < 0.60) {
            return "HUELLA";
        }
        if (valor < 0.80) {
            return "ROSTRO";
        }
        if (valor < 0.95) {
            return "TARJETA";
        }
        if (valor < 0.99) {
            return "PIN";
        }
        return "OTRO";
    }

    private SelectorDias crearSelectorDias(
            LocalDate inicio,
            LocalDate fin,
            long seed
    ) {

        List<LocalDate> fechas = inicio.datesUntil(fin.plusDays(1)).toList();
        double[] acumulados = new double[fechas.size()];
        SplittableRandom variacion = new SplittableRandom(seed ^ 0x5DEECE66DL);
        double total = 0.0;

        for (int i = 0; i < fechas.size(); i++) {
            double peso = pesoDiaSemana(fechas.get(i).getDayOfWeek());
            peso *= 0.90 + variacion.nextDouble() * 0.20;
            total += peso;
            acumulados[i] = total;
        }

        return new SelectorDias(fechas, acumulados, total);
    }

    private double pesoDiaSemana(DayOfWeek dia) {

        return switch (dia) {
            case MONDAY -> 1.15;
            case TUESDAY, WEDNESDAY, THURSDAY -> 1.00;
            case FRIDAY -> 1.10;
            case SATURDAY -> 0.55;
            case SUNDAY -> 0.25;
        };
    }

    private void insertarLote(List<AccesoSintetico> registros) {

        transactionTemplate.executeWithoutResult(status ->
                jdbcTemplate.batchUpdate(
                        INSERTAR_REGISTRO,
                        registros,
                        TAMANO_LOTE,
                        this::asignarParametros
                )
        );
    }

    private void asignarParametros(
            PreparedStatement ps,
            AccesoSintetico acceso
    ) throws java.sql.SQLException {

        Timestamp recepcion = Timestamp.valueOf(acceso.fechaRecepcion());

        ps.setBoolean(1, true);
        ps.setTimestamp(2, recepcion);
        ps.setTimestamp(3, recepcion);
        ps.setLong(4, acceso.personaId());
        ps.setLong(5, acceso.dispositivoId());
        ps.setTimestamp(6, Timestamp.valueOf(acceso.fechaHora()));
        ps.setString(7, acceso.tipoEvento());
        ps.setString(8, acceso.metodo());
        ps.setString(9, acceso.estado());
        ps.setString(10, acceso.codigoEvento());
        ps.setTimestamp(11, recepcion);
    }

    private void limpiarLoteIncompleto(String prefijoEvento) {

        transactionTemplate.executeWithoutResult(status ->
                jdbcTemplate.update(
                        "DELETE FROM registros_acceso WHERE codigo_evento LIKE ?",
                        prefijoEvento + "%"
                )
        );
    }

    private String crearIdentificadorLote(DatasetGenerationRequest request) {

        String parametros = request.getSeed()
                + "|" + request.getFechaInicio()
                + "|" + request.getFechaFin()
                + "|" + request.getCantidad();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(parametros.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 6).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private double redondear(double valor) {

        return Math.round(valor * 100.0) / 100.0;
    }

    private record AccesoSintetico(
            long personaId,
            long dispositivoId,
            LocalDateTime fechaHora,
            String tipoEvento,
            String metodo,
            String estado,
            String codigoEvento,
            LocalDateTime fechaRecepcion
    ) {
    }

    private record SelectorDias(
            List<LocalDate> fechas,
            double[] acumulados,
            double total
    ) {

        private LocalDate seleccionar(SplittableRandom random) {
            double objetivo = random.nextDouble(total);
            int izquierda = 0;
            int derecha = acumulados.length - 1;

            while (izquierda < derecha) {
                int medio = (izquierda + derecha) >>> 1;
                if (objetivo < acumulados[medio]) {
                    derecha = medio;
                } else {
                    izquierda = medio + 1;
                }
            }

            return fechas.get(izquierda);
        }

        private LocalDate fechaPorIndice(int indice) {
            return fechas.get(Math.floorMod(indice, fechas.size()));
        }
    }
}
