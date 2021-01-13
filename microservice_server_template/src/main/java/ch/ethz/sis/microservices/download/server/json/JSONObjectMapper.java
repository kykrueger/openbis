package ch.ethz.sis.microservices.download.server.json;

import java.io.InputStream;

public interface JSONObjectMapper
{

    <T> T readValue(InputStream src, Class<T> valueType) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
