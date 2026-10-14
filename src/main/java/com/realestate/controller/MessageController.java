package com.realestate.controller;

import com.realestate.model.Message;
import com.realestate.model.Property;
import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.service.MessageService;
import com.realestate.service.PropertyService;
import com.realestate.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final PropertyService propertyService;

    public MessageController(MessageService messageService,
                             UserService userService,
                             PropertyService propertyService) {
        this.messageService = messageService;
        this.userService = userService;
        this.propertyService = propertyService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not logged in"));
    }

    @GetMapping
    public String messageCenter(@RequestParam(value = "with", required = false) Long otherUserId,
                                @RequestParam(value = "propertyId", required = false) Long propertyId,
                                Model model, Authentication auth) {
        User currentUser = getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);

        // List potential contact partners based on role
        if (currentUser.getRole() == Role.ROLE_PROPERTY_MANAGER) {
            model.addAttribute("contacts", userService.findByRole(Role.ROLE_TENANT));
        } else if (currentUser.getRole() == Role.ROLE_TENANT) {
            model.addAttribute("contacts", userService.findByRole(Role.ROLE_PROPERTY_MANAGER));
        } else {
            model.addAttribute("contacts", userService.getAllUsers());
        }

        // If a specific conversation is selected
        if (otherUserId != null) {
            userService.findById(otherUserId).ifPresent(otherUser -> {
                model.addAttribute("activeChatUser", otherUser);
                List<Message> conversation = messageService.getConversation(currentUser, otherUser);
                model.addAttribute("messages", conversation);
            });
        }

        if (propertyId != null) {
            propertyService.findById(propertyId).ifPresent(p -> model.addAttribute("relatedProperty", p));
        }

        return "messages";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("recipientId") Long recipientId,
                              @RequestParam(value = "propertyId", required = false) Long propertyId,
                              @RequestParam(value = "subject", required = false) String subject,
                              @RequestParam("content") String content,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            User sender = getCurrentUser(auth);
            User recipient = userService.findById(recipientId)
                    .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + recipientId));

            Property property = null;
            if (propertyId != null) {
                property = propertyService.findById(propertyId).orElse(null);
            }

            messageService.sendMessage(sender, recipient, property, subject, content);
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully to " + recipient.getName() + "!");
            return "redirect:/messages?with=" + recipientId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error sending message: " + e.getMessage());
            return "redirect:/messages";
        }
    }
}
