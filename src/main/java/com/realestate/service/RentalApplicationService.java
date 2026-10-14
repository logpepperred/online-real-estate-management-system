package com.realestate.service;

import com.realestate.model.ApplicationStatus;
import com.realestate.model.Property;
import com.realestate.model.RentalApplication;
import com.realestate.model.User;
import com.realestate.repository.RentalApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalApplicationService {

    private final RentalApplicationRepository applicationRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public RentalApplicationService(RentalApplicationRepository applicationRepository,
                                   ActivityLogService activityLogService,
                                   NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    public RentalApplication applyForProperty(RentalApplication application, User tenant, Property property) {
        if (applicationRepository.existsByPropertyAndTenant(property, tenant)) {
            throw new IllegalArgumentException("You have already applied for this property.");
        }

        application.setTenant(tenant);
        application.setProperty(property);
        application.setStatus(ApplicationStatus.PENDING);

        RentalApplication saved = applicationRepository.save(application);

        activityLogService.logActivity(tenant, "APPLICATION_SUBMITTED",
                "Tenant submitted rental application for: '" + property.getTitle() + "'");

        // Notify property manager
        notificationService.sendNotification(
                property.getManager(),
                "New Rental Application",
                tenant.getName() + " has applied for your property: '" + property.getTitle() + "'",
                "/manager/applications"
        );

        return saved;
    }

    public List<RentalApplication> getApplicationsByTenant(User tenant) {
        return applicationRepository.findByTenantOrderByAppliedAtDesc(tenant);
    }

    public List<RentalApplication> getApplicationsByManager(User manager) {
        return applicationRepository.findByManagerOrderByAppliedAtDesc(manager);
    }

    public Optional<RentalApplication> findById(Long id) {
        return applicationRepository.findById(id);
    }

    public void approveApplication(Long applicationId, User manager) {
        RentalApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        application.setStatus(ApplicationStatus.APPROVED);
        application.setRejectionReason(null);
        applicationRepository.save(application);

        activityLogService.logActivity(manager, "APPLICATION_APPROVED",
                "Manager approved rental application for: " + application.getTenant().getName() + " on " + application.getProperty().getTitle());

        notificationService.sendNotification(
                application.getTenant(),
                "Application Approved!",
                "Great news! Your application for '" + application.getProperty().getTitle() + "' has been approved. The manager will issue your rental agreement shortly.",
                "/tenant/applications"
        );
    }

    public void rejectApplication(Long applicationId, String reason, User manager) {
        RentalApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        applicationRepository.save(application);

        activityLogService.logActivity(manager, "APPLICATION_REJECTED",
                "Manager rejected rental application for: " + application.getTenant().getName() + " on " + application.getProperty().getTitle());

        notificationService.sendNotification(
                application.getTenant(),
                "Application Status Update",
                "Your application for '" + application.getProperty().getTitle() + "' was not accepted. Reason: " + reason,
                "/tenant/applications"
        );
    }

    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }

    public long countByManager(User manager) {
        return applicationRepository.countByManager(manager);
    }

    public long countByManagerAndStatus(User manager, ApplicationStatus status) {
        return applicationRepository.countByManagerAndStatus(manager, status);
    }

    public long countAllApplications() {
        return applicationRepository.count();
    }
}
