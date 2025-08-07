package club.ss220.manager.model.exception;

public class UnknownCharacterSpeciesException extends RuntimeException {

    private final String species;

    public UnknownCharacterSpeciesException(String species) {
        super("Unknown species: " + species);
        this.species = species;
    }
}
