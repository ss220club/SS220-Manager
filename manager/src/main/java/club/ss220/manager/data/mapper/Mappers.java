package club.ss220.manager.data.mapper;

import club.ss220.manager.config.BandaStationConfig;
import club.ss220.manager.data.api.UserDto;
import club.ss220.manager.data.db.game.bandastation.BandaStationPlayer;
import club.ss220.manager.data.db.game.paradise.ParadiseBan;
import club.ss220.manager.data.db.game.paradise.ParadiseCharacter;
import club.ss220.manager.data.db.game.paradise.ParadisePlayer;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.model.Player;
import club.ss220.manager.model.RoleCategory;
import club.ss220.manager.model.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class Mappers {

    private final RoleCategoryMapping roleCategoryMapping;

    public Mappers(RoleCategoryMapping roleCategoryMapping) {
        this.roleCategoryMapping = roleCategoryMapping;
    }

    public User toUser(UserDto centralPlayer, List<Player> players) {
        return User.builder()
                .id(centralPlayer.getId())
                .discordId(centralPlayer.getDiscordId())
                .ckey(centralPlayer.getCkey())
                .gameInfo(getGameInfo(players))
                .build();
    }

    private static TreeMap<GameBuild, Player> getGameInfo(List<Player> players) {
        TreeMap<GameBuild, Player> gameInfo = new TreeMap<>();
        for (Player player : players) {
            Player previousInfo = gameInfo.put(player.getGameBuild(), player);
            if (previousInfo != null) {
                String message = "Duplicate player info for build: " + player.getGameBuild();
                throw new IllegalArgumentException(message);
            }
        }
        return gameInfo;
    }

    public Player toPlayer(ParadisePlayer player) {
        return Player.builder()
                .gameBuild(GameBuild.PARADISE)
                .ckey(player.getCkey())
                .byondJoinDate(player.getByondJoinDate())
                .firstSeenDateTime(player.getFirstSeen())
                .lastSeenDateTime(player.getLastSeen())
                .ip(toInetAddress(player.getIp()))
                .computerId(player.getComputerId())
                .lastAdminRank(player.getLastAdminRank())
                .exp(roleCategoryMapping.getParadiseExp(player.getExp()))
                .characters(player.getCharacters().stream().map(this::toGameCharacter).toList())
                .build();
    }

    public Player toPlayer(BandaStationPlayer player) {
        return Player.builder()
                .gameBuild(GameBuild.BANDASTATION)
                .ckey(player.getCkey())
                .byondJoinDate(player.getByondJoinDate())
                .firstSeenDateTime(player.getFirstSeen())
                .lastSeenDateTime(player.getLastSeen())
                .ip(toInetAddress((int) player.getIp()))
                .computerId(player.getComputerId())
                .lastAdminRank(player.getLastAdminRank())
                .exp(roleCategoryMapping.getBandastationExp(player.getRoleTime()))
                .characters(null) // TODO: Update this when bandastation will store game characters in a database.
                .build();
    }

    public club.ss220.manager.model.GameCharacter toGameCharacter(ParadiseCharacter paradiseCharacter) {
        return club.ss220.manager.model.GameCharacter.builder()
                .ckey(paradiseCharacter.getCkey())
                .slot(paradiseCharacter.getSlot())
                .realName(paradiseCharacter.getRealName())
                .gender(GameCharacter.Gender.fromValue(paradiseCharacter.getGender()))
                .age(paradiseCharacter.getAge())
                .species(paradiseCharacter.getSpecies())
                .build();
    }

    public club.ss220.manager.model.Ban toBan(ParadiseBan ban) {
        return club.ss220.manager.model.Ban.builder()
                .id(ban.getId())
                .ckey(ban.getCkey())
                .adminCkey(ban.getAdminCkey())
                .reason(ban.getReason())
                .banTime(ban.getBanDatetime())
                .unbanTime(ban.getUnbanDatetime())
                .banType(ban.getBanType())
                .isActive(ban.isActive())
                .build();
    }

    private InetAddress toInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP: " + ip, e);
        }
    }

    public static InetAddress toInetAddress(int value) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP long value: " + Integer.toHexString(value), e);
        }
    }

    @Service
    public static class RoleCategoryMapping {

        private final Map<String, RoleCategory> roleCategoryMap;

        public RoleCategoryMapping(BandaStationConfig bandaStationConfig) {
            roleCategoryMap = bandaStationConfig.getRoles().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(role -> Map.entry(role, entry.getKey())))
                    .map(e -> Map.entry(e.getKey(), RoleCategory.valueOf(e.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public TreeMap<RoleCategory, Duration> getBandastationExp(Map<String, Long> roleTime) {
            return roleTime.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> getRoleCategory(entry.getKey()),
                            entry -> Duration.ofMinutes(entry.getValue()),
                            Duration::plus,
                            TreeMap::new
                    ));
        }

        public TreeMap<RoleCategory, Duration> getParadiseExp(String exp) {
            TreeMap<RoleCategory, Duration> result = new TreeMap<>(new EnumMap<>(RoleCategory.class));
            Arrays.stream(exp.split("&"))
                    .map(v -> v.split("="))
                    .forEach(v -> result.put(RoleCategory.fromValue(v[0]), Duration.ofMinutes(Integer.parseInt(v[1]))));
            return result;
        }

        public RoleCategory getRoleCategory(String role) {
            return roleCategoryMap.getOrDefault(role, RoleCategory.SPECIAL);
        }
    }
}
