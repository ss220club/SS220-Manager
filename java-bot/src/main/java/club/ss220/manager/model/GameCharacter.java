package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Data
@Builder
public class GameCharacter {
    private final Integer id;
    private final String ckey;
    private final Integer slot;
    private final String realName;
    private final String gender;
    private final Short age;
    private final Species species;

    @Getter
    public enum Species {
        HUMAN("Human"),
        DIONA("Diona"),
        DRASK("Drask"),
        GREY("Grey"),
        KIDAN("Kidan"),
        MACHINE("Machine"),
        NIAN("Nian"),
        PLASMAMAN("Plasmaman"),
        SKRELL("Skrell"),
        SLIME_PEOPLE("Slime People"),
        TAJARAN("Tajaran"),
        UNATHI("Unathi"),
        VOX("Vox"),
        VULPKANIN("Vulpkanin"),
        NUCLEATION("Nucleation");

        private final String name;

        Species(String name) {
            this.name = name;
        }

        public static Species fromName(String name) {
            return Arrays.stream(Species.values())
                    .filter(species -> species.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Unknown species: " + name));
        }
    }
}
