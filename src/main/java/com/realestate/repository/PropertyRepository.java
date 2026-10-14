package com.realestate.repository;

import com.realestate.model.Property;
import com.realestate.model.PropertyStatus;
import com.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByStatusOrderByCreatedAtDesc(PropertyStatus status);

    List<Property> findByManagerOrderByCreatedAtDesc(User manager);

    List<Property> findByManagerAndStatus(User manager, PropertyStatus status);

    long countByStatus(PropertyStatus status);

    long countByManager(User manager);

    @Query("SELECT p FROM Property p WHERE p.status = 'APPROVED' " +
           "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:propertyType IS NULL OR :propertyType = '' OR p.propertyType = :propertyType) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:bedrooms IS NULL OR p.bedrooms >= :bedrooms)")
    List<Property> searchProperties(
            @Param("keyword") String keyword,
            @Param("propertyType") String propertyType,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("bedrooms") Integer bedrooms);
}
