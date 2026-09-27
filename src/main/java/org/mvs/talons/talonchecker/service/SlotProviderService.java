package org.mvs.talons.talonchecker.service;

import org.mvs.talons.talonchecker.dto.SlotRequest;
import org.mvs.talons.talonchecker.dto.SlotResponse;

import java.util.List;

public interface SlotProviderService {
    SlotResponse findAvailable(SlotRequest slotRequest);
}
