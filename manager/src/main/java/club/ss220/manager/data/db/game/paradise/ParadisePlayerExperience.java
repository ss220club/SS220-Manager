package club.ss220.manager.data.db.game.paradise;

import club.ss220.manager.model.PlayerExperience;
import club.ss220.manager.model.RoleCategory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ParadisePlayerExperience extends PlayerExperience {

    public ParadisePlayerExperience(Map<RoleCategory, Duration> exp) {
        super(exp);
    }

    @Override
    public List<RoleCategory> getRelevantRoles() {
        return List.of(
                RoleCategory.GHOST,
                RoleCategory.LIVING,
                RoleCategory.SPECIAL,
                RoleCategory.CREW,
                RoleCategory.COMMAND,
                RoleCategory.SECURITY,
                RoleCategory.ENGINEERING,
                RoleCategory.SCIENCE,
                RoleCategory.MEDICAL,
                RoleCategory.SUPPLY,
                RoleCategory.SERVICE,
                RoleCategory.MISC,
                RoleCategory.SILICON
        );
    }
}
