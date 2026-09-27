package org.mvs.talons.talonchecker.repository;

import org.mvs.talons.talonchecker.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
}
