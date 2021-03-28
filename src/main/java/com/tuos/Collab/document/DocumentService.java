package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.collabuser.CollabUserRepository;
import com.tuos.Collab.operation.DocumentEditService;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CollabUserRepository collabUserRepository;
    private final DocumentEditService documentEditService;


    public DocumentDTO createDocument(Document document, String email){
        //TODO: Revisit for security/safety
        CollabUser collabUser = collabUserRepository.findByEmail(email).get();
        document.setAuthor(collabUser);

        //TODO: Maybe add some form of message if relationship already exists
        collabUser.addEditableDocument(document);

        document = documentRepository.save(document);
        collabUserRepository.save(collabUser);

        DocumentDTO doc = new DocumentDTO(document.getId(), document.getName(), document.author.getName(), document.getLastModified());
        return doc;
    }

//    public Document getDocument(Long id) {
//        Optional<Document> document = documentRepository.findById(id);
//        return document.get();
//    }

    public List<DocumentDTO> getAllDocuments(String email) {
        CollabUser collabUser = collabUserRepository.findByEmail(email).get();
        List<DocumentDTO> documents = documentRepository.findByEditors_Id(collabUser.getId());
        return documents;
    }



    public void delete(Long id) {
        documentEditService.deleteActiveDocument(id);
        documentRepository.deleteById(id);
    }

    public void addEditor(String email, Long docId) {
        CollabUser collabUser = collabUserRepository.findByEmail(email).get();
        Document document = documentRepository.findById(docId).get();

        //TODO: Maybe add some form of message if relationship already exists
        collabUser.addEditableDocument(document);

        collabUserRepository.save(collabUser);
        //collabUserRepository.save(collabUser);
    }


//    private class DocumentResponse{
//        Long id;
//        String name;
//        String author;
//
//        public DocumentResponse()
//    }
}
