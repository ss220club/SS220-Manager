package club.ss220.manager.config;

import club.ss220.manager.db.paradise.entity.GameCharacter;
import club.ss220.manager.db.paradise.entity.Player;
import club.ss220.manager.db.paradise.repository.GameCharacterRepository;
import club.ss220.manager.db.paradise.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("test")
public class TestDatabaseConfig {

    private final PlayerRepository playerRepository;
    private final GameCharacterRepository characterRepository;

    @Bean
    @Order(1)
    public CommandLineRunner initTestDatabaseRunner() {
        return args -> initTestDatabase();
    }

    public void initTestDatabase() {
        try {
            log.info("Initializing test in-memory database with JPA entities");
            
            Player player1 = new Player();
            player1.setCkey("testckey1");
            player1.setFirstSeen(LocalDateTime.now().minusDays(30));
            player1.setLastSeen(LocalDateTime.now().minusHours(5));
            player1.setLastAdminRank("Player");
            
            Player player2 = new Player();
            player2.setCkey("testckey2");
            player2.setFirstSeen(LocalDateTime.now().minusDays(60));
            player2.setLastSeen(LocalDateTime.now().minusDays(2));
            player2.setLastAdminRank("Admin");
            
            List<Player> savedPlayers = playerRepository.saveAll(List.of(player1, player2));
            log.info("Created {} test players", savedPlayers.size());
            
            GameCharacter character1 = new GameCharacter();
            character1.setCkey("testckey1");
            character1.setSlot(1);
            character1.setRealName("John Doe");
            character1.setGender("male");
            character1.setAge(30);
            character1.setSpecies("human");
            
            GameCharacter character2 = new GameCharacter();
            character2.setCkey("testckey1");
            character2.setSlot(2);
            character2.setRealName("Jane Doe");
            character2.setGender("female");
            character2.setAge(25);
            character2.setSpecies("tajaran");
            
            GameCharacter character3 = new GameCharacter();
            character3.setCkey("testckey2");
            character3.setSlot(1);
            character3.setRealName("Admin Character");
            character3.setGender("male");
            character3.setAge(40);
            character3.setSpecies("human");
            
            List<GameCharacter> savedCharacters = characterRepository.saveAll(List.of(character1, character2, character3));
            log.info("Created {} test characters", savedCharacters.size());
            
            log.info("Test database initialized with sample data");
        } catch (Exception e) {
            log.error("Error initializing test database", e);
        }
    }
}
