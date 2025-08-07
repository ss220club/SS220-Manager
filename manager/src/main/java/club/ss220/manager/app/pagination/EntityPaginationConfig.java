package club.ss220.manager.app.pagination;

import club.ss220.core.service.PaginatedQuery;
import lombok.Builder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@Builder(toBuilder = true)
public record EntityPaginationConfig<T, Q extends PaginatedQuery<Q>>(
        Q query,
        Function<Q, List<T>> dataProvider,
        ToIntFunction<Q> totalCountProvider,
        Function<SelectOption, T> itemProvider,
        BiFunction<PaginationData<T>, Q, MessageEmbed> pageRenderer,
        Function<List<T>, List<SelectOption>> optionMapper,
        Function<T, MessageEmbed> detailRenderer
) {

    public boolean allowsDetailView() {
        return optionMapper != null && detailRenderer != null;
    }

    public EntityPaginationConfig<T, Q> withQuery(Q query) {
        return toBuilder().query(query).build();
    }
}
