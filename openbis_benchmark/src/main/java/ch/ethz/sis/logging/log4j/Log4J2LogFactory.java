package ch.ethz.sis.logging.log4j;

import org.apache.logging.log4j.LogManager;

import ch.ethz.sis.logging.LogFactory;
import ch.ethz.sis.logging.Logger;

public class Log4J2LogFactory implements LogFactory
{
    @Override
    public <T> Logger getLogger(Class<T> clazz)
    {
        return (Logger) new Log4JLogger(LogManager.getLogger(clazz));
    }
}