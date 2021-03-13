package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path="api/doc")
public class DocumentController {

    private DocumentService documentService;

    public String create(@RequestBody Map<String, String> request){
        String name = request.get("name");
        String text = request.get("text");
        String email = request.get("email");
        Document document = new Document(name, text);
        return documentService.createDocument(document, email);
    }

    @GetMapping(path="/api/doc/{documentID}")
    public Document getDocument(@PathVariable Long documentID){
        return documentService.getDocument(documentID);
    }

    @GetMapping
    public List<DocumentDTO> getAllDocuments(Authentication authentication, Principal principal){
        return documentService.getAllDocuments(principal.getName());
    }
}
