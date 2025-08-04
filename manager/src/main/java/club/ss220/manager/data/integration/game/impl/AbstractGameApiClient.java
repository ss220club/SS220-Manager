package club.ss220.manager.data.integration.game.impl;

import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameServer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractGameApiClient implements GameApiClient {

    public static final int TIMEOUT_MS = 5000;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<List<String>> getPlayersList(GameServer gameServer) {
        return callServer(gameServer, "playerlist", new TypeReference<>() {});
    }

    @Override
    public Mono<Boolean> sendHostAnnounce(GameServer gameServer, String message) {
        return callServer(gameServer, "announce=" + message, new TypeReference<>() {});
    }

    @Override
    public Mono<Boolean> sendAdminMessage(GameServer gameServer, String ckey, String message, String adminName) {
        // TODO: Implement admin message sending
        throw new UnsupportedOperationException("Admin message sending is not implemented yet");
    }

    protected <T> Mono<T> callServer(GameServer gameServer, String command, TypeReference<T> typeRef) {
        return Mono.fromCallable(() -> executeCommand(gameServer, command, typeRef));
    }

    protected <T> T executeCommand(GameServer gameServer, String command, TypeReference<T> typeRef) throws IOException {
        String fullCommand = buildCommand(gameServer, command);
        log.debug("Executing topic '{}' on {}:{}", fullCommand, gameServer.getHost(), gameServer.getPort());

        byte[] responseBytes = sendReceiveData(gameServer, fullCommand);
        Object raw = decodeByondResponse(responseBytes).get("data");
        return objectMapper.convertValue(raw, typeRef);
    }

    protected String buildCommand(GameServer server, String command) {
        StringBuilder sb = new StringBuilder(command);
        if (server.getKey() != null && !server.getKey().isEmpty()) {
            sb.append("&key=").append(server.getKey());
        }
        sb.append("&format=json");
        return sb.toString();
    }

    protected byte[] sendReceiveData(GameServer server, String command) throws IOException {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), TIMEOUT_MS);

            byte[] packet = preparePacket(command);
            socket.getOutputStream().write(packet);
            socket.getOutputStream().flush();

            byte[] buffer = new byte[16384];
            int bytesRead = socket.getInputStream().read(buffer);

            if (bytesRead <= 0) {
                return new byte[0];
            }

            return Arrays.copyOf(buffer, bytesRead);

        } catch (SocketTimeoutException e) {
            log.error("Timeout connecting to server {}:{}", server.getHost(), server.getPort());
            throw new IOException("Connection timeout", e);
        }
    }

    protected byte[] preparePacket(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(dataBytes.length + 10);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x83);
        buffer.putShort((short) (dataBytes.length + 6));
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        buffer.put(dataBytes);
        buffer.put((byte) 0x00);
        return buffer.array();
    }

    protected Map<String, Object> decodeByondResponse(byte[] data) throws IOException {
        if (data.length <= 6) {
            return Map.of();
        }

        byte[] jsonBytes = Arrays.copyOfRange(data, 5, data.length - 1);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        if (jsonString.isEmpty()) {
            return Map.of();
        }

        JsonNode node = objectMapper.readTree(jsonString);
        if (node.isArray()) {
            return Map.of("data", objectMapper.convertValue(node, List.class));
        } else if (node.isObject()) {
            return Map.of("data", objectMapper.convertValue(node, Map.class));
        }

        return Map.of("data", new Object());
    }
}
