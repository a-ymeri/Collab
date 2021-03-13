package com.tuos.Collab.collabuser;

import com.tuos.Collab.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly=true)
public interface CollabUserRepository extends JpaRepository<CollabUser, Long> {
    Optional<CollabUser> findByEmail(String email);
}
