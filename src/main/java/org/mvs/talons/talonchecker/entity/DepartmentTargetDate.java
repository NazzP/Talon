package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DepartmentTargetDate implements Serializable {
    private Integer departmentId;
    private LocalDate targetDate;
}
