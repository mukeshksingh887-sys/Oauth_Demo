package com.OAuth.demo.repository;

import com.OAuth.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

//@Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerSubject = :providerSubject")
    // this query build intrnally by hibernate

    Optional<User> findByProviderAndProviderSubject(String provider,
                                                    String providerSubject);

}
