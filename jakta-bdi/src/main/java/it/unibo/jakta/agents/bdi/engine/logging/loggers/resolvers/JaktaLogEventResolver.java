package it.unibo.jakta.agents.bdi.engine.logging.loggers.resolvers;

import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent;
import kotlinx.serialization.json.Json;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolver;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.MessageResolverFactory;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.util.JsonWriter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;

import static it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.resolveObjectMessage;

public final class JaktaLogEventResolver implements EventResolver {

    private final EventResolver internalResolver;
    private final Json json;

    JaktaLogEventResolver(final EventResolverContext context, final TemplateResolverConfig config) {
        this.internalResolver = MessageResolverFactory.getInstance().create(context, config);
        this.json = JaktaJsonComponent.getJson();
    }

    static String getName() {
        return "agentContext";
    }

    @Override
    public void resolve(final LogEvent logEvent, final JsonWriter jsonWriter) {
        Message message = logEvent.getMessage();

        if (message instanceof ObjectMessage) {
            resolveObjectMessage(json, (ObjectMessage) message, jsonWriter);
        } else {
            internalResolver.resolve(logEvent, jsonWriter);
        }
    }
}
