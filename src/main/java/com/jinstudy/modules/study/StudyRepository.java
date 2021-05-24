package com.jinstudy.modules.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    @EntityGraph(value="Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    boolean existsByPath(String path);

    @EntityGraph(value="Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagByPath(String path);
    // WithTag는 JPA에 무의미한 키워드임!! 결국 이렇게 하면 결과는 findByPath와 동일한데 다른 @EntityGraph를 사용 가능.
    @EntityGraph(value="Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZoneByPath(String path);

    @EntityGraph(value="Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value="Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);
}
