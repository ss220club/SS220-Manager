package club.ss220.manager.app.command;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import io.github.freya022.botcommands.api.core.GlobalExceptionHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExceptionHandler extends GlobalExceptionHandlerAdapter {

    public static final Duration DISPATCH_INTERVAL = Duration.ofMinutes(10);

    private final Senders senders;
    private final Embeds embeds;
    private final Map<Pair<Class<? extends Event>, Class<? extends Throwable>>, Long> exceptionDispatches;

    public ExceptionHandler(Senders senders, Embeds embeds) {
        this.senders = senders;
        this.embeds = embeds;
        exceptionDispatches = new HashMap<>();
    }

    @Override
    public void handle(@NotNull MessageReceivedEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при обработке сообщения.";
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "message", event.getMessage().getJumpUrl()
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при выполнении команды.";
        Map<String, String> options = event.getInteraction().getOptions().stream()
                .collect(Collectors.toMap(OptionMapping::getName, OptionMapping::getAsString));
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "command", event.getName(),
                "parameters", StringUtils.join(options)
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull MessageContextInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с сообщением через контекстное меню.";
        Map<String, Object> context = Map.of(
                "channel", Optional.ofNullable(event.getChannel()).map(Channel::getAsMention).orElse("null"),
                "message", event.getTarget().getJumpUrl(),
                "command", event.getInteraction().getName()
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull UserContextInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с пользователем через контекстное меню.";
        Map<String, Object> context = Map.of(
                "channel", Optional.ofNullable(event.getChannel()).map(Channel::getAsMention).orElse("null"),
                "user", event.getTarget().getAsMention(),
                "command", event.getInteraction().getName()
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull ButtonInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с кнопкой.";
        String button = event.getButton().getLabel();
        if (event.getButton().getEmoji() != null) {
            button = event.getButton().getEmoji().getFormatted() + " " + button;
        }
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "button", button
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull StringSelectInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с выпадающим списком значений.";
        Map<String, String> options = event.getSelectMenu().getOptions().stream()
                .collect(Collectors.toMap(SelectOption::getLabel, SelectOption::getValue));
        Map<String, String> selectedOptions = event.getSelectedOptions().stream()
                .collect(Collectors.toMap(SelectOption::getLabel, SelectOption::getValue));
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "placeholder", String.valueOf(event.getSelectMenu().getPlaceholder()),
                "options", StringUtils.join(options),
                "selectedOptions", StringUtils.join(selectedOptions)
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull EntitySelectInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с выпадающим списком значений.";
        List<String> options = event.getValues().stream().map(IMentionable::getAsMention).toList();
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "placeholder", String.valueOf(event.getSelectMenu().getPlaceholder()),
                "options", StringUtils.join(options)
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@NotNull ModalInteractionEvent event, @NotNull Throwable throwable) {
        String message = "Произошла ошибка при взаимодействии с модальным окном.";
        Map<String, String> values = event.getValues().stream()
                .collect(Collectors.toMap(ModalMapping::getId, ModalMapping::getAsString));
        Map<String, Object> context = Map.of(
                "channel", event.getChannel().getAsMention(),
                "message", Optional.ofNullable(event.getMessage()).map(Message::getJumpUrl).orElse("null"),
                "modalId", event.getModalId(),
                "values", StringUtils.join(values)
        );
        handleError(event, throwable, message, context);
    }

    @Override
    public void handle(@Nullable Event event, @NotNull Throwable throwable) {
        String message = "Произошла неизвестная ошибка.";
        Map<String, Object> context = Map.of();
        handleError(event, throwable, message, context);
    }

    private void handleError(Event event, Throwable throwable, String message, Map<String, Object> context) {
        log.error(throwable.getMessage(), throwable);
        if (!shouldDispatch(event, throwable)) {
            return;
        }

        MessageEmbed messageEmbed = embeds.uncaughtException(message, new LinkedHashMap<>(context));
        String stackTrace = getFullStackTrace(throwable);
        senders.sendUncaughtExceptionReport(event.getJDA(), messageEmbed, stackTrace);
    }

    private boolean shouldDispatch(Event event, Throwable throwable) {
        Pair<Class<? extends Event>, Class<? extends Throwable>> key = Pair.of(event.getClass(), throwable.getClass());
        long now = System.currentTimeMillis();
        long nextDispatch = exceptionDispatches.getOrDefault(key, 0L);
        if (now < nextDispatch) {
            return false;
        }

        exceptionDispatches.put(key, now + DISPATCH_INTERVAL.toMillis());
        return true;
    }

    public static String getFullStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
