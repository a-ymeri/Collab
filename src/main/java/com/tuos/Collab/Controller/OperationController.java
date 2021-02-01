package com.tuos.Collab.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.tuos.Collab.Model.*;

@Controller
public class OperationController {
	HashMap<Integer, Document> activeDocuments = new HashMap<Integer, Document>();

	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	public OperationController() {
		activeDocuments.put(1, new Document("123456789"));
	}

	@MessageMapping("/sendcharacter")
	public void applyOperation(Map<Object, Object> payload) {
		System.out.println(payload.toString());

		// TODO: FIX TYPES, SANITIZE INPUT

		char character = ((String) payload.get("text")).charAt(0); // TODO: Text batches
		int index = (int) payload.get("offset");
		long siteID = (long) payload.get("siteID"); // TODO: change to UUID
		int stateID = (int) payload.get("stateID");
		String type = ((String) payload.get("type")).substring(0, 3); // TODO: other operations

		// char, position, siteid, stateid, op
		Operation op = new Operation(character, index, siteID, stateID, type);
		// TODO: assign user ID and document on headers??? spring help. Also
		// exceptions!!
		try {
			op = activeDocuments.get(1).update(op);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		payload.put("offset", op.getPosition());
		payload.put("stateID", op.getStateId());
		
		messagingTemplate.convertAndSend("/topic/1", payload);
		System.out.println(activeDocuments.get(1).getText());
		System.out.println(payload.toString());
		System.out.println("------------------------------");
	}

	@SubscribeMapping("file/{docId}/getDocument")
	public HashMap<String, String> getDocument(@DestinationVariable String docId) {
		System.out.println("Document Id is: " + docId);

		/*
		 * code = server response. 404 - Document doesn't exist 403 - Document is
		 * forbidden 1 - All good
		 * 
		 * 
		 */
		HashMap<String, String> response = new HashMap<String, String>();
		try {
			Document doc = activeDocuments.get(Integer.parseInt(docId));
			doc.resetHB();
			if (doc == null) {

				// If found on DB, add to list

				// If not found, return 404

				// If found but no access, return 403
			} else {
				response.put("response", "1");
				response.put("text", doc.getText());
				response.put("state", Long.toString(doc.getState()));
			}
		} catch (NumberFormatException e) {// ID is invalid, can't parse
			e.printStackTrace();
			response.put("response", "404");
			response.put("text", "");
		}
		return response;
	}

}
