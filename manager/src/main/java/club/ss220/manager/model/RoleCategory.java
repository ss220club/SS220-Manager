package club.ss220.manager.model;

import club.ss220.manager.model.exception.UnknownRoleCategoryException;
import lombok.Getter;

@Getter
public enum RoleCategory {
    ADMIN(0, "Админ"),
    GHOST(0, "Призрак"),
    LIVING(0, "Живой"),
    SPECIAL(1, "Специальные"),
    IGNORE(0, "[NT REDACTED]"),
    ANTAGONIST(1, "Антагонист"),
    CREW(1, "Экипаж"),
    COMMAND(2, "Командование"),
    NT_REPRESENTATION(2, "Представительство НТ"),
    SECURITY(2, "Безопасность"),
    ENGINEERING(2, "Инженерия"),
    SCIENCE(2, "Исследование"),
    MEDICAL(2, "Медицина"),
    SUPPLY(2, "Снабжение"),
    SERVICE(2, "Сервис"),
    JUSTICE(2, "Правосудие"),
    MISC(2, "Разное"),
    SILICON(2, "Роботы");

    private final int level;
    private final String formattedName;

    RoleCategory(int level, String formattedName) {
        this.level = level;
        this.formattedName = formattedName;
    }

    public static RoleCategory fromValue(String value) {
        try {
            return RoleCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownRoleCategoryException(value);
        }
    }
}
