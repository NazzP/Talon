package org.mvs.talons.talonchecker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class SlotResponse {
    private int serviceId;
    private int departmentId;
    private List<LocalDateTime> dateTimes;
}
