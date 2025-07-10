package org.apache.guacamole.websocket;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.protocol.GuacamoleInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class InterceptingGuacamoleReader implements GuacamoleReader {

    private static final Logger logger = LoggerFactory.getLogger(InterceptingGuacamoleTunnel.class);

    private final GuacamoleReader delegate;
    private final String connectionId;

    public InterceptingGuacamoleReader(GuacamoleReader delegate, String connectionId) {
        this.delegate = delegate;
        this.connectionId = connectionId;
    }

    @Override
    public boolean available() throws GuacamoleException {
        return delegate.available();
    }

    @Override
    public char[] read() throws GuacamoleException {
        return delegate.read();
    }

    @Override
    public GuacamoleInstruction readInstruction() throws GuacamoleException {
        GuacamoleInstruction instruction = delegate.readInstruction();

        if (instruction != null) {
            String opcode = instruction.getOpcode();

            switch (opcode) {
                case "mouse":
                    logger.info("Mouse event: {}", instruction);
                    sendEvent("mouse", connectionId);
                    break;
                case "key":
                    logger.info("Keyboard event: {}", instruction);
                    sendEvent("key", connectionId);
                    break;
                case "touch":
                    logger.info("Touch event: {}", instruction);
                    sendEvent("touch", connectionId);
                    break;
                default:
                    // Другие события можно обработать при необходимости
                    break;
            }
        }

        return instruction;
    }

    private void sendEvent(String type, String connectionId) {
        try {
            URL url = new URI("http://app/api/guac-events").toURL();
            String json = String.format("{\"type\":\"%s\",\"connectionId\":\"%s\"}", type, connectionId);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes());
                }

                int responseCode = connection.getResponseCode();
                logger.info("Sent event {} for connection {}, response: {}", type, connectionId, responseCode);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.error("Error sending event " + type + " for connection " + connectionId, e);
        }
    }
}
