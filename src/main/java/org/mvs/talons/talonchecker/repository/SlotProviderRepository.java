package org.mvs.talons.talonchecker.repository;

import org.mvs.talons.talonchecker.entity.SlotGroup;
import org.mvs.talons.talonchecker.entity.SlotGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotProviderRepository extends JpaRepository<SlotGroup, SlotGroupId> {
}
