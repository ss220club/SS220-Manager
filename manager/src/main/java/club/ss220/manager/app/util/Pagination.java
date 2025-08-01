package club.ss220.manager.app.util;

import club.ss220.manager.app.pagination.EntityPaginationConfig;
import club.ss220.manager.app.pagination.PaginationData;
import club.ss220.manager.service.PaginatedQuery;
import dev.freya02.jda.emojis.unicode.Emojis;
import io.github.freya022.botcommands.api.ReceiverConsumer;
import io.github.freya022.botcommands.api.components.Button;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;
import io.github.freya022.botcommands.api.components.event.StringSelectEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

@Slf4j
@Component
@RequiresComponents
@AllArgsConstructor
public class Pagination {

    private final Buttons buttons;
    private final SelectMenus selectMenus;
    private final Embeds embeds;

    public <T> StringSelectMenu createSelectMenu(List<T> items,
                                                 Function<List<T>, List<SelectOption>> optionMapper,
                                                 ReceiverConsumer<InteractionConstraints> constraintsManipulator,
                                                 Consumer<StringSelectEvent> selectEventHandler) {
        return selectMenus.stringSelectMenu()
                .ephemeral()
                .constraints(constraintsManipulator)
                .bindTo(selectEventHandler)
                .addOptions(optionMapper.apply(items))
                .build();
    }

    public List<Button> createNavigationButtons(User user, int page, int totalPages, IntConsumer pageChangeAction) {
        return List.of(
                buttons.secondary(Emojis.POINT_LEFT)
                        .ephemeral()
                        .constraints(c -> c.addUsers(user))
                        .bindTo(e -> pageChangeAction.accept(page - 1))
                        .build()
                        .withDisabled(page <= 0),

                buttons.secondary(Emojis.POINT_RIGHT)
                        .ephemeral()
                        .constraints(c -> c.addUsers(user))
                        .bindTo(e -> pageChangeAction.accept(page + 1))
                        .build()
                        .withDisabled(page + 1 >= totalPages)
        );
    }

    public Button createBackButton(User user, Runnable backAction) {
        return buttons.secondary(Emojis.LEFTWARDS_ARROW_WITH_HOOK)
                .ephemeral()
                .constraints(c -> c.addUsers(user))
                .bindTo(e -> backAction.run())
                .build();
    }

    public void replaceWithDetailView(StringSelectEvent event, MessageEmbed detailEmbed, Button backButton) {
        event.editMessageEmbeds(detailEmbed)
                .setComponents(ActionRow.of(backButton))
                .queue();
    }

    public <T, Q extends PaginatedQuery<Q>> void sendPaginatedEntityList(InteractionHook hook,
                                                                         EntityPaginationConfig<T, Q> paginationConfig) {
        Q query = paginationConfig.query();
        int limit = query.getLimit();
        int page = query.getPage();

        List<T> items = paginationConfig.dataProvider().apply(query);
        int totalItems = paginationConfig.totalCountProvider().applyAsInt(query);
        var paginationData = new PaginationData<>(items, limit, page, totalItems, "");
        MessageEmbed embed = paginationConfig.pageRenderer().apply(paginationData, query);

        User user = hook.getInteraction().getUser();
        IntConsumer pageChange = newPage -> {
            Q newQuery = query.withPage(newPage);
            sendPaginatedEntityList(hook, paginationConfig.withQuery(newQuery));
        };


        var builder = new MessageCreateBuilder().setEmbeds(embed);

        // Dropdown + detail view if config provides them
        if (paginationConfig.allowsDetailView()) {
            Consumer<StringSelectEvent> handler = event -> handleSelectEvent(event, hook, paginationConfig, user);
            var optionMapper = paginationConfig.optionMapper();
            StringSelectMenu selectMenu = createSelectMenu(items, optionMapper, c -> c.addUsers(user), handler);
            builder.setComponents(ActionRow.of(selectMenu));
        }
        List<Button> navButtons = createNavigationButtons(user, page, paginationData.getTotalPages(), pageChange);
        builder.setComponents(ActionRow.of(navButtons));

        hook.sendMessage(builder.build())
                .setEphemeral(true)
                .setAllowedMentions(java.util.Collections.emptyList())
                .queue();
    }

    private <T, Q extends PaginatedQuery<Q>> void handleSelectEvent(StringSelectEvent event, InteractionHook hook,
                                                                    EntityPaginationConfig<T, Q> paginationConfig,
                                                                    User user) {
        try {
            SelectOption selectedOption = event.getInteraction().getSelectedOptions().getFirst();
            T selectedItem = paginationConfig.itemProvider().apply(selectedOption);
            MessageEmbed detailEmbed = paginationConfig.detailRenderer().apply(selectedItem);
            Button backButton = createBackButton(user, () -> sendPaginatedEntityList(hook, paginationConfig));
            replaceWithDetailView(event, detailEmbed, backButton);
        } catch (Exception e) {
            log.error("Error handling select event for pagination", e);
            event.replyEmbeds(embeds.error("Произошла ошибка при поиске выбранного элемента."))
                    .setEphemeral(true)
                    .queue();
        }
    }
}
