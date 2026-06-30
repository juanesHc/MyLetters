package com.myletters.notes.config.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;
import java.util.UUID;

/**
 * Mapeo UUID &lt;-&gt; String para MongoDB.
 *
 * El código sigue usando UUID en todas partes (id, ownerId, queries); solo cambia CÓMO se
 * almacena: como texto plano en lugar de binario nativo. Así evitamos el CodecConfigurationException
 * ("uuidRepresentation has not been specified") y, de paso, los ids quedan legibles en Mongo.
 *
 * Spring Data aplica estos converters en escritura, lectura Y al construir las queries (el
 * QueryMapper convierte el UUID del criterio a String antes de buscar), por eso no hay que tocar
 * documentos, services ni repositorios.
 */
@Configuration
public class MongoUuidConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new UuidToStringConverter(),
                new StringToUuidConverter()
        ));
    }

    /** Al guardar: UUID -> String. Afecta a id y ownerId. */
    @WritingConverter
    static class UuidToStringConverter implements Converter<UUID, String> {
        @Override
        public String convert(UUID source) {
            return source.toString();
        }
    }

    /** Al leer: String -> UUID. Solo se aplica cuando el campo destino es UUID. */
    @ReadingConverter
    static class StringToUuidConverter implements Converter<String, UUID> {
        @Override
        public UUID convert(String source) {
            return UUID.fromString(source);
        }
    }
}
