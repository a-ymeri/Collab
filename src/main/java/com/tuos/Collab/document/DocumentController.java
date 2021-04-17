package com.tuos.Collab.document;

import com.tuos.Collab.collabuser.CollabUser;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class DocumentController {

    private DocumentService documentService;


    @PostMapping("/api/doc")
    public ResponseEntity<DocumentDTO> create(@RequestBody HashMap<String, String> request, Principal principal){
        String docName = request.get("docId");
        Document document = new Document(docName);

        DocumentDTO doc = null;
        try{
            doc = documentService.createDocument(document, principal.getName());
        }catch(Exception e){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(doc);
    }

//    @GetMapping(path="/api/doc/{documentID}")
//    public Document getDocument(@PathVariable Long documentID){
//        return documentService.getDocument(documentID);
//    }

    @GetMapping("api/doc")
    public List<DocumentDTO> getAllDocuments(Principal principal){
        return documentService.getAllDocuments(principal.getName());
    }

    @DeleteMapping(path = "/api/doc/delete/{id}")
    public ResponseEntity delete(@PathVariable Long id){

        try{
            documentService.delete(id);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
        return ResponseEntity.ok("Document deleted successfully!");
    }

    @PostMapping(path="/api/doc/editor")
    public ResponseEntity addEditor(@RequestBody UserPermissionRequest request){
        String email = request.getEmail();
        Long docId = request.getDocId();
        try{
            documentService.addEditor(email, docId);
        }catch(Exception e){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Editor added successfully");
    }
}
