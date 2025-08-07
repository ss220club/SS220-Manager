package club.ss220.manager.model.exception;

public class UnknownGameBuildException extends RuntimeException {

    private final String serverName;

    public UnknownGameBuildException(String serverName) {
        super("Unknown game build: " + serverName);
        this.serverName = serverName;
    }
}
