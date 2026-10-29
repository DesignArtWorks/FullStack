package com.escala.authservice.repository;

import com.escala.authservice.entity.Company;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    /** Serializa operacoes que contam e consomem limites do plano (SELECT ... FOR UPDATE); exige transacao. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Company c where c.id = :id")
    Optional<Company> lockById(@Param("id") UUID id);

    Optional<Company> findBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    long countByPlanType(String planType);
}
