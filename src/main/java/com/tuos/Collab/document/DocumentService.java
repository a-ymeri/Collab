package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.collabuser.CollabUserRepository;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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


    public String createDocument(Document document, String email){
        //TODO: Revisit for security/safety
        CollabUser collabUser = collabUserRepository.findByEmail(email).get();
        document.setAuthor(collabUser);
        documentRepository.save(document);
        return "document saved";
    }

    public Document getDocument(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        return document.get();
    }

    public List<DocumentDTO> getAllDocuments(String email) {
        List<DocumentDTO> documents = documentRepository.findByAuthorEmail(email);
        return documents;
    }


//    private class DocumentResponse{
//        Long id;
//        String name;
//        String author;
//
//        public DocumentResponse()
//    }
}
