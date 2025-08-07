package club.ss220.manager.service;

import club.ss220.core.model.Member;
import club.ss220.core.service.MemberService;
import club.ss220.manager.model.MemberTarget;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MemberTargetService {

    private final MemberService memberService;

    public Optional<Member> resolve(MemberTarget target) {
        log.debug("Resolving member target: {}", target);
        if (target.discordUser() != null) {
            return resolveByDiscordId(target.discordUser().getIdLong());
        }

        String query = target.query();
        if (query == null) {
            throw new IllegalArgumentException("Member target must have a discord user or a query");
        }

        if (query.startsWith("<@") && query.endsWith(">")) {
            query = query.substring(2, query.length() - 1);
        }

        try {
            long userId = MiscUtil.parseSnowflake(query);
            return resolveByDiscordId(userId);
        } catch (NumberFormatException e) {
            return resolveByCkey(query);
        }
    }

    private Optional<Member> resolveByDiscordId(Long userId) {
        Optional<Member> member = memberService.getMemberByDiscordId(userId);
        if (member.isPresent()) {
            log.debug("Successfully resolved discord {} to member {}", userId, member.get().getId());
        } else {
            log.debug("No member found for discord: {}", userId);
        }

        return member;
    }

    private Optional<Member> resolveByCkey(String ckey) {
        Optional<Member> member = memberService.getMemberByCkey(ckey);
        if (member.isPresent()) {
            log.debug("Successfully resolved ckey '{}' to member {}", ckey, member.get().getId());
        } else {
            log.debug("No member found for ckey: '{}'", ckey);
        }

        return member;
    }
}
