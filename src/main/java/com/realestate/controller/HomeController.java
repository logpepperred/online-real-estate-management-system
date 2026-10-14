package com.realestate.controller;

import com.realestate.model.Property;
import com.realestate.model.PropertyStatus;
import com.realestate.model.User;
import com.realestate.service.PropertyService;
import com.realestate.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final PropertyService propertyService;
    private final UserService userService;

    public HomeController(PropertyService propertyService, UserService userService) {
        this.propertyService = propertyService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model, Authentication auth) {
        List<Property> featuredProperties = propertyService.getApprovedProperties();
        model.addAttribute("featuredProperties", featuredProperties);
        model.addAttribute("totalProperties", propertyService.countByStatus(PropertyStatus.APPROVED));
        model.addAttribute("totalManagers", userService.countByRole(com.realestate.model.Role.ROLE_PROPERTY_MANAGER));
        model.addAttribute("totalTenants", userService.countByRole(com.realestate.model.Role.ROLE_TENANT));

        if (auth != null && auth.isAuthenticated()) {
            userService.findByEmail(auth.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "index";
    }

    @GetMapping("/properties")
    public String browseProperties(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "propertyType", required = false) String propertyType,
                                   @RequestParam(value = "minPrice", required = false) Double minPrice,
                                   @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                   @RequestParam(value = "bedrooms", required = false) Integer bedrooms,
                                   Model model, Authentication auth) {

        List<Property> properties = propertyService.searchProperties(keyword, propertyType, minPrice, maxPrice, bedrooms);
        model.addAttribute("properties", properties);
        model.addAttribute("keyword", keyword);
        model.addAttribute("propertyType", propertyType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("bedrooms", bedrooms);

        if (auth != null && auth.isAuthenticated()) {
            userService.findByEmail(auth.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "properties";
    }

    @GetMapping("/properties/{id}")
    public String propertyDetails(@PathVariable("id") Long id, Model model, Authentication auth) {
        Optional<Property> opt = propertyService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = opt.get();
        // Increment view count
        propertyService.incrementViews(property);

        model.addAttribute("property", property);

        if (auth != null && auth.isAuthenticated()) {
            userService.findByEmail(auth.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "property-detail";
    }
}
