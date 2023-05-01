package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.JsonEncoderBase;
import ch.qos.logback.core.util.DirectJson;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a concrete JsonEncoder for {@link ILoggingEvent} that emits fields according to object's configuration.
 * It is partially imported from <a href="https://github.com/hkupty/penna">penna</a>, but adapted to logback's structure.
 *
 * @author Henry John Kupty
 */
public class JsonEncoder extends JsonEncoderBase<ILoggingEvent> {


    // Excerpt below imported from
    // ch.qos.logback.contrib.json.classic.JsonLayout
    public static final String TIMESTAMP_ATTR_NAME = "timestamp";
    public static final String LEVEL_ATTR_NAME = "level";
    public static final String MARKERS_ATTR_NAME = "tags";
    public static final String THREAD_ATTR_NAME = "thread";
    public static final String MDC_ATTR_NAME = "mdc";
    public static final String LOGGER_ATTR_NAME = "logger";
    public static final String FORMATTED_MESSAGE_ATTR_NAME = "message";
    public static final String MESSAGE_ATTR_NAME = "raw-message";
    public static final String EXCEPTION_ATTR_NAME = "exception";
    public static final String CONTEXT_ATTR_NAME = "context";

    protected boolean includeLevel;
    protected boolean includeThreadName;
    protected boolean includeMDC;
    protected boolean includeLoggerName;
    protected boolean includeFormattedMessage;
    protected boolean includeMessage;
    protected boolean includeException;
    protected boolean includeContextName;

    private final List<Emitter<ILoggingEvent>> emitters;


    public JsonEncoder() {
        super();

        emitters = new ArrayList<>();
        this.includeLevel = true;
        this.includeThreadName = true;
        this.includeMDC = true;
        this.includeLoggerName = true;
        this.includeFormattedMessage = true;
        this.includeException = true;
        this.includeContextName = true;
    }

    //protected  = new DirectJson();


    public void writeMessage(DirectJson jsonWriter, ILoggingEvent event) {
        jsonWriter.writeStringValue(MESSAGE_ATTR_NAME, event.getMessage());
    }

    public void writeFormattedMessage(DirectJson jsonWriter, ILoggingEvent event) {
        jsonWriter.writeStringValue(FORMATTED_MESSAGE_ATTR_NAME, event.getFormattedMessage());
    }

    public void writeLogger(DirectJson jsonWriter, ILoggingEvent event) {
        jsonWriter.writeStringValue(LOGGER_ATTR_NAME, event.getLoggerName());
    }

    public void writeThreadName(DirectJson jsonWriter, ILoggingEvent event) {
        jsonWriter.writeStringValue(THREAD_ATTR_NAME, event.getThreadName());
    }

    public void writeLevel(DirectJson jsonWriter, ILoggingEvent event) {
        jsonWriter.writeStringValue(LEVEL_ATTR_NAME, event.getLevel().levelStr);
    }


    public void writeMarkers(DirectJson jsonWriter, ILoggingEvent event) {
        var markers = event.getMarkerList();
        if (!markers.isEmpty()) {
            jsonWriter.openArray(MARKERS_ATTR_NAME);
            for (var marker : markers) {
                jsonWriter.writeString(marker.getName());
                jsonWriter.writeSep();
            }
            // Close array will overwrite the last "," in the buffer, so we are OK
            jsonWriter.closeArray();
            jsonWriter.writeSep();
        }
    }

    public void writeMdc(DirectJson jsonWriter, ILoggingEvent event) {
        var mdc = event.getMDCPropertyMap();
        if (!mdc.isEmpty()) {
            jsonWriter.openObject(MDC_ATTR_NAME);
            for (var entry : mdc.entrySet()) {
                jsonWriter.writeStringValue(entry.getKey(), entry.getValue());
            }
            jsonWriter.closeObject();
            jsonWriter.writeSep();
        }
    }

    private void buildEmitterList() {
        // This method should be re-entrant and allow for reconfiguring the emitters if something change;
        emitters.clear();

        // TODO figure out order
        if (includeLevel) emitters.add(this::writeLevel);
        if (includeMDC) emitters.add(this::writeMdc);
        if (includeMessage) emitters.add(this::writeMessage);
        if (includeFormattedMessage) emitters.add(this::writeFormattedMessage);
        if (includeThreadName) emitters.add(this::writeThreadName);
        if (includeLoggerName) emitters.add(this::writeLogger);
        // TODO add fields missing:
        // context
        // exception
        // custom data
        // marker
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        if (emitters.isEmpty()) {
            buildEmitterList();
        }
        DirectJson jsonWriter = new DirectJson();
        jsonWriter.openObject();

        for (var emitter: emitters) {
            emitter.write(jsonWriter, event);
        }

        jsonWriter.closeObject();
        return jsonWriter.flush();
    }
}
