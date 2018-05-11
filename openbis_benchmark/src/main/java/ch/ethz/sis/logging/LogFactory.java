package ch.ethz.sis.logging;

public interface LogFactory {
    <T> Logger getLogger(Class<T> clazz);
}