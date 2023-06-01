package com.pmso.product_management_system_original.dataaccess.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    private String slug;

    @Column(name = "date_creation")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateCreation;

    @Column(name = "date_modification")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateModification;

    private Boolean supprimer;

    public Category(Long category_id) {
        this.id = category_id;
    }
}
