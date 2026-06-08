package util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Punto único de acceso a un ObjectMapper de Jackson reutilizable.
 *
 * Antes cada vista creaba su propio {@code new ObjectMapper()} (caro y
 * repetido). Controllers y dispatcher comparten esta instancia.
 */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
