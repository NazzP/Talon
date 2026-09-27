package org.mvs.talons.talonchecker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SlotRequest {
    private int serviceId;
    private int departmentId;
}
