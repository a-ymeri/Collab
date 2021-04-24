package com.tuos.Collab.operation;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.collabuser.CollabUserRepository;
import com.tuos.Collab.document.Document;
import com.tuos.Collab.document.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class DocumentEditController {

    //DocumentRepository documentRepository;
    CollabUserRepository collabUserRepository;
    DocumentEditService documentEditService;
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/sendcharacter")
    public void applyOperation(OperationDTO payload, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        System.out.println(payload.toString());

        // TODO: FIX TYPES, SANITIZE INPUT
        // TODO: Make an object for payload so i can cleanup the next 10 lines
        Long documentId = Long.parseLong((String) simpMessageHeaderAccessor.getSessionAttributes().get("docID"));

        // char, position, siteid, stateid, op
        Operation op = payload.toOperation();

        try {
            op = documentEditService.update(documentId, op);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            messagingTemplate.convertAndSend("/topic/1", "404");
            e.printStackTrace();
        }

        payload.setOffset(op.getPosition());
        payload.setStateID(op.getStateId());

        messagingTemplate.convertAndSend("/topic/"+documentId, payload);
        System.out.println(payload.toString());
        System.out.println("------------------------------");
    }

    @MessageMapping("/sendcopy")
    public void sendOperation(String payload) {
        System.out.println(payload.toString());
//
//        try {
//            op = documentEditService.update(documentId, op);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            messagingTemplate.convertAndSend("/topic/1", "404");
//            e.printStackTrace();
//        }

        messagingTemplate.convertAndSend("/topic/2", payload);
    }

    @MessageMapping("/changestyle")
    public void applyStyleOperation(StyleOperationDTO payload, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        Long documentId = Long.parseLong((String) simpMessageHeaderAccessor.getSessionAttributes().get("docID"));
        StyleOperationDTO temp = null;
        try {
            temp = documentEditService.addTag(documentId, payload);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/"+documentId, "404");
            e.printStackTrace();
        }

        payload.setOffset(temp.getOffset());
        payload.setEndOffset(temp.getEndOffset());
        messagingTemplate.convertAndSend("/topic/"+documentId, payload);
    }

    @SubscribeMapping("file/{docId}")
    public ResponseEntity<?> getDocument(@DestinationVariable String docId, SimpMessageHeaderAccessor simpMessageHeaderAccessor, Principal p) {
        System.out.println("Document Id is: " + docId);
        simpMessageHeaderAccessor.getSessionAttributes().put("docID", docId);

        //TODO: Possible DB optimization with the query (we're looking for editors, and if we find it then we again look for the file)
        List<String> users = collabUserRepository.findAllEditors(Long.parseLong(docId));

        //If user doesn't have access to the document
        if (!users.contains(p.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this document");
        }

        Document d = documentEditService.getDocument(Long.parseLong(docId));

        HashMap<String, Object> response = new HashMap<>();
        if (d == null) {
            return ResponseEntity.notFound().build();
        } else {
            response.put("state", String.valueOf(d.getState()));
            String text = "";
            for(String node : d.getTextArray()){
                text+= node;
            }
            response.put("text", text);
            response.put("tree", d.getStyleTree());
            return ResponseEntity.ok(response);
        }
    }
}
