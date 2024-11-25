package com.poc.jpa.entity_manager.repository;

import com.poc.jpa.entity_manager.entity.SomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SomeEntityRepository extends JpaRepository<SomeEntity, Long> {
    List<SomeEntity> findByNameContainingIgnoreCase(String name);
}