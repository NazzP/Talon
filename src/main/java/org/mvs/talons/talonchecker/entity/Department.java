package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    private Integer id;
    private String name;
    private String region;
    private String district;
    private String city;
    private String street;
    private String building;
    private Integer allowOnlineCount;
}
