package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

interface MetaData{
    String getName();
    Long getId();
    String getAuthorName();
}
@Repository
@Transactional(readOnly=true)
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT new com.tuos.Collab.document.DocumentDTO (d.id, d.name, d.author.name) from Document d where d.author.id = :id")
    List<DocumentDTO> findByAuthorId(@Param("id") Long id);

    @Query("SELECT new com.tuos.Collab.document.DocumentDTO (d.id, d.name, d.author.name) from Document d where d.author.email = :email")
    List<DocumentDTO> findByAuthorEmail(@Param("email") String email);
}