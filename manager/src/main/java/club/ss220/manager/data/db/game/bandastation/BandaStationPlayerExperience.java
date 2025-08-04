package club.ss220.manager.data.db.game.bandastation;

import club.ss220.manager.model.PlayerExperience;
import club.ss220.manager.model.RoleCategory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class BandaStationPlayerExperience extends PlayerExperience {

    public BandaStationPlayerExperience(Map<RoleCategory, Duration> exp) {
        super(exp);
    }

    @Override
    public List<RoleCategory> getRoles() {
        return List.of(
                RoleCategory.ADMIN,
                RoleCategory.GHOST,
                RoleCategory.LIVING,
                RoleCategory.SPECIAL,
                RoleCategory.ANTAGONIST,
                RoleCategory.CREW,
                RoleCategory.COMMAND,
                RoleCategory.NT_REPRESENTATION,
                RoleCategory.SECURITY,
                RoleCategory.ENGINEERING,
                RoleCategory.SCIENCE,
                RoleCategory.MEDICAL,
                RoleCategory.SUPPLY,
                RoleCategory.SERVICE,
                RoleCategory.JUSTICE,
                RoleCategory.MISC,
                RoleCategory.SILICON
        );
    }
}
