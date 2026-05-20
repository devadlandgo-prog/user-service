package com.landgo.userservice.service;

import com.landgo.userservice.entity.Expertise;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.repository.ExpertiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpertiseService {

    private final ExpertiseRepository expertiseRepository;

    @Transactional(readOnly = true)
    public List<Expertise> getAllExpertise(boolean activeOnly) {
        return activeOnly ? expertiseRepository.findByActiveTrue() : expertiseRepository.findAll();
    }

    @Transactional
    public Expertise createExpertise(String name, String description) {
        if (expertiseRepository.existsByName(name)) {
            throw new BadRequestException("Expertise with name '" + name + "' already exists");
        }
        Expertise expertise = Expertise.builder()
                .name(name)
                .description(description)
                .active(true)
                .build();
        return expertiseRepository.save(expertise);
    }

    @Transactional
    public Expertise updateExpertise(UUID id, String name, String description, Boolean active) {
        Expertise expertise = expertiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expertise not found"));
        
        if (name != null) {
            if (!expertise.getName().equals(name) && expertiseRepository.existsByName(name)) {
                throw new BadRequestException("Expertise with name '" + name + "' already exists");
            }
            expertise.setName(name);
        }
        if (description != null) expertise.setDescription(description);
        if (active != null) expertise.setActive(active);
        
        return expertiseRepository.save(expertise);
    }

    @Transactional
    public void deleteExpertise(UUID id) {
        if (!expertiseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expertise not found");
        }
        expertiseRepository.deleteById(id);
    }

    @Transactional
    public void deleteExpertiseByName(String name) {
        Expertise expertise = expertiseRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Expertise not found with name: " + name));
        expertiseRepository.delete(expertise);
    }
}
