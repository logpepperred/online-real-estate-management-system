package com.realestate.service;

import com.realestate.model.AgreementStatus;
import com.realestate.model.Property;
import com.realestate.model.PropertyStatus;
import com.realestate.model.RentalAgreement;
import com.realestate.model.User;
import com.realestate.repository.PropertyRepository;
import com.realestate.repository.RentalAgreementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalAgreementService {

    private final RentalAgreementRepository agreementRepository;
    private final PropertyRepository propertyRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public RentalAgreementService(RentalAgreementRepository agreementRepository,
                                  PropertyRepository propertyRepository,
                                  ActivityLogService activityLogService,
                                  NotificationService notificationService) {
        this.agreementRepository = agreementRepository;
        this.propertyRepository = propertyRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    public RentalAgreement createAgreement(RentalAgreement agreement, User manager, User tenant, Property property) {
        agreement.setManager(manager);
        agreement.setTenant(tenant);
        agreement.setProperty(property);
        agreement.setStatus(AgreementStatus.ACTIVE);

        RentalAgreement saved = agreementRepository.save(agreement);

        // Update property status to RENTED
        property.setStatus(PropertyStatus.RENTED);
        propertyRepository.save(property);

        activityLogService.logActivity(manager, "AGREEMENT_CREATED",
                "Created rental agreement for '" + property.getTitle() + "' with tenant " + tenant.getName());

        notificationService.sendNotification(
                tenant,
                "New Rental Agreement Issued",
                "A rental agreement has been issued for '" + property.getTitle() + "'. You can view the details in your dashboard.",
                "/tenant/agreements"
        );

        return saved;
    }

    public List<RentalAgreement> getAgreementsByManager(User manager) {
        return agreementRepository.findByManagerOrderByCreatedAtDesc(manager);
    }

    public List<RentalAgreement> getAgreementsByTenant(User tenant) {
        return agreementRepository.findByTenantOrderByCreatedAtDesc(tenant);
    }

    public List<RentalAgreement> getAllAgreements() {
        return agreementRepository.findAll();
    }

    public Optional<RentalAgreement> findById(Long id) {
        return agreementRepository.findById(id);
    }

    public void terminateAgreement(Long agreementId, User actor) {
        RentalAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("Agreement not found: " + agreementId));

        agreement.setStatus(AgreementStatus.TERMINATED);
        agreementRepository.save(agreement);

        // Reset property status back to APPROVED so it can be rented again
        Property property = agreement.getProperty();
        property.setStatus(PropertyStatus.APPROVED);
        propertyRepository.save(property);

        activityLogService.logActivity(actor, "AGREEMENT_TERMINATED",
                "Terminated rental agreement for '" + property.getTitle() + "'");

        notificationService.sendNotification(
                agreement.getTenant(),
                "Rental Agreement Terminated",
                "Your rental agreement for '" + property.getTitle() + "' has been terminated.",
                "/tenant/agreements"
        );
    }

    public long countByStatus(AgreementStatus status) {
        return agreementRepository.countByStatus(status);
    }

    public long countByManager(User manager) {
        return agreementRepository.countByManager(manager);
    }

    public long countAllAgreements() {
        return agreementRepository.count();
    }
}
