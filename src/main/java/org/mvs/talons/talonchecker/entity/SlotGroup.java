package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotGroup {

    @EmbeddedId
    private SlotGroupId id;

    @OneToMany(mappedBy = "slotGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlotDate> slots;

    public SlotGroup(int serviceId, int departmentId, List<SlotDate> slots) {
        this.id = new SlotGroupId(serviceId, departmentId);
        this.slots = slots;
        if (slots != null) slots.forEach(s -> s.setSlotGroup(this));
    }
}

