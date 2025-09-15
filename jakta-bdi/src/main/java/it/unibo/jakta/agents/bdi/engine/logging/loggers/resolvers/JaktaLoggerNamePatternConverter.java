package it.unibo.jakta.agents.bdi.engine.logging.loggers.resolvers;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.*;
import org.apache.logging.log4j.util.PerformanceSensitive;

import static it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.extractLastComponent;

@Plugin(name = "JaktaLoggerNamePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"c", "logger"})
@PerformanceSensitive("allocation")
public class JaktaLoggerNamePatternConverter extends NamePatternConverter {

    private static final JaktaLoggerNamePatternConverter INSTANCE = new JaktaLoggerNamePatternConverter(null);

    private JaktaLoggerNamePatternConverter(final String[] options) {
        super("Logger", "logger", options);
    }

    public static JaktaLoggerNamePatternConverter newInstance(final String[] options) {
        if (options == null || options.length == 0) {
            return INSTANCE;
        }
        return new JaktaLoggerNamePatternConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String eventName = event.getLoggerName();
        String name = extractLastComponent(eventName);
        abbreviate(name, toAppendTo);
    }
}