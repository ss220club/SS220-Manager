package club.ss220.manager.app.view;

import club.ss220.manager.app.controller.MemberInfoController;
import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Formatters;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.model.Member;
import club.ss220.manager.model.Player;
import club.ss220.manager.model.RoleCategory;
import com.ibm.icu.text.MessageFormat;
import dev.freya02.jda.emojis.unicode.Emojis;
import io.github.freya022.botcommands.api.components.SelectMenus;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static club.ss220.manager.app.controller.MemberInfoController.MemberInfoContext;

@Component
@RequiredArgsConstructor
public class MemberInfoView {

    private MemberInfoController controller;
    private final SelectMenus selectMenus;
    private final Senders senders;
    private final Embeds embeds;
    private final Formatters formatters;

    @Lazy
    @Autowired
    public void setController(MemberInfoController controller) {
        this.controller = controller;
    }

    public void renderMemberInfo(InteractionHook hook, User user, MemberInfoContext context) {
        MessageCreateData message = buildMessageData(user, context);
        hook.setEphemeral(true).sendMessage(message).queue();
    }

    public void updateUserInfo(InteractionHook hook, User user, MemberInfoContext context) {
        MessageEditData message = MessageEditData.fromCreateData(buildMessageData(user, context));
        hook.editOriginal(message).queue();
    }

    public void renderUserNotFound(InteractionHook hook, User user) {
        senders.sendEmbedEphemeral(hook, embeds.error("Пользователь " + user.getAsMention() + " не найден."));
    }

    private MessageCreateData buildMessageData(User user, MemberInfoContext context) {
        MessageEmbed embed = createUserInfoEmbed(context);
        ActionRow buildSelectMenu = createBuildSelectMenu(user, context);

        return new MessageCreateBuilder().setEmbeds(embed).setComponents(buildSelectMenu).build();
    }

    private ActionRow createBuildSelectMenu(User discordUser, MemberInfoContext context) {
        Set<GameBuild> availableBuilds = context.getMember().getGameInfo().keySet();
        GameBuild selectedBuild = context.getSelectedBuild();

        List<SelectOption> options = availableBuilds.stream()
                .map(build -> {
                    GameBuildStyle style = GameBuildStyle.fromName(build.getName());
                    return SelectOption.of(style.getName(), build.name()).withEmoji(style.getEmoji());
                })
                .toList();

        StringSelectMenu selectMenu = selectMenus.stringSelectMenu()
                .ephemeral()
                .constraints(constraints -> constraints.addUsers(discordUser))
                .bindTo(selectEvent -> {
                    String selectedValue = selectEvent.getValues().getFirst();
                    controller.handleBuildSelection(selectEvent, context, selectedValue);
                })
                .setPlaceholder("Игровой билд")
                .addOptions(options)
                .setDefaultValues(selectedBuild.name())
                .build();

        return ActionRow.of(selectMenu);
    }

    private MessageEmbed createUserInfoEmbed(MemberInfoContext context) {
        Member member = context.getMember();
        GameBuild selectedBuild = context.getSelectedBuild();
        boolean isConfidential = context.isConfidential();

        Player player = member.getGameInfo().get(selectedBuild);
        String description = "**Discord:** " + User.fromId(member.getDiscordId()).getAsMention() + "\n"
                             + "**CKEY:** " + member.getCkey() + "\n\n"
                             + createPlayerInfoBlock(player, isConfidential);

        List<MessageEmbed.Field> fields = List.of(
                playerExpField(player),
                playerCharactersField(player.getCharacters())
        );

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о пользователе " + member.getId());
        embed.setDescription(description);
        embed.getFields().addAll(fields);
        if (isConfidential) {
            embed.setFooter("Осторожно, сообщение содержит конфиденциальные данные.");
        }
        embed.setColor(UiConstants.COLOR_INFO);
        return embed.build();
    }

    public MessageEmbed getView(Member member, GameBuild selectedBuild, boolean isConfidential) {
        MemberInfoContext context = MemberInfoContext.builder()
                .member(member)
                .selectedBuild(selectedBuild)
                .confidential(isConfidential)
                .build();
        return createUserInfoEmbed(context);
    }

    private String createPlayerInfoBlock(Player player, boolean isConfidential) {
        String info = "**Ранг:** " + player.getLastAdminRank() + "\n"
                      + "**Стаж:** " + player.getKnownFor().toDays() + " дн.\n"
                      + "**BYOND создан:** " + formatters.formatDate(player.getByondJoinDate()) + "\n"
                      + "**Первый вход:** " + formatters.formatDateTime(player.getFirstSeenDateTime()) + "\n"
                      + "**Последний вход:** " + formatters.formatDateTime(player.getLastSeenDateTime()) + "\n";
        if (isConfidential) {
            info += "**IP:** ||" + player.getIp().getHostAddress() + "||\n"
                    + "**CID:** ||" + player.getComputerId() + "||\n";
        }
        return info;
    }

    private MessageEmbed.Field playerExpField(Player player) {
        Duration livingExp = player.getExp().getForRole(RoleCategory.LIVING).orElseThrow();
        Duration ghostExp = player.getExp().getForRole(RoleCategory.GHOST).orElseThrow();
        Duration totalExp = livingExp.plus(ghostExp);

        String title = "Время в игре: " + totalExp.toHours() + " ч.";
        String value = player.getExp().getAll().entrySet().stream()
                .filter(e -> !e.getKey().equals(RoleCategory.IGNORE))
                .map(e -> playerExpLine(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, true);
    }

    private String playerExpLine(RoleCategory category, Duration exp) {
        return UiConstants.SPACE_FILLER.repeat(category.getLevel())
               + category.getFormattedName() + ": " + exp.toHours() + " ч.";
    }

    private MessageEmbed.Field playerCharactersField(@Nullable List<GameCharacter> characters) {
        if (characters == null) {
            String value = Emojis.CONSTRUCTION.getFormatted() + " Пока недоступно.";
            return new MessageEmbed.Field("Персонажи: 0", value, true);
        }

        String title = "Персонажи: " + characters.size();
        String value = characters.stream()
                .map(this::playerCharacterLine)
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, true);
    }

    private String playerCharacterLine(GameCharacter character) {
        String ageFormat = "{0, plural, one{# год} few{# года} many{# лет} other{# лет}}.";
        return "`%02d` %s\n%s %s %s".formatted(
                character.getSlot(), character.getRealName(),
                getGenderEmoji(character.getGender()).getFormatted(),
                character.getSpecies().getName(),
                MessageFormat.format(ageFormat, character.getAge()));
    }

    private Emoji getGenderEmoji(GameCharacter.Gender gender) {
        return switch (gender) {
            case MALE -> Emojis.MALE_SIGN;
            case FEMALE -> Emojis.FEMALE_SIGN;
            case PLURAL -> Emojis.PARKING;
            case OTHER -> Emojis.HELICOPTER;
        };
    }
}
