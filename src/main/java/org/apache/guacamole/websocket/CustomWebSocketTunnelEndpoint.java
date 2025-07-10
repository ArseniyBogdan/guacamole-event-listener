package org.apache.guacamole.websocket;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomWebSocketTunnelEndpoint extends GuacamoleWebSocketTunnelEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketTunnelEndpoint.class);

    public CustomWebSocketTunnelEndpoint() {
        logger.info("CustomWebSocketTunnelEndpoint initialized");
    }

    @Override
    public GuacamoleTunnel createTunnel(Session session, EndpointConfig config) throws GuacamoleException {
        // Получаем query string из URI сессии WebSocket
        String query = session.getRequestURI().getQuery(); // например: "protocol=rdp&hostname=192.168.0.100"

        // Значения по умолчанию
        String protocol = "rdp";
        String hostname = "192.168.0.100";
        String port = "3389";

        if (query != null) {
            // Разбираем параметры query string
            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

            protocol = params.getOrDefault("protocol", protocol);
            hostname = params.getOrDefault("hostname", hostname);
            port = params.getOrDefault("port", port);
        }

        SimpleGuacamoleTunnel baseTunnel = getSimpleGuacamoleTunnel(protocol, hostname, port);
        logger.info("Created tunnel for session {}", session.getId());

        return new InterceptingGuacamoleTunnel(baseTunnel);
    }

    private static SimpleGuacamoleTunnel getSimpleGuacamoleTunnel(String protocol, String hostname, String port) throws GuacamoleException {
        GuacamoleConfiguration guacConfig = new GuacamoleConfiguration();
        guacConfig.setProtocol(protocol); // или "vnc", "ssh" — можно выбирать динамически
        guacConfig.setParameter("hostname", hostname);
        guacConfig.setParameter("port", port);

        ConfiguredGuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("127.0.0.1", 4822),
                guacConfig
        );

        SimpleGuacamoleTunnel baseTunnel = new SimpleGuacamoleTunnel(socket);
        return baseTunnel;
    }
}
