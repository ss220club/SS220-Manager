package club.ss220.manager.app.pagination;

import java.util.List;

public record PaginationData<T>(
        List<T> items,
        int limit,
        int page,
        int totalItems,
        String title
) {

    public int getTotalPages() {
        return totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / limit);
    }
}
