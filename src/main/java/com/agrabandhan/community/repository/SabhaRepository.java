package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.SamajSabha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SabhaRepository extends JpaRepository<SamajSabha, Long> {
    List<SamajSabha> findByCityIgnoreCaseAndActiveTrue(String city);
    List<SamajSabha> findByStateIgnoreCaseAndActiveTrue(String state);
    Page<SamajSabha> findByActiveTrue(Pageable pageable);
}
