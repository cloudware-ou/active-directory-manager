package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OneTimeKeysService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long saveOneTimeKeys(String alicePublicKey) {
        OneTimeKeys oneTimeKeys = new OneTimeKeys();
        oneTimeKeys.setAlicePublicKey(alicePublicKey);
        entityManager.persist(oneTimeKeys);
        entityManager.flush();  // Ensure the entity is saved immediately
        return oneTimeKeys.getId();
    }

    @Transactional(readOnly = true)
    public OneTimeKeys getOneTimeKeys(Long id) {
        OneTimeKeys entity = entityManager.find(OneTimeKeys.class, id);
        entityManager.refresh(entity);  // Refresh to get latest data
        return entity;
    }
}
