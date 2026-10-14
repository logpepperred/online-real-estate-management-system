package com.realestate.service;

import com.realestate.model.ActivityLog;
import com.realestate.model.User;
import com.realestate.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(User user, String action, String details) {
        ActivityLog log = new ActivityLog(user, action, details);
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getRecentActivities() {
        return activityLogRepository.findTop20ByOrderByTimestampDesc();
    }

    public List<ActivityLog> getAllActivities() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }
}
