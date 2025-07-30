package club.ss220.manager.data.integration.game.impl;

import club.ss220.manager.config.GameConfig;
import club.ss220.manager.data.integration.game.AdminStatusDto;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.data.integration.game.PlayerStatusDto;
import club.ss220.manager.data.integration.game.ServerStatusDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class GameApiClientImpl implements GameApiClient {

    private static final int TIMEOUT_MS = 5000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameConfig gameConfig;

    public GameApiClientImpl(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    @Override
    public Mono<ServerStatusDto> getServerStatus(String serverName) {
        return callServer(serverName, "status", new TypeReference<>() {});
    }

    @Override
    public Mono<Map<GameServer, ServerStatusDto>> getAllServersStatus() {
        return Flux.fromIterable(gameConfig.getServers())
                .flatMap(server -> getServerStatus(server.getName())
                        .map(status -> Map.entry(server, status))
                        .onErrorResume(e -> {
                            log.error("Failed to get status from server {}", server.getName(), e);
                            return Mono.just(Map.entry(server, ServerStatusDto.unknown()));
                        })
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }


    @Override
    public Mono<List<PlayerStatusDto>> getPlayersList(String serverName) {
        return callServer(serverName, "playerlist", new TypeReference<>() {});
    }


    @Override
    public Mono<Map<GameServer, List<AdminStatusDto>>> getAllAdminsList() {
        Function<GameServer, Mono<Map.Entry<GameServer, List<AdminStatusDto>>>> getServerAdmins = server ->
                callServer(server.getName(), "adminwho", new TypeReference<List<AdminStatusDto>>() {})
                        .onErrorResume(e -> {
                            log.error("Failed to get admins list from server {}", server.getName(), e);
                            return Mono.just(List.of());
                        })
                        .map(admins -> Map.entry(server, admins));

        return Flux.fromIterable(gameConfig.getServers())
                .flatMap(getServerAdmins)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public Mono<Boolean> sendHostAnnounce(String serverName, String message) {
        return callServer(serverName, "announce=" + message, new TypeReference<>() {});
    }

    @Override
    public Mono<Boolean> sendAdminMessage(String serverName, String ckey, String message, String adminName) {
        // TODO: Implement admin message sending
        throw new UnsupportedOperationException("Admin message sending is not implemented yet");
    }

    private <T> Mono<T> callServer(String serverName, String command, TypeReference<T> typeRef) {
        return Mono.defer(() -> Mono.justOrEmpty(gameConfig.getServerByName(serverName))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No server found with name: " + serverName)))
                .flatMap(server -> Mono.fromCallable(() -> executeCommand(server, command, typeRef)))
        );
    }

    private <T> T executeCommand(GameServer server, String command, TypeReference<T> typeRef) throws IOException {
        String fullCommand = buildCommand(server, command);
        log.debug("Executing command '{}' on {}:{}", fullCommand, server.getIp(), server.getPort());

        byte[] responseBytes = sendReceiveData(server, fullCommand);
        Object raw = decodeByondResponse(responseBytes).get("data");
        return objectMapper.convertValue(raw, typeRef);
    }

    private String buildCommand(GameServer server, String command) {
        StringBuilder sb = new StringBuilder(command);
        if (server.getKey() != null && !server.getKey().isEmpty()) {
            sb.append("&key=").append(server.getKey());
        }
        sb.append("&format=json");
        return sb.toString();
    }

    private byte[] sendReceiveData(GameServer server, String command) throws IOException {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            socket.connect(new InetSocketAddress(server.getIp(), server.getPort()), TIMEOUT_MS);

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
            log.error("Timeout connecting to server {}:{}", server.getIp(), server.getPort());
            throw new IOException("Connection timeout", e);
        }
    }

    private byte[] preparePacket(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(dataBytes.length + 7);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x83);
        buffer.putShort((short) (dataBytes.length + 6));
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00});
        buffer.put(dataBytes);
        buffer.put((byte) 0x00);
        return buffer.array();
    }

    private Map<String, Object> decodeByondResponse(byte[] data) throws IOException {
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
