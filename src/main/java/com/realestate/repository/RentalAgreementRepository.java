package com.realestate.repository;

import com.realestate.model.AgreementStatus;
import com.realestate.model.Property;
import com.realestate.model.RentalAgreement;
import com.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalAgreementRepository extends JpaRepository<RentalAgreement, Long> {

    List<RentalAgreement> findByTenantOrderByCreatedAtDesc(User tenant);

    List<RentalAgreement> findByManagerOrderByCreatedAtDesc(User manager);

    List<RentalAgreement> findByPropertyOrderByCreatedAtDesc(Property property);

    List<RentalAgreement> findByManagerAndStatus(User manager, AgreementStatus status);

    long countByStatus(AgreementStatus status);

    long countByManager(User manager);
}
