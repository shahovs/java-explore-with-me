package ru.practicum.ewm.server.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.ewm.server.model.Hit;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatsRepositoryImpl implements StatsRepositoryCustom {
    private final EntityManager entityManager;

    public StatsRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<HitShortWithHitsDtoResponse> findAllWithHits(LocalDateTime startTime, LocalDateTime endTime,
                                                             List<String> uris, Boolean unique) {
        // подготовливаем основу для запроса
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HitShortWithHitsDtoResponse> criteriaQuery =
                criteriaBuilder.createQuery(HitShortWithHitsDtoResponse.class);
        Root<Hit> root = criteriaQuery.from(Hit.class);

        // создаем предикаты для where
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.between(root.get("requestTimeStamp"), startTime, endTime));

        if (uris != null && uris.size() != 0) {
            predicates.add(root.get("uri").in(uris));
        }
        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        // указываем что брать (select)
        criteriaQuery.multiselect(root.get("app"), root.get("uri"),
                unique != null && unique
                ? criteriaBuilder.countDistinct(root.get("ip"))
                : criteriaBuilder.count(root.get("ip")));

        // group and order
        criteriaQuery.groupBy(root.get("uri"), root.get("app"));
        criteriaQuery.orderBy(criteriaBuilder.desc(criteriaBuilder.literal(3)));

        // создаем запрос и отправляем
        TypedQuery<HitShortWithHitsDtoResponse> query = entityManager.createQuery(criteriaQuery);
        List<HitShortWithHitsDtoResponse> resultList = query.getResultList();
        return resultList;
    }

}
