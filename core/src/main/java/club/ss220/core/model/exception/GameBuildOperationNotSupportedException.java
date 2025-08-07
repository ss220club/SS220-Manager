package club.ss220.core.model.exception;

import club.ss220.core.model.GameBuild;

public class GameBuildOperationNotSupportedException extends RuntimeException {

    private final GameBuild gameBuild;

    public GameBuildOperationNotSupportedException(GameBuild gameBuild, String message) {
        super(message);
        this.gameBuild = gameBuild;
    }

    public GameBuildOperationNotSupportedException(GameBuild gameBuild, String message, Throwable cause) {
        super(message, cause);
        this.gameBuild = gameBuild;
    }
}
