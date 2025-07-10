package org.apache.guacamole.event;

import org.apache.guacamole.net.event.TunnelCloseEvent;
import org.apache.guacamole.net.event.TunnelConnectEvent;
import org.apache.guacamole.net.event.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class RemoteEventListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(RemoteEventListener.class);

    public RemoteEventListener() {
        logger.info("RemoteEventListener initialized");
    }

    @Override
    public void handleEvent(Object event) {
        try {
            if (event instanceof TunnelConnectEvent) {
                TunnelConnectEvent connectEvent = (TunnelConnectEvent) event;
                sendEvent("connect", connectEvent.getTunnel().getUUID().toString());
            } else if (event instanceof TunnelCloseEvent) {
                TunnelCloseEvent closeEvent = (TunnelCloseEvent) event;
                sendEvent("disconnect", closeEvent.getTunnel().getUUID().toString());
            }
        } catch (Exception ex) {
            logger.error("Error sending event", ex);
        }
    }

    private void sendEvent(String type, String connectionId) throws Exception {
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
    }
}
