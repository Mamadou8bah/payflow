package com.mamadou.sutura.user.repository;

import com.mamadou.sutura.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    org.springframework.data.domain.Page<User> findByRole(com.mamadou.sutura.user.enums.Role role, org.springframework.data.domain.Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findForKycUpdate(@Param("id") Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("select u from User u where replace(replace(replace(replace(replace(u.phoneNumber, ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') in :phones")
    java.util.List<User> findTransferRecipientsByPhone(@Param("phones") java.util.List<String> phones);
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    default Optional<User> findByLoginIdentifier(String identifier) {
        return findByEmailOrPhoneNumber(identifier, identifier);
    }
}
