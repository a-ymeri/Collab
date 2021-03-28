package com.tuos.Collab.collabuser;

import com.tuos.Collab.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tuos.Collab.collabuser.CollabUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface CollabUserRepository extends JpaRepository<CollabUser, Long> {
    Optional<CollabUser> findByEmail(String email);

    @Modifying
    @Query("update CollabUser c set c.name = :name WHERE c.email = :email")
    void changeUsername(String name, String email);

    @Query("SELECT c.email from CollabUser c join c.editableDocuments e where e.id= :id")
    List<String> findAllEditors(@Param("id") Long id);
}
