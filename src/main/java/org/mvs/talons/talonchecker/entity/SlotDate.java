package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "service_id", referencedColumnName = "serviceId"),
            @JoinColumn(name = "department_id", referencedColumnName = "departmentId")
    })
    private SlotGroup slotGroup;

    public SlotDate(LocalDateTime date) {
        this.date = date;
    }
}

