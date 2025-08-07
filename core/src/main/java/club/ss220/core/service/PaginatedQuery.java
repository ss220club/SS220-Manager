package club.ss220.core.service;

public interface PaginatedQuery<Q extends PaginatedQuery<Q>> {

    int getLimit();

    int getPage();

    Q withPage(int page);
}
