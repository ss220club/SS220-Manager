package club.ss220.manager.data.mapper;

import club.ss220.manager.data.api.PlayerDto;
import club.ss220.manager.data.db.paradise.ParadiseBan;
import club.ss220.manager.data.db.paradise.ParadiseCharacter;
import club.ss220.manager.data.db.paradise.ParadisePlayer;
import club.ss220.manager.data.integration.game.AdminStatusDto;
import club.ss220.manager.data.integration.game.PlayerStatusDto;
import club.ss220.manager.data.integration.game.ServerStatusDto;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdmin;
import club.ss220.manager.model.OnlinePlayer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class Mappers {

    private Mappers() {
    }

    public club.ss220.manager.model.Player toPlayer(PlayerDto centralPlayer, ParadisePlayer paradisePlayer) {
        return club.ss220.manager.model.Player.builder()
                .id(centralPlayer.getId())
                .discordId(centralPlayer.getDiscordId())
                .ckey(centralPlayer.getCkey())
                .firstSeen(paradisePlayer.getFirstSeen())
                .lastSeen(paradisePlayer.getLastSeen())
                .ip(paradisePlayer.getIp())
                .computerId(paradisePlayer.getComputerId())
                .lastAdminRank(paradisePlayer.getLastAdminRank())
                .exp(paradisePlayer.getExp())
                .speciesWhitelist(paradisePlayer.getSpeciesWhitelist())
                .build();
    }

    public club.ss220.manager.model.GameCharacter toGameCharacter(ParadiseCharacter paradiseCharacter) {
        return club.ss220.manager.model.GameCharacter.builder()
                .ckey(paradiseCharacter.getCkey())
                .slot(paradiseCharacter.getSlot())
                .realName(paradiseCharacter.getRealName())
                .gender(paradiseCharacter.getGender())
                .age(paradiseCharacter.getAge())
                .species(paradiseCharacter.getSpecies())
                .build();
    }

    public GameServerStatus toGameServerStatus(ServerStatusDto serverStatusDto) {
        return GameServerStatus.builder()
                .version(serverStatusDto.getVersion())
                .playersCount(serverStatusDto.getPlayersCount())
                .stationTime(serverStatusDto.getStationTime())
                .roundDuration(serverStatusDto.getRoundDuration())
                .map(serverStatusDto.getMap())
                .adminsCount(serverStatusDto.getAdminsCount())
                .roundId(serverStatusDto.getRoundId())
                .mode(serverStatusDto.getMode())
                .build();
    }

    public OnlinePlayer toOnlinePlayer(PlayerStatusDto playerStatusDto) {
        return OnlinePlayer.builder()
                .ckey(playerStatusDto.getCkey())
                .characterName(playerStatusDto.getCharacterName())
                .job(playerStatusDto.getJob())
                .build();
    }

    public OnlineAdmin toOnlineAdmin(AdminStatusDto adminStatusDto) {
        return OnlineAdmin.builder()
                .ckey(adminStatusDto.getCkey())
                .key(adminStatusDto.getKey())
                .rank(adminStatusDto.getRank())
                .afkDuration(Duration.ofMillis(100L * adminStatusDto.getAfkDuration()))
                .stealthMode(OnlineAdmin.StealthMode.fromValue(adminStatusDto.getStealthMode()))
                .stealthKey(adminStatusDto.getStealthKey())
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
}
