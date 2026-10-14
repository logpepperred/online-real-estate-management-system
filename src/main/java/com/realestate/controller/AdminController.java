package com.realestate.controller;

import com.realestate.model.*;
import com.realestate.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final RentalApplicationService applicationService;
    private final RentalAgreementService agreementService;
    private final ActivityLogService activityLogService;
    private final SystemSettingService settingService;

    public AdminController(UserService userService,
                           PropertyService propertyService,
                           RentalApplicationService applicationService,
                           RentalAgreementService agreementService,
                           ActivityLogService activityLogService,
                           SystemSettingService settingService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.applicationService = applicationService;
        this.agreementService = agreementService;
        this.activityLogService = activityLogService;
        this.settingService = settingService;
    }

    private User getAdminUser(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    // 1. Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("totalProperties", propertyService.countAllProperties());
        model.addAttribute("pendingListings", propertyService.countByStatus(PropertyStatus.PENDING_APPROVAL));
        model.addAttribute("approvedListings", propertyService.countByStatus(PropertyStatus.APPROVED));
        model.addAttribute("totalAgreements", agreementService.countAllAgreements());
        model.addAttribute("activeAgreements", agreementService.countByStatus(AgreementStatus.ACTIVE));
        model.addAttribute("totalApplications", applicationService.countAllApplications());

        model.addAttribute("recentActivities", activityLogService.getRecentActivities());
        model.addAttribute("allProperties", propertyService.getAllProperties());
        model.addAttribute("allUsers", userService.getAllUsers());

        return "admin/dashboard";
    }

    // 2. User Management
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "role", required = false) String roleStr,
                            Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        List<User> users;
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                Role role = Role.valueOf(roleStr);
                users = userService.findByRole(role);
            } catch (Exception e) {
                users = userService.getAllUsers();
            }
        } else {
            users = userService.getAllUsers();
        }
        model.addAttribute("users", users);
        model.addAttribute("selectedRole", roleStr);
        model.addAttribute("newUser", new User());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("role") Role role,
                             @RequestParam(value = "phoneNumber", required = false) String phone,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(name, email, password, role, phone);
            redirectAttributes.addFlashAttribute("successMessage", "User account created successfully for: " + email);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("role") Role role,
                             @RequestParam(value = "phoneNumber", required = false) String phone,
                             @RequestParam(value = "enabled", defaultValue = "true") boolean enabled,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, name, email, role, phone, enabled);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully: " + email);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 3. Property Listing Management
    @GetMapping("/properties")
    public String manageProperties(Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        model.addAttribute("properties", propertyService.getAllProperties());
        model.addAttribute("pendingCount", propertyService.countByStatus(PropertyStatus.PENDING_APPROVAL));
        model.addAttribute("approvedCount", propertyService.countByStatus(PropertyStatus.APPROVED));
        model.addAttribute("rejectedCount", propertyService.countByStatus(PropertyStatus.REJECTED));
        return "admin/properties";
    }

    @PostMapping("/properties/approve/{id}")
    public String approveProperty(@PathVariable("id") Long id,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            User admin = getAdminUser(auth);
            propertyService.approveProperty(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Listing approved successfully and is now active for tenants!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving listing: " + e.getMessage());
        }
        return "redirect:/admin/properties";
    }

    @PostMapping("/properties/reject/{id}")
    public String rejectProperty(@PathVariable("id") Long id,
                                 @RequestParam(value = "reason", defaultValue = "Does not comply with listing guidelines") String reason,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            User admin = getAdminUser(auth);
            propertyService.rejectProperty(id, reason, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Listing has been rejected with feedback sent to manager.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting listing: " + e.getMessage());
        }
        return "redirect:/admin/properties";
    }

    // 4. System Settings
    @GetMapping("/settings")
    public String settingsPage(Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        model.addAttribute("settings", settingService.getAllSettings());
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> allParams,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        allParams.remove("_csrf");
        settingService.updateBatchSettings(allParams);
        activityLogService.logActivity(getAdminUser(auth), "SETTINGS_UPDATED", "Updated system configurations.");
        redirectAttributes.addFlashAttribute("successMessage", "System settings updated successfully!");
        return "redirect:/admin/settings";
    }

    // 5. Activity Monitoring
    @GetMapping("/activity-logs")
    public String activityMonitoring(Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        model.addAttribute("logs", activityLogService.getAllActivities());
        return "admin/activity-logs";
    }

    // 6. Performance Reports & Analytics
    @GetMapping("/reports")
    public String performanceReports(Model model, Authentication auth) {
        model.addAttribute("currentUser", getAdminUser(auth));
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("totalManagers", userService.countByRole(Role.ROLE_PROPERTY_MANAGER));
        model.addAttribute("totalTenants", userService.countByRole(Role.ROLE_TENANT));

        model.addAttribute("totalProperties", propertyService.countAllProperties());
        model.addAttribute("approvedProperties", propertyService.countByStatus(PropertyStatus.APPROVED));
        model.addAttribute("rentedProperties", propertyService.countByStatus(PropertyStatus.RENTED));
        model.addAttribute("pendingProperties", propertyService.countByStatus(PropertyStatus.PENDING_APPROVAL));

        model.addAttribute("totalAgreements", agreementService.countAllAgreements());
        model.addAttribute("activeAgreements", agreementService.countByStatus(AgreementStatus.ACTIVE));
        model.addAttribute("terminatedAgreements", agreementService.countByStatus(AgreementStatus.TERMINATED));

        model.addAttribute("totalApplications", applicationService.countAllApplications());
        model.addAttribute("approvedApplications", applicationService.countByStatus(ApplicationStatus.APPROVED));
        model.addAttribute("pendingApplications", applicationService.countByStatus(ApplicationStatus.PENDING));

        return "admin/reports";
    }
}
