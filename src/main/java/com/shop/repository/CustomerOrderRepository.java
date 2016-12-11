package com.shop.repository;

import com.shop.domain.CustomerOrder;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the CustomerOrder entity.
 */
@SuppressWarnings("unused")
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder,Long> {

}
