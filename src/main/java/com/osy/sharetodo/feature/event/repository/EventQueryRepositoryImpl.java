package com.osy.sharetodo.feature.event.repository;

import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.domain.QEvent;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Event> searchByOwnerAndFilters(Long ownerPersonId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword, Pageable pageable) {
        QEvent e = QEvent.event;

        OrderSpecifier<?> order = new OrderSpecifier<>(Order.DESC, e.startsAtUtc);

        List<Event> content = queryFactory
                .selectFrom(e)
                .where(whereBuild(ownerPersonId, fromUtc, toUtc, keyword))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(whereBuild(ownerPersonId, fromUtc, toUtc, keyword))
                .fetchOne();
        long totalElements = total == null ? 0L : total;

        return new PageImpl<>(content, pageable, totalElements);
    }

    private BooleanBuilder whereBuild(Long ownerPersonId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword) {
        QEvent e = QEvent.event;
        BooleanBuilder where = new BooleanBuilder();

        where.and(e.owner.id.eq(ownerPersonId));

        if (fromUtc != null) {
            where.and(e.endsAtUtc.goe(fromUtc));
        }
        if (toUtc != null) {
            where.and(e.startsAtUtc.loe(toUtc));
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            where.and(
                    e.title.lower().like(kw)
                            .or(e.location.lower().like(kw))
            );
        }

        return where;
    }
}
