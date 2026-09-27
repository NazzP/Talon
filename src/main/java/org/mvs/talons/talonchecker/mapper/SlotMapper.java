package org.mvs.talons.talonchecker.mapper;

import org.mvs.talons.talonchecker.dto.SlotRequest;
import org.mvs.talons.talonchecker.dto.SlotResponse;
import org.mvs.talons.talonchecker.entity.SlotDate;
import org.mvs.talons.talonchecker.entity.SlotGroup;
import org.mvs.talons.talonchecker.entity.SlotGroupId;
import org.springframework.stereotype.Component;

@Component
public class SlotMapper {

    public SlotGroup toEntity(SlotRequest slotRequest) {
        return SlotGroup.builder()
                .id(new SlotGroupId(slotRequest.getServiceId(), slotRequest.getDepartmentId()))
                .build();
    }

    public SlotResponse fromEntity(SlotGroup slotGroup) {
        return SlotResponse.builder()
                .serviceId(slotGroup.getId().getServiceId())
                .departmentId(slotGroup.getId().getDepartmentId())
                .dateTimes(slotGroup.getSlots()
                        .stream()
                        .map(SlotDate::getDate)
                        .toList()
                )
                .build();
    }
}
