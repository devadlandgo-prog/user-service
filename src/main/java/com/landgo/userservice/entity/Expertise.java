package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expertise_options", schema = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Expertise extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;
}
