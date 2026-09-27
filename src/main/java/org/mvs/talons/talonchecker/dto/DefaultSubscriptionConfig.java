package org.mvs.talons.talonchecker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultSubscriptionConfig {
    private Set<Integer> departmentIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private java.util.Map<Integer, Set<LocalDate>> targetDatesPerDepartment;

    public DefaultSubscriptionConfig(Set<Integer> departmentIds, LocalDate startDate, LocalDate endDate) {
        this.departmentIds = departmentIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetDatesPerDepartment = new java.util.HashMap<>();
    }
}
