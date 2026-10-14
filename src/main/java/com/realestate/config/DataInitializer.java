package com.realestate.config;

import com.realestate.model.*;
import com.realestate.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RentalApplicationRepository applicationRepository;
    private final RentalAgreementRepository agreementRepository;
    private final MessageRepository messageRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PropertyRepository propertyRepository,
                           RentalApplicationRepository applicationRepository,
                           RentalAgreementRepository agreementRepository,
                           MessageRepository messageRepository,
                           SystemSettingRepository systemSettingRepository,
                           ActivityLogRepository activityLogRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.applicationRepository = applicationRepository;
        this.agreementRepository = agreementRepository;
        this.messageRepository = messageRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.activityLogRepository = activityLogRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Database already seeded
        }

        System.out.println(">>> Initializing sample data for Online Real Estate Management System...");

        // 1. Create Users
        User admin = new User("System Administrator", "admin@realestate.com",
                passwordEncoder.encode("admin123"), Role.ROLE_ADMIN, "+1 (555) 100-2000");

        User manager1 = new User("Sarah Jenkins (Property Manager)", "manager@realestate.com",
                passwordEncoder.encode("manager123"), Role.ROLE_PROPERTY_MANAGER, "+1 (555) 234-5678");
        manager1.setBio("Licensed Property Manager with 8+ years managing luxury and residential properties.");

        User manager2 = new User("David Miller (Property Manager)", "david@realestate.com",
                passwordEncoder.encode("manager123"), Role.ROLE_PROPERTY_MANAGER, "+1 (555) 345-6789");
        manager2.setBio("Specializing in suburban homes, student housing, and commercial rentals.");

        User tenant1 = new User("Alex Johnson (Tenant)", "tenant@realestate.com",
                passwordEncoder.encode("tenant123"), Role.ROLE_TENANT, "+1 (555) 987-6543");
        tenant1.setBio("Software Engineer working remotely. Quiet, non-smoker, reliable rent payer.");
        tenant1.setRentalHistoryNotes("Rented 2-bedroom in Austin for 2 years (2022-2024). Perfect on-time payment record.");

        User tenant2 = new User("Emily Davis (Tenant)", "emily@realestate.com",
                passwordEncoder.encode("tenant123"), Role.ROLE_TENANT, "+1 (555) 876-5432");
        tenant2.setBio("Postgraduate medical researcher looking for a tranquil 1BHK/studio apartment.");

        userRepository.saveAll(Arrays.asList(admin, manager1, manager2, tenant1, tenant2));

        // 2. System Settings
        List<SystemSetting> settings = Arrays.asList(
                new SystemSetting("site_name", "EstateHub Real Estate", "Name of the web application", "GENERAL"),
                new SystemSetting("currency_symbol", "$", "Currency symbol for rents and deposits", "FINANCIAL"),
                new SystemSetting("contact_email", "support@estatehub.com", "System support email address", "GENERAL"),
                new SystemSetting("service_fee_percent", "5.0", "Service fee percentage for managers", "FINANCIAL"),
                new SystemSetting("max_upload_size_mb", "10", "Maximum photo upload size in Megabytes", "SYSTEM"),
                new SystemSetting("maintenance_mode", "false", "Whether the system is in maintenance mode", "SYSTEM")
        );
        systemSettingRepository.saveAll(settings);

        // 3. Properties
        Property p1 = new Property();
        p1.setTitle("Luxury Downtown Loft with Skyline Views");
        p1.setDescription("Stunning contemporary loft located in the heart of downtown. Features 12ft high ceilings, floor-to-ceiling panoramic windows, exposed brick, chef kitchen with stainless steel appliances, and in-unit washer/dryer.");
        p1.setAddress("450 Market Street, Apt 14B");
        p1.setCity("San Francisco");
        p1.setState("CA");
        p1.setZipCode("94105");
        p1.setPrice(2800.0);
        p1.setSecurityDeposit(2800.0);
        p1.setBedrooms(2);
        p1.setBathrooms(2);
        p1.setAreaSqFt(1150.0);
        p1.setPropertyType("Apartment");
        p1.setImageUrl("https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80");
        p1.setStatus(PropertyStatus.APPROVED);
        p1.setManager(manager1);
        p1.setViewsCount(142);

        Property p2 = new Property();
        p2.setTitle("Modern Sunset Villa with Private Garden");
        p2.setDescription("Spacious suburban villa offering tranquility and modern comfort. Includes private fenced backyard, heated swimming pool, 2-car garage, solar panels, and hardwood floors throughout.");
        p2.setAddress("782 Meadowlark Lane");
        p2.setCity("Austin");
        p2.setState("TX");
        p2.setZipCode("78704");
        p2.setPrice(3500.0);
        p2.setSecurityDeposit(3500.0);
        p2.setBedrooms(4);
        p2.setBathrooms(3);
        p2.setAreaSqFt(2600.0);
        p2.setPropertyType("Villa");
        p2.setImageUrl("https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80");
        p2.setStatus(PropertyStatus.APPROVED);
        p2.setManager(manager1);
        p2.setViewsCount(210);

        Property p3 = new Property();
        p3.setTitle("Chic Minimalist Studio near Tech Campus");
        p3.setDescription("Bright and airy open-concept studio perfect for students or working professionals. Comes fully furnished with high-speed fiber internet connection, modern kitchenette, and secure keycard entry.");
        p3.setAddress("120 Innovation Way, Unit 302");
        p3.setCity("Seattle");
        p3.setState("WA");
        p3.setZipCode("98109");
        p3.setPrice(1350.0);
        p3.setSecurityDeposit(1350.0);
        p3.setBedrooms(1);
        p3.setBathrooms(1);
        p3.setAreaSqFt(550.0);
        p3.setPropertyType("Studio");
        p3.setImageUrl("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80");
        p3.setStatus(PropertyStatus.APPROVED);
        p3.setManager(manager2);
        p3.setViewsCount(89);

        Property p4 = new Property();
        p4.setTitle("Sunlit Waterfront Condo with Balcony");
        p4.setDescription("Direct oceanfront views from your private 8th-floor balcony. Includes central AC, building gym, concierge service, assigned covered parking, and direct beach access.");
        p4.setAddress("2200 Ocean Boulevard, Suite 8A");
        p4.setCity("Miami");
        p4.setState("FL");
        p4.setZipCode("33139");
        p4.setPrice(2950.0);
        p4.setSecurityDeposit(2950.0);
        p4.setBedrooms(2);
        p4.setBathrooms(2);
        p4.setAreaSqFt(1280.0);
        p4.setPropertyType("Condo");
        p4.setImageUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80");
        p4.setStatus(PropertyStatus.APPROVED);
        p4.setManager(manager2);
        p4.setViewsCount(320);

        Property p5 = new Property();
        p5.setTitle("Newly Renovated Townhouse with Rooftop Deck");
        p5.setDescription("3-story townhouse newly remodeled with marble countertops, smart thermostat, rooftop deck for entertaining, and finished basement ideal for home office or media room.");
        p5.setAddress("514 Elm Street");
        p5.setCity("Chicago");
        p5.setState("IL");
        p5.setZipCode("60614");
        p5.setPrice(2400.0);
        p5.setSecurityDeposit(2400.0);
        p5.setBedrooms(3);
        p5.setBathrooms(2);
        p5.setAreaSqFt(1800.0);
        p5.setPropertyType("Townhouse");
        p5.setImageUrl("https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80");
        p5.setStatus(PropertyStatus.PENDING_APPROVAL); // Awaiting admin approval
        p5.setManager(manager1);
        p5.setViewsCount(15);

        Property p6 = new Property();
        p6.setTitle("Executive High-Rise Penthouse");
        p6.setDescription("Exclusive top-floor residence with wrap-around balcony, private elevator access, marble bathrooms, and 24/7 doorman service. Currently under active lease.");
        p6.setAddress("100 Central Park West, Penthouse 2");
        p6.setCity("New York");
        p6.setState("NY");
        p6.setZipCode("10023");
        p6.setPrice(4900.0);
        p6.setSecurityDeposit(4900.0);
        p6.setBedrooms(3);
        p6.setBathrooms(3);
        p6.setAreaSqFt(2200.0);
        p6.setPropertyType("Apartment");
        p6.setImageUrl("https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80");
        p6.setStatus(PropertyStatus.RENTED);
        p6.setManager(manager2);
        p6.setViewsCount(450);

        propertyRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));

        // 4. Sample Rental Applications
        RentalApplication app1 = new RentalApplication();
        app1.setProperty(p1);
        app1.setTenant(tenant1);
        app1.setStatus(ApplicationStatus.APPROVED);
        app1.setMoveInDate(LocalDate.now().plusWeeks(2));
        app1.setMonthlyIncome(8500.0);
        app1.setOccupation("Senior Software Engineer");
        app1.setNotes("Looking for a 12-month lease. Excellent credit score (780).");
        applicationRepository.save(app1);

        RentalApplication app2 = new RentalApplication();
        app2.setProperty(p3);
        app2.setTenant(tenant2);
        app2.setStatus(ApplicationStatus.PENDING);
        app2.setMoveInDate(LocalDate.now().plusMonths(1));
        app2.setMonthlyIncome(5200.0);
        app2.setOccupation("Biomedical Researcher");
        app2.setNotes("Non-smoking, quiet graduate researcher starting at local research institute.");
        applicationRepository.save(app2);

        // 5. Sample Rental Agreement (for p6)
        RentalAgreement agreement1 = new RentalAgreement();
        agreement1.setProperty(p6);
        agreement1.setTenant(tenant1);
        agreement1.setManager(manager2);
        agreement1.setStartDate(LocalDate.now().minusMonths(3));
        agreement1.setEndDate(LocalDate.now().plusMonths(9));
        agreement1.setMonthlyRent(4900.0);
        agreement1.setSecurityDeposit(4900.0);
        agreement1.setTermsAndConditions("1. Rent is due on the 1st of each calendar month.\n2. Subletting is strictly prohibited without prior written consent.\n3. Routine property inspection every 6 months.\n4. Utilities included: Water, Trash, and Building Maintenance.");
        agreement1.setStatus(AgreementStatus.ACTIVE);
        agreementRepository.save(agreement1);

        // 6. Sample Messages
        Message msg1 = new Message(tenant1, manager1, p1, "Parking Spot Inquiry",
                "Hello Sarah, I loved the virtual tour of the Downtown Loft. Does the unit include an assigned parking stall in the underground garage?");
        msg1.setRead(true);

        Message msg2 = new Message(manager1, tenant1, p1, "Re: Parking Spot Inquiry",
                "Hi Alex! Yes, the apartment comes with one designated covered parking stall and access to guest parking.");
        msg2.setRead(true);

        Message msg3 = new Message(tenant2, manager2, p3, "Move-in date flexibility",
                "Hi David, is there any flexibility to move in a week earlier on the Seattle studio?");
        msg3.setRead(false);

        messageRepository.saveAll(Arrays.asList(msg1, msg2, msg3));

        // 7. Sample Notifications
        Notification notif1 = new Notification(tenant1, "Application Approved",
                "Congratulations! Your application for 'Luxury Downtown Loft' has been approved by the manager.", "/tenant/applications");
        Notification notif2 = new Notification(manager1, "New Property Listing Pending",
                "Your listing 'Newly Renovated Townhouse' has been submitted for Admin approval.", "/manager/properties");
        Notification notif3 = new Notification(tenant2, "Welcome to EstateHub",
                "Thank you for registering! Explore verified listings and apply online with ease.", "/tenant/search");

        notificationRepository.saveAll(Arrays.asList(notif1, notif2, notif3));

        // 8. Activity Logs
        ActivityLog log1 = new ActivityLog(admin, "SYSTEM_INIT", "Database initialized with sample data and test accounts.");
        ActivityLog log2 = new ActivityLog(manager1, "PROPERTY_SUBMITTED", "Submitted 'Newly Renovated Townhouse' for approval.");
        ActivityLog log3 = new ActivityLog(tenant1, "APPLICATION_SUBMITTED", "Submitted rental application for 'Luxury Downtown Loft'.");
        ActivityLog log4 = new ActivityLog(manager1, "APPLICATION_APPROVED", "Approved rental application for tenant Alex Johnson.");

        activityLogRepository.saveAll(Arrays.asList(log1, log2, log3, log4));

        System.out.println(">>> Sample data successfully initialized!");
    }
}
