package ru.practicum.ewm.server.repository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.server.model.Hit;
import ru.practicum.ewm.server.model.HitShortWithHits;

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
    private final StatsRepository statsRepository;
    private final EntityManager entityManager;

    public StatsRepositoryImpl(@Lazy StatsRepository statsRepository, EntityManager entityManager) {
        this.statsRepository = statsRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<HitShortWithHits> findAllWithHits(LocalDateTime startTime, LocalDateTime endTime,  String[] uris) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HitShortWithHits> criteriaQuery = criteriaBuilder.createQuery(HitShortWithHits.class);
        Root<Hit> root = criteriaQuery.from(Hit.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.between(root.get("request_time_stamp"), startTime, endTime));
        if (uris != null) {
            List<String> urisList = List.of(uris);
            predicates.add(root.get("uri").in(urisList));
        }
        criteriaQuery.multiselect(criteriaBuilder.count(root.get("uri")));
//        predicates.add(criteriaQuery.multiselect(criteriaBuilder.count(root.get("uri"))));
        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        TypedQuery<HitShortWithHits> query = entityManager.createQuery(criteriaQuery);
        List<HitShortWithHits> resultList = query.getResultList();
        return resultList;
    }

}
