package operaciones;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AcTemperaturas {

    public static List<RegistrodeTemperaturas> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy H:mm");
        try (Stream<String> lineas = Files.lines(Paths.get(nombreArchivo))) {
            return lineas.skip(1)
                    .map(linea -> linea.split(";"))
                    .filter(partes -> partes.length >= 5)
                    .map(textos -> new RegistrodeTemperaturas(
                            textos[4].trim(), // ciudad
                            textos[0].trim(), // nombre de la estacion de la que se obtuvo el registro
                            Double.parseDouble(textos[3].trim()), // valor (temperatura)
                            LocalDate.parse(textos[2].trim(), formatoFecha))) // fecha
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static List<RegistrodeTemperaturas> filtrar(String ciudad, LocalDate desde, LocalDate hasta,
            List<RegistrodeTemperaturas> registros) {
        return registros.stream()
                .filter(r -> r.getCiudad().equalsIgnoreCase(ciudad)
                        && !r.getFecha().isBefore(desde)
                        && !r.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public static List<String> getCiudades(List<RegistrodeTemperaturas> registros) {
        return registros.stream()
                .map(RegistrodeTemperaturas::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static double getPromedio(List<RegistrodeTemperaturas> registros) {
        return registros.isEmpty() ? 0
                : registros.stream()
                        .mapToDouble(RegistrodeTemperaturas::getValor)
                        .average()
                        .orElse(0);
    }

    public static double getMaximo(List<RegistrodeTemperaturas> registros) {
        return registros.isEmpty() ? 0
                : registros.stream()
                        .mapToDouble(RegistrodeTemperaturas::getValor)
                        .max()
                        .orElse(0);
    }

    public static double getMinimo(List<RegistrodeTemperaturas> registros) {
        return registros.isEmpty() ? 0
                : registros.stream()
                        .mapToDouble(RegistrodeTemperaturas::getValor)
                        .min()
                        .orElse(0);
    }

    public static List<RegistrodeTemperaturas> filtrarPorRango(LocalDate desde, LocalDate hasta,
            List<RegistrodeTemperaturas> registros) {
        return registros.stream()
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public static String getCiudadMasCalurosa(LocalDate fecha, List<RegistrodeTemperaturas> registros) {
        return registros.stream()
                .filter(r -> r.getFecha().isEqual(fecha))
                .collect(Collectors.groupingBy(RegistrodeTemperaturas::getCiudad,
                        Collectors.averagingDouble(RegistrodeTemperaturas::getValor)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    public static String getCiudadMasFria(LocalDate fecha, List<RegistrodeTemperaturas> registros) {
        return registros.stream()
                .filter(r -> r.getFecha().isEqual(fecha))
                .collect(Collectors.groupingBy(RegistrodeTemperaturas::getCiudad,
                        Collectors.averagingDouble(RegistrodeTemperaturas::getValor)))
                .entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin datos");
    }

    public static Map<String, Double> getPromedioPorCiudad(LocalDate desde, LocalDate hasta,
            List<RegistrodeTemperaturas> registros) {
        return filtrarPorRango(desde, hasta, registros).stream()
                .collect(Collectors.groupingBy(RegistrodeTemperaturas::getCiudad,
                        Collectors.averagingDouble(RegistrodeTemperaturas::getValor)));
    }

}
