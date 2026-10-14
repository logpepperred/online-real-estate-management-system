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
@RequestMapping("/manager")
public class PropertyManagerController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final RentalApplicationService applicationService;
    private final RentalAgreementService agreementService;
    private final MessageService messageService;

    public PropertyManagerController(UserService userService,
                                     PropertyService propertyService,
                                     RentalApplicationService applicationService,
                                     RentalAgreementService agreementService,
                                     MessageService messageService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.applicationService = applicationService;
        this.agreementService = agreementService;
        this.messageService = messageService;
    }

    private User getManager(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Manager not logged in"));
    }

    // 1. Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User manager = getManager(auth);
        List<Property> myProperties = propertyService.getPropertiesByManager(manager);
        List<RentalApplication> applications = applicationService.getApplicationsByManager(manager);
        List<RentalAgreement> agreements = agreementService.getAgreementsByManager(manager);

        long activeProperties = myProperties.stream().filter(p -> p.getStatus() == PropertyStatus.APPROVED).count();
        long rentedProperties = myProperties.stream().filter(p -> p.getStatus() == PropertyStatus.RENTED).count();
        long pendingApplications = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();
        long totalViews = myProperties.stream().mapToLong(p -> p.getViewsCount() != null ? p.getViewsCount() : 0).sum();

        model.addAttribute("currentUser", manager);
        model.addAttribute("myProperties", myProperties);
        model.addAttribute("totalProperties", myProperties.size());
        model.addAttribute("activeProperties", activeProperties);
        model.addAttribute("rentedProperties", rentedProperties);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("recentApplications", applications.stream().limit(5).toList());
        model.addAttribute("recentAgreements", agreements.stream().limit(5).toList());

        return "manager/dashboard";
    }

    // 2. Property Listings
    @GetMapping("/properties")
    public String listProperties(Model model, Authentication auth) {
        User manager = getManager(auth);
        model.addAttribute("currentUser", manager);
        model.addAttribute("properties", propertyService.getPropertiesByManager(manager));
        return "manager/properties";
    }

    @GetMapping("/properties/new")
    public String newPropertyForm(Model model, Authentication auth) {
        model.addAttribute("currentUser", getManager(auth));
        model.addAttribute("property", new Property());
        model.addAttribute("isEdit", false);
        return "manager/property-form";
    }

    @PostMapping("/properties/save")
    public String saveProperty(@ModelAttribute("property") Property property,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            propertyService.createProperty(property, manager);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Property '" + property.getTitle() + "' submitted successfully! It will be listed once approved by the Admin.");
            return "redirect:/manager/properties";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving property: " + e.getMessage());
            return "redirect:/manager/properties/new";
        }
    }

    @GetMapping("/properties/edit/{id}")
    public String editPropertyForm(@PathVariable("id") Long id, Model model, Authentication auth) {
        User manager = getManager(auth);
        Property property = propertyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));

        if (!property.getManager().getId().equals(manager.getId())) {
            return "redirect:/manager/properties";
        }

        model.addAttribute("currentUser", manager);
        model.addAttribute("property", property);
        model.addAttribute("isEdit", true);
        return "manager/property-form";
    }

    @PostMapping("/properties/update/{id}")
    public String updateProperty(@PathVariable("id") Long id,
                                 @ModelAttribute("property") Property property,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            propertyService.updateProperty(id, property, manager);
            redirectAttributes.addFlashAttribute("successMessage", "Property listing updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating property: " + e.getMessage());
        }
        return "redirect:/manager/properties";
    }

    @PostMapping("/properties/delete/{id}")
    public String deleteProperty(@PathVariable("id") Long id,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            propertyService.deleteProperty(id, manager);
            redirectAttributes.addFlashAttribute("successMessage", "Property listing deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting property: " + e.getMessage());
        }
        return "redirect:/manager/properties";
    }

    // 3. Rental Applications Management
    @GetMapping("/applications")
    public String viewApplications(Model model, Authentication auth) {
        User manager = getManager(auth);
        model.addAttribute("currentUser", manager);
        model.addAttribute("applications", applicationService.getApplicationsByManager(manager));
        return "manager/applications";
    }

    @PostMapping("/applications/approve/{id}")
    public String approveApplication(@PathVariable("id") Long id,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            applicationService.approveApplication(id, manager);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application approved! You can now create a rental agreement for this tenant.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving application: " + e.getMessage());
        }
        return "redirect:/manager/applications";
    }

    @PostMapping("/applications/reject/{id}")
    public String rejectApplication(@PathVariable("id") Long id,
                                    @RequestParam(value = "reason", defaultValue = "Application criteria not met") String reason,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            applicationService.rejectApplication(id, reason, manager);
            redirectAttributes.addFlashAttribute("successMessage", "Application rejected with reason provided.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting application: " + e.getMessage());
        }
        return "redirect:/manager/applications";
    }

    // 4. Rental Agreements Management
    @GetMapping("/agreements")
    public String viewAgreements(Model model, Authentication auth) {
        User manager = getManager(auth);
        model.addAttribute("currentUser", manager);
        model.addAttribute("agreements", agreementService.getAgreementsByManager(manager));
        return "manager/agreements";
    }

    @GetMapping("/agreements/new")
    public String newAgreementForm(@RequestParam(value = "applicationId", required = false) Long applicationId,
                                   Model model, Authentication auth) {
        User manager = getManager(auth);
        model.addAttribute("currentUser", manager);

        RentalAgreement agreement = new RentalAgreement();
        if (applicationId != null) {
            applicationService.findById(applicationId).ifPresent(app -> {
                agreement.setProperty(app.getProperty());
                agreement.setTenant(app.getTenant());
                agreement.setMonthlyRent(app.getProperty().getPrice());
                agreement.setSecurityDeposit(app.getProperty().getSecurityDeposit());
            });
        }

        model.addAttribute("agreement", agreement);
        model.addAttribute("myProperties", propertyService.getPropertiesByManager(manager));
        model.addAttribute("tenants", userService.findByRole(Role.ROLE_TENANT));
        return "manager/agreement-form";
    }

    @PostMapping("/agreements/save")
    public String saveAgreement(@ModelAttribute("agreement") RentalAgreement agreement,
                                @RequestParam("propertyId") Long propertyId,
                                @RequestParam("tenantId") Long tenantId,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            Property property = propertyService.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));
            User tenant = userService.findById(tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

            agreementService.createAgreement(agreement, manager, tenant, property);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Rental agreement created successfully! Property status updated to Rented.");
            return "redirect:/manager/agreements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating agreement: " + e.getMessage());
            return "redirect:/manager/agreements/new";
        }
    }

    @PostMapping("/agreements/terminate/{id}")
    public String terminateAgreement(@PathVariable("id") Long id,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            User manager = getManager(auth);
            agreementService.terminateAgreement(id, manager);
            redirectAttributes.addFlashAttribute("successMessage", "Rental agreement terminated. Property is now available again.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/agreements";
    }

    // 5. Listing Analytics
    @GetMapping("/analytics")
    public String listingAnalytics(Model model, Authentication auth) {
        User manager = getManager(auth);
        List<Property> properties = propertyService.getPropertiesByManager(manager);
        List<RentalAgreement> agreements = agreementService.getAgreementsByManager(manager);
        List<RentalApplication> applications = applicationService.getApplicationsByManager(manager);

        long totalViews = properties.stream().mapToLong(p -> p.getViewsCount() != null ? p.getViewsCount() : 0).sum();
        double monthlyRevenue = agreements.stream()
                .filter(a -> a.getStatus() == AgreementStatus.ACTIVE)
                .mapToDouble(RentalAgreement::getMonthlyRent)
                .sum();

        model.addAttribute("currentUser", manager);
        model.addAttribute("properties", properties);
        model.addAttribute("totalProperties", properties.size());
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("activeAgreementsCount", agreements.stream().filter(a -> a.getStatus() == AgreementStatus.ACTIVE).count());
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        return "manager/analytics";
    }
}
