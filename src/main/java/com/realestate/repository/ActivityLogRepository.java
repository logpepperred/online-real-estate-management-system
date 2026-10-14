package com.realestate.repository;

import com.realestate.model.ActivityLog;
import com.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop20ByOrderByTimestampDesc();

    List<ActivityLog> findAllByOrderByTimestampDesc();

    List<ActivityLog> findByUserOrderByTimestampDesc(User user);
}
