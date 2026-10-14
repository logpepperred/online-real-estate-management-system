package com.realestate.repository;

import com.realestate.model.ApplicationStatus;
import com.realestate.model.Property;
import com.realestate.model.RentalApplication;
import com.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalApplicationRepository extends JpaRepository<RentalApplication, Long> {

    List<RentalApplication> findByTenantOrderByAppliedAtDesc(User tenant);

    List<RentalApplication> findByPropertyOrderByAppliedAtDesc(Property property);

    @Query("SELECT a FROM RentalApplication a WHERE a.property.manager = :manager ORDER BY a.appliedAt DESC")
    List<RentalApplication> findByManagerOrderByAppliedAtDesc(@Param("manager") User manager);

    @Query("SELECT COUNT(a) FROM RentalApplication a WHERE a.property.manager = :manager")
    long countByManager(@Param("manager") User manager);

    @Query("SELECT COUNT(a) FROM RentalApplication a WHERE a.property.manager = :manager AND a.status = :status")
    long countByManagerAndStatus(@Param("manager") User manager, @Param("status") ApplicationStatus status);

    boolean existsByPropertyAndTenant(Property property, User tenant);

    long countByStatus(ApplicationStatus status);
}
