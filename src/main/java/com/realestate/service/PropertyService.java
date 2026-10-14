package com.realestate.service;

import com.realestate.model.Property;
import com.realestate.model.PropertyStatus;
import com.realestate.model.User;
import com.realestate.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public PropertyService(PropertyRepository propertyRepository,
                           ActivityLogService activityLogService,
                           NotificationService notificationService) {
        this.propertyRepository = propertyRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public List<Property> getApprovedProperties() {
        return propertyRepository.findByStatusOrderByCreatedAtDesc(PropertyStatus.APPROVED);
    }

    public List<Property> getPropertiesByManager(User manager) {
        return propertyRepository.findByManagerOrderByCreatedAtDesc(manager);
    }

    public Optional<Property> findById(Long id) {
        return propertyRepository.findById(id);
    }

    public Property createProperty(Property property, User manager) {
        property.setManager(manager);
        property.setStatus(PropertyStatus.PENDING_APPROVAL);
        Property saved = propertyRepository.save(property);
        activityLogService.logActivity(manager, "PROPERTY_SUBMITTED",
                "Manager submitted property listing: '" + saved.getTitle() + "' in " + saved.getCity());
        return saved;
    }

    public Property updateProperty(Long id, Property updated, User manager) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        // Check ownership or admin
        if (!existing.getManager().getId().equals(manager.getId()) && !manager.getRole().name().equals("ROLE_ADMIN")) {
            throw new SecurityException("Unauthorized to modify this property listing");
        }

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setZipCode(updated.getZipCode());
        existing.setPrice(updated.getPrice());
        existing.setSecurityDeposit(updated.getSecurityDeposit());
        existing.setBedrooms(updated.getBedrooms());
        existing.setBathrooms(updated.getBathrooms());
        existing.setAreaSqFt(updated.getAreaSqFt());
        existing.setPropertyType(updated.getPropertyType());
        if (updated.getImageUrl() != null && !updated.getImageUrl().trim().isEmpty()) {
            existing.setImageUrl(updated.getImageUrl());
        }

        Property saved = propertyRepository.save(existing);
        activityLogService.logActivity(manager, "PROPERTY_UPDATED",
                "Updated listing details for: '" + saved.getTitle() + "'");
        return saved;
    }

    public void approveProperty(Long id, User admin) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        property.setStatus(PropertyStatus.APPROVED);
        property.setRejectionReason(null);
        propertyRepository.save(property);

        activityLogService.logActivity(admin, "PROPERTY_APPROVED",
                "Admin approved property listing: '" + property.getTitle() + "'");

        notificationService.sendNotification(
                property.getManager(),
                "Listing Approved",
                "Your property '" + property.getTitle() + "' has been approved and is now live!",
                "/manager/properties"
        );
    }

    public void rejectProperty(Long id, String reason, User admin) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        property.setStatus(PropertyStatus.REJECTED);
        property.setRejectionReason(reason);
        propertyRepository.save(property);

        activityLogService.logActivity(admin, "PROPERTY_REJECTED",
                "Admin rejected property listing: '" + property.getTitle() + "'. Reason: " + reason);

        notificationService.sendNotification(
                property.getManager(),
                "Listing Rejected",
                "Your property '" + property.getTitle() + "' was rejected. Reason: " + reason,
                "/manager/properties"
        );
    }

    public void deleteProperty(Long id, User user) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        if (!property.getManager().getId().equals(user.getId()) && !user.getRole().name().equals("ROLE_ADMIN")) {
            throw new SecurityException("Unauthorized to delete this property");
        }

        activityLogService.logActivity(user, "PROPERTY_DELETED",
                "Deleted property listing: '" + property.getTitle() + "'");
        propertyRepository.delete(property);
    }

    public void incrementViews(Property property) {
        property.setViewsCount(property.getViewsCount() + 1);
        propertyRepository.save(property);
    }

    public List<Property> searchProperties(String keyword, String propertyType, Double minPrice, Double maxPrice, Integer bedrooms) {
        return propertyRepository.searchProperties(keyword, propertyType, minPrice, maxPrice, bedrooms);
    }

    public long countByStatus(PropertyStatus status) {
        return propertyRepository.countByStatus(status);
    }

    public long countByManager(User manager) {
        return propertyRepository.countByManager(manager);
    }

    public long countAllProperties() {
        return propertyRepository.count();
    }
}
