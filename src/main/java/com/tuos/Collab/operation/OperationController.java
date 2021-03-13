package com.tuos.Collab.operation;

import java.util.HashMap;
import java.util.Map;

import com.tuos.Collab.document.Document;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class OperationController {


    DocumentEditService documentEditService;
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/sendcharacter")
    public void applyOperation(Map<Object, Object> payload, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        System.out.println(payload.toString());

        // TODO: FIX TYPES, SANITIZE INPUT
        // TODO: Make an object for payload so i can cleanup the next 10 lines

        char character = ((String) payload.get("text")).charAt(0); // TODO: Text batches
        int index = (int) payload.get("offset");
        long siteID = (long) payload.get("siteID"); // TODO: change to UUID
        int stateID = (int) payload.get("stateID");

        Long documentId = Long.parseLong((String)simpMessageHeaderAccessor.getSessionAttributes().get("docID"));

        String type = "";
        if (((String) payload.get("type")).equals("insert_text")) {
            type = "ins";
        } else if (((String) payload.get("type")).equals("remove_text")) {
            type = "del";
        }


        // char, position, siteid, stateid, op
        Operation op = new Operation(character, index, siteID, stateID, type);
        try {
            op = documentEditService.update(documentId, op);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            messagingTemplate.convertAndSend("/topic/1", "404");
            e.printStackTrace();
        }

        payload.put("offset", op.getPosition());
        payload.put("stateID", op.getStateId());

        messagingTemplate.convertAndSend("/topic/1", payload);
        //System.out.println(activeDocuments.get(1).getText());
        System.out.println(payload.toString());
        System.out.println("------------------------------");

    }

    @SubscribeMapping("file/{docId}")
    public ResponseEntity<?> getDocument(@DestinationVariable String docId, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        System.out.println("Document Id is: " + docId);
        simpMessageHeaderAccessor.getSessionAttributes().put("docID",docId);
        /*
         * code = server response. 404 - Document doesn't exist 403 - Document is
         * forbidden 1 - All good
         *
         *
         */
        Document d = documentEditService.getDocument(Long.parseLong(docId));

        HashMap<String, String> response = new HashMap<>();
        if(d==null) {
            return ResponseEntity.notFound().build();
        }else{
            response.put("state",String.valueOf(d.getState()));
            response.put("text",d.getText());
            return ResponseEntity.ok(response);
        }
    }
}
