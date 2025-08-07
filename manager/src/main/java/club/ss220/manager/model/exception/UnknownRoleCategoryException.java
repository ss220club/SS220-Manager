package club.ss220.manager.model.exception;

public class UnknownRoleCategoryException extends RuntimeException {

    private final String roleCategory;

    public UnknownRoleCategoryException(String roleCategory) {
        super("Unknown role category: " + roleCategory);
        this.roleCategory = roleCategory;
    }
}
