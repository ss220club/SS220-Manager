package club.ss220.manager.app.controller;

import club.ss220.manager.app.view.MemberInfoView;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.Member;
import club.ss220.manager.model.MemberTarget;
import club.ss220.manager.service.MemberTargetService;
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
    private final MemberTargetService targetService;

    public void showMemberInfo(InteractionHook hook, User viewer) {
        MemberTarget target = MemberTarget.fromUser(viewer);
        showMemberInfo(hook, viewer, target, false);
    }

    public void showMemberInfo(InteractionHook hook, User viewer, User target) {
        MemberTarget memberTarget = MemberTarget.fromUser(target);
        showMemberInfo(hook, viewer, memberTarget, true);
    }

    public void showMemberInfo(InteractionHook hook, User viewer, MemberTarget target) {
        showMemberInfo(hook, viewer, target, true);
    }

    private void showMemberInfo(InteractionHook hook, User viewer, MemberTarget target, boolean isConfidential) {
        try {
            Optional<Member> memberOptional = targetService.resolve(target);
            if (memberOptional.isEmpty()) {
                view.renderMemberNotFound(hook, target);
                log.debug("Member not found for target {}", target);
                return;
            }

            Member member = memberOptional.get();
            GameBuild defaultBuild = member.getGameInfo().firstKey();
            MemberInfoContext context = isConfidential
                                        ? MemberInfoContext.confidentialInfo(member, defaultBuild)
                                        : MemberInfoContext.publicInfo(member, defaultBuild);

            view.renderMemberInfo(hook, viewer, context);

            log.debug("Displayed {} member info for target {}", isConfidential ? "confidential" : "public", target);
        } catch (Exception e) {
            log.error("Error displaying member info for target {}", target, e);
            throw new RuntimeException(e);
        }
    }

    public void handleBuildSelection(StringSelectEvent selectEvent, MemberInfoContext context, String selectedValue) {
        try {
            selectEvent.deferEdit().queue();

            GameBuild selectedBuild = GameBuild.valueOf(selectedValue);
            MemberInfoContext newContext = context.withBuild(selectedBuild);
            view.updateMemberInfo(selectEvent.getHook(), selectEvent.getUser(), newContext);

            log.debug("Displayed updated info with build selection: {}", selectedBuild.getName());
        } catch (Exception e) {
            log.error("Error handling build selection for build {}", selectedValue, e);
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
    }
}
