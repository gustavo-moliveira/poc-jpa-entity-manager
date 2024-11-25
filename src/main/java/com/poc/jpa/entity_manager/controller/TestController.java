package com.poc.jpa.entity_manager.controller;

import com.poc.jpa.entity_manager.entity.SomeEntity;
import com.poc.jpa.entity_manager.repository.SomeEntityRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private SomeEntityRepository someEntityRepository;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/jpa/simple-read")
    public List<SomeEntity> simpleReadJpa() {
        return someEntityRepository.findAll();
    }

    @GetMapping("/entitymanager/simple-read")
    public List<SomeEntity> simpleReadEntityManager() {
        return entityManager.createQuery("SELECT e FROM SomeEntity e", SomeEntity.class).getResultList();
    }

    @GetMapping("/jpa/complex-query")
    public List<SomeEntity> complexQueryJpa(@RequestParam String name) {
        return someEntityRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/entitymanager/complex-query")
    public List<SomeEntity> complexQueryEntityManager(@RequestParam String name) {
        return entityManager.createQuery("SELECT e FROM SomeEntity e WHERE LOWER(e.name) LIKE LOWER(:name)", SomeEntity.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    @PostMapping("/jpa/batch-insert")
    public ResponseEntity<?> batchInsertJpa(@RequestBody List<SomeEntity> entities) {
        someEntityRepository.saveAll(entities);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/entitymanager/batch-insert")
    public ResponseEntity<?> batchInsertEntityManager(@RequestBody List<SomeEntity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));
            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return ResponseEntity.ok().build();
    }
}
