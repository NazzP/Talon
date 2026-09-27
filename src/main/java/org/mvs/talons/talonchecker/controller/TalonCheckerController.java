package org.mvs.talons.talonchecker.controller;

import org.mvs.talons.talonchecker.dto.SlotRequest;
import org.mvs.talons.talonchecker.dto.SlotResponse;
import org.mvs.talons.talonchecker.service.SlotProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("api/v1/talon")
public class TalonCheckerController {
    private final SlotProviderService slotProviderService;

    @Autowired
    public TalonCheckerController(SlotProviderService slotProviderService) {
        this.slotProviderService = slotProviderService;
    }

    public ResponseEntity<SlotResponse> getAvailableSlots(@RequestBody SlotRequest request){
        return new ResponseEntity<>(slotProviderService.findAvailable(request), HttpStatus.OK);
    }
}
