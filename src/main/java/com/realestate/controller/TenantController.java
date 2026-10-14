package com.realestate.controller;

import com.realestate.model.*;
import com.realestate.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final RentalApplicationService applicationService;
    private final RentalAgreementService agreementService;
    private final NotificationService notificationService;

    public TenantController(UserService userService,
                            PropertyService propertyService,
                            RentalApplicationService applicationService,
                            RentalAgreementService agreementService,
                            NotificationService notificationService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.applicationService = applicationService;
        this.agreementService = agreementService;
        this.notificationService = notificationService;
    }

    private User getTenant(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Tenant not logged in"));
    }

    // 1. Tenant Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User tenant = getTenant(auth);
        List<RentalApplication> applications = applicationService.getApplicationsByTenant(tenant);
        List<RentalAgreement> agreements = agreementService.getAgreementsByTenant(tenant);
        List<Notification> notifications = notificationService.getRecentUserNotifications(tenant);

        long activeAgreementsCount = agreements.stream().filter(a -> a.getStatus() == AgreementStatus.ACTIVE).count();
        long pendingApplicationsCount = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();

        model.addAttribute("currentUser", tenant);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("pendingApplicationsCount", pendingApplicationsCount);
        model.addAttribute("activeAgreementsCount", activeAgreementsCount);
        model.addAttribute("unreadNotificationsCount", notificationService.getUnreadCount(tenant));
        model.addAttribute("recentApplications", applications.stream().limit(5).toList());
        model.addAttribute("activeAgreements", agreements.stream().filter(a -> a.getStatus() == AgreementStatus.ACTIVE).toList());
        model.addAttribute("recentNotifications", notifications);

        return "tenant/dashboard";
    }

    // 2. Property Search
    @GetMapping("/search")
    public String searchProperties(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "propertyType", required = false) String propertyType,
                                   @RequestParam(value = "minPrice", required = false) Double minPrice,
                                   @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                   @RequestParam(value = "bedrooms", required = false) Integer bedrooms,
                                   Model model, Authentication auth) {
        model.addAttribute("currentUser", getTenant(auth));
        List<Property> properties = propertyService.searchProperties(keyword, propertyType, minPrice, maxPrice, bedrooms);
        model.addAttribute("properties", properties);
        model.addAttribute("keyword", keyword);
        model.addAttribute("propertyType", propertyType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("bedrooms", bedrooms);
        return "tenant/search";
    }

    // 3. Rental Application
    @GetMapping("/apply/{propertyId}")
    public String applyForm(@PathVariable("propertyId") Long propertyId, Model model, Authentication auth) {
        User tenant = getTenant(auth);
        Property property = propertyService.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        model.addAttribute("currentUser", tenant);
        model.addAttribute("property", property);
        model.addAttribute("application", new RentalApplication());
        return "tenant/apply-form";
    }

    @PostMapping("/apply/submit")
    public String submitApplication(@ModelAttribute("application") RentalApplication application,
                                    @RequestParam("propertyId") Long propertyId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            User tenant = getTenant(auth);
            Property property = propertyService.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            applicationService.applyForProperty(application, tenant, property);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application submitted successfully for '" + property.getTitle() + "'! The property manager has been notified.");
            return "redirect:/tenant/applications";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tenant/search";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting application: " + e.getMessage());
            return "redirect:/tenant/search";
        }
    }

    @GetMapping("/applications")
    public String viewApplications(Model model, Authentication auth) {
        User tenant = getTenant(auth);
        model.addAttribute("currentUser", tenant);
        model.addAttribute("applications", applicationService.getApplicationsByTenant(tenant));
        return "tenant/applications";
    }

    // 4. Agreement Management
    @GetMapping("/agreements")
    public String viewAgreements(Model model, Authentication auth) {
        User tenant = getTenant(auth);
        model.addAttribute("currentUser", tenant);
        model.addAttribute("agreements", agreementService.getAgreementsByTenant(tenant));
        return "tenant/agreements";
    }

    // 5. Profile Management
    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        User tenant = getTenant(auth);
        model.addAttribute("currentUser", tenant);
        return "tenant/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("bio") String bio,
                                @RequestParam("rentalHistoryNotes") String rentalHistoryNotes,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            User tenant = getTenant(auth);
            userService.updateProfile(tenant, name, phoneNumber, bio, rentalHistoryNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/tenant/profile";
    }

    // 6. Notifications
    @GetMapping("/notifications")
    public String viewNotifications(Model model, Authentication auth) {
        User tenant = getTenant(auth);
        model.addAttribute("currentUser", tenant);
        model.addAttribute("notifications", notificationService.getUserNotifications(tenant));
        return "tenant/notifications";
    }

    @PostMapping("/notifications/mark-read/{id}")
    public String markNotificationRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/tenant/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead(Authentication auth) {
        User tenant = getTenant(auth);
        notificationService.markAllAsRead(tenant);
        return "redirect:/tenant/notifications";
    }
}
