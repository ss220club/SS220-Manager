package club.ss220.manager.service;

public interface PaginatedQuery<Q extends PaginatedQuery<Q>> {

    int getLimit();

    int getPage();

    Q withPage(int page);
}
