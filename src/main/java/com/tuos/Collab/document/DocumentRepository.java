package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;


@Repository
@Transactional()
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT new com.tuos.Collab.document.DocumentDTO (d.id, d.name, d.author.name, d.lastModified) from Document d where d.author.id = :id")
    List<DocumentDTO> findByAuthorId(@Param("id") Long id);

    @Query("SELECT new com.tuos.Collab.document.DocumentDTO (d.id, d.name, d.author.name, d.lastModified) from Document d join d.editors e where e.id= :editorId")
    List<DocumentDTO> findByEditors_Id(@Param("editorId") Long id);


    @Query("DELETE FROM Document d where d.id =:id")
    @Modifying
    void deleteById(@Param("id") Long id); //TODO: Access Control
}