package com.osy.sharetodo.feature.event.repository;

import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.domain.QEvent;
import com.osy.sharetodo.feature.participant.domain.ParticipantStatus;
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

import static com.osy.sharetodo.feature.event.domain.QEvent.event;
import static com.osy.sharetodo.feature.participant.domain.QParticipant.participant;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Event> searchByOwnerAndFilters(Long ownerPersonId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword, Pageable pageable) {
        OrderSpecifier<?> order = new OrderSpecifier<>(Order.DESC, event.startsAtUtc);

        List<Event> content = queryFactory
                .selectFrom(event)
                .where(whereBuild(ownerPersonId, fromUtc, toUtc, keyword))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(event.count())
                .from(event)
                .where(whereBuild(ownerPersonId, fromUtc, toUtc, keyword))
                .fetchOne();
        long totalElements = total == null ? 0L : total;

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Page<Event> searchByParticipantAndFilters(Long personId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword, Pageable pageable) {
        OrderSpecifier<?> order = new OrderSpecifier<>(Order.DESC, event.startsAtUtc);

        List<Event> content = queryFactory
                .select(event)
                .from(participant)
                .join(participant.event, event)
                .where(
                        participant.person.id.eq(personId),
                        participant.status.eq(ParticipantStatus.ACCEPTED),
                        eventFilters(fromUtc, toUtc, keyword)
                )
                .distinct()
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(event.id.countDistinct())
                .from(participant)
                .join(participant.event, event)
                .where(
                        participant.person.id.eq(personId),
                        participant.status.eq(ParticipantStatus.ACCEPTED),
                        eventFilters(fromUtc, toUtc, keyword)
                )
                .fetchOne();

        long totalElements = total == null ? 0L : total;
        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public List<Event> searchCalendarEvents(Long personId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword) {
        OrderSpecifier<?> order = new OrderSpecifier<>(Order.ASC, event.startsAtUtc);

        return queryFactory
                .select(event)
                .from(event)
                .leftJoin(participant).on(
                        participant.event.eq(event),
                        participant.person.id.eq(personId),
                        participant.status.eq(ParticipantStatus.ACCEPTED)
                )
                .where(
                        event.owner.id.eq(personId).or(participant.id.isNotNull()),
                        eventFilters(fromUtc, toUtc, keyword)
                )
                .distinct()
                .orderBy(order)
                .fetch();
    }


    private BooleanBuilder whereBuild(Long ownerPersonId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(event.owner.id.eq(ownerPersonId));

        if (fromUtc != null) where.and(event.endsAtUtc.goe(fromUtc));
        if (toUtc != null) where.and(event.startsAtUtc.loe(toUtc));

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            where.and(event.title.lower().like(kw).or(event.location.lower().like(kw)));
        }

        return where;
    }

    private BooleanBuilder eventFilters(LocalDateTime fromUtc, LocalDateTime toUtc, String keyword) {
        BooleanBuilder where = new BooleanBuilder();

        if (fromUtc != null) where.and(event.endsAtUtc.goe(fromUtc));
        if (toUtc != null) where.and(event.startsAtUtc.loe(toUtc));

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            where.and(event.title.lower().like(kw).or(event.location.lower().like(kw)));
        }

        return where;
    }
}
