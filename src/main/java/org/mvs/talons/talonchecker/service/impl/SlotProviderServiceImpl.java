package org.mvs.talons.talonchecker.service.impl;

import org.mvs.talons.talonchecker.dto.SlotRequest;
import org.mvs.talons.talonchecker.dto.SlotResponse;
import org.mvs.talons.talonchecker.entity.SlotGroup;
import org.mvs.talons.talonchecker.mapper.SlotMapper;
import org.mvs.talons.talonchecker.repository.SlotProviderRepository;
import org.mvs.talons.talonchecker.service.SlotProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlotProviderServiceImpl implements SlotProviderService {

    private final SlotProviderRepository slotProviderRepository;
    private final SlotMapper slotMapper;

    @Autowired
    public SlotProviderServiceImpl(SlotProviderRepository slotProviderRepository, SlotMapper slotMapper) {
        this.slotProviderRepository = slotProviderRepository;
        this.slotMapper = slotMapper;
    }

    @Override
    public SlotResponse findAvailable(SlotRequest slotRequest) {
        SlotGroup slotGroup = slotProviderRepository.findById(slotMapper.toEntity(slotRequest).getId()).orElseThrow(
                RuntimeException::new
        );
        return slotMapper.fromEntity(slotGroup);
    }
}
