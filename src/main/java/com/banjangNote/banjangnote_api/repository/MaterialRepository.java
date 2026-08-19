package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByNameAndUnitPrice(String name, Integer unitPrice);
}
