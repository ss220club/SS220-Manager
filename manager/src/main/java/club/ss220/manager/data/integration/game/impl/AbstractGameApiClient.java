package club.ss220.manager.data.integration.game.impl;

import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.data.integration.game.exception.GameApiException;
import club.ss220.manager.model.GameServer;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    protected final ObjectMapper objectMapper = new ObjectMapper();

    private static final int TIMEOUT_MS = 5000;
    private static final int HEADER_BYTES = 4;
    private static final int HOLDER_BYTES = 6;
    private static final int MAX_MESSAGE_LENGTH = Short.MAX_VALUE;
    private static final String DATA_PROPERTY = "data";

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

    protected <T> T executeCommand(GameServer gameServer, String command, TypeReference<T> typeRef) {
        String fullCommand = buildCommand(gameServer, command);
        log.debug("Executing topic '{}' on {}:{}", fullCommand, gameServer.getHost(), gameServer.getPort());

        try {
            byte[] responseBytes = sendReceiveData(gameServer, fullCommand);
            Object raw = decodeByondResponse(responseBytes).get(DATA_PROPERTY);
            return objectMapper.convertValue(raw, typeRef);

        } catch (JsonProcessingException e) {
            String message = "Error decoding response for command '" + fullCommand + "'";
            throw new GameApiException(gameServer, message, e);
        } catch (IOException e) {
            String message = "Error executing command '" + fullCommand + "'";
            throw new GameApiException(gameServer, message, e);
        } catch (IllegalArgumentException e) {
            String message = "Error converting response to " + typeRef + ", command '" + fullCommand + "'";
            throw new GameApiException(gameServer, message, e);
        }
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

            byte[] buffer = new byte[MAX_MESSAGE_LENGTH];
            int bytesRead = socket.getInputStream().read(buffer);

            if (bytesRead <= 0) {
                return new byte[0];
            }

            return Arrays.copyOf(buffer, bytesRead);

        } catch (SocketTimeoutException e) {
            throw new GameApiException(server, "Server " + server.getFullName() + " is unavailable", e);
        }
    }

    protected byte[] preparePacket(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        int messageLength = dataBytes.length + HOLDER_BYTES;
        if (messageLength > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message '" + data + "' exceeds maximum length:" + MAX_MESSAGE_LENGTH);
        }

        ByteBuffer buffer = ByteBuffer.allocate(dataBytes.length + HEADER_BYTES + HOLDER_BYTES);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x83);
        buffer.putShort((short) (dataBytes.length + HOLDER_BYTES));
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        buffer.put(dataBytes);
        buffer.put((byte) 0x00);
        return buffer.array();
    }

    protected Map<String, Object> decodeByondResponse(byte[] data) throws JsonProcessingException {
        if (data.length <= HOLDER_BYTES) {
            return Map.of();
        }

        byte[] jsonBytes = Arrays.copyOfRange(data, HOLDER_BYTES - 1, data.length - 1);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        if (jsonString.isEmpty()) {
            return Map.of();
        }

        JsonNode node = objectMapper.readTree(jsonString);
        if (node.isArray()) {
            return Map.of(DATA_PROPERTY, objectMapper.convertValue(node, List.class));
        } else if (node.isObject()) {
            return Map.of(DATA_PROPERTY, objectMapper.convertValue(node, Map.class));
        }

        return Map.of(DATA_PROPERTY, new Object());
    }
}
