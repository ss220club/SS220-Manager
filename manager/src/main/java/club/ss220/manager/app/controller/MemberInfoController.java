package club.ss220.manager.app.controller;

import club.ss220.manager.app.view.MemberInfoView;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.Member;
import club.ss220.manager.service.UserService;
import io.github.freya022.botcommands.api.components.event.StringSelectEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class MemberInfoController {

    private final MemberInfoView view;
    private final UserService memberService;

    public void showMemberInfo(InteractionHook hook, User user) {
        try {
            Optional<Member> memberOptional = memberService.getMemberByDiscordId(user.getIdLong());
            if (memberOptional.isEmpty()) {
                view.renderUserNotFound(hook, user);
                log.debug("User {} ({}) not found", user.getAsTag(), user.getIdLong());
                return;
            }

            Member member = memberOptional.get();
            GameBuild defaultBuild = member.getGameInfo().firstKey();
            MemberInfoContext context = MemberInfoContext.publicInfo(member, defaultBuild);
            view.renderMemberInfo(hook, user, context);

            log.debug("Displayed member info for user {} ({})", user.getAsTag(), user.getIdLong());
        } catch (Exception e) {
            log.error("Error rendering member info for user {} ({})", user.getAsTag(), user.getIdLong(), e);
            throw new RuntimeException(e);
        }
    }

    public void handleBuildSelection(StringSelectEvent selectEvent, MemberInfoContext context, String selectedValue) {
        User user = selectEvent.getUser();
        try {
            selectEvent.deferEdit().queue();

            GameBuild selectedBuild = GameBuild.valueOf(selectedValue);
            MemberInfoContext newContext = context.withBuild(selectedBuild);
            view.updateUserInfo(selectEvent.getHook(), user, newContext);
            log.debug("Displayed updated member info for user {} ({})", user.getAsTag(), user.getIdLong());
        } catch (Exception e) {
            log.error("Error handling build selection for user {} ({})", user.getAsTag(), user.getIdLong(), e);
            throw new RuntimeException(e);
        }
    }

    @Value
    @Builder(toBuilder = true)
    public static class MemberInfoContext {

        Member member;
        GameBuild selectedBuild;
        boolean confidential;

        public static MemberInfoContext publicInfo(Member member, GameBuild selectedBuild) {
            return MemberInfoContext.builder()
                    .member(member)
                    .selectedBuild(selectedBuild)
                    .confidential(false)
                    .build();
        }

        public static MemberInfoContext confidentialInfo(Member member, GameBuild selectedBuild) {
            return MemberInfoContext.builder()
                    .member(member)
                    .selectedBuild(selectedBuild)
                    .confidential(true)
                    .build();
        }

        public MemberInfoContext withBuild(GameBuild newBuild) {
            return toBuilder()
                    .selectedBuild(newBuild)
                    .build();
        }

        public MemberInfoContext withConfidentiality(boolean isConfidential) {
            return toBuilder()
                    .confidential(isConfidential)
                    .build();
        }
    }
}
