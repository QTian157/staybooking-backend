package com.tq.staybooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tq.staybooking.model.Authority;
import org.springframework.stereotype.Repository;

/**
 * Similar to UserRepository,
 * we can create another interface called AuthorityRepository to handle database operations for authority management.
 * Again, we don’t need to create a real implementation of this interface,
 * as Spring will help with that.
 *
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
