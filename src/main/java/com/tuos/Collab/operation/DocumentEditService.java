package com.tuos.Collab.operation;

import com.tuos.Collab.document.Document;
import com.tuos.Collab.document.DocumentRepository;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.operation.OperationKey;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DocumentEditService {

    HashMap<Long, Document> activeDocuments;
    DocumentRepository documentRepository;

    HashMap<OperationKey, OperationKey> effectsRelation;
    ArrayList<Operation> historyBuffer;

    @Autowired
    public DocumentEditService(DocumentRepository documentRepository) {
        this.activeDocuments = new HashMap<>();
        this.documentRepository = documentRepository;
    }


    public Document getDocument(Long id) {
        Document d = activeDocuments.get(id);
        if (d == null) { // not active, check DB
            d = documentRepository.findById(id).get();
            activeDocuments.put(id, d);
        }
        return d;
    }

    public synchronized Operation update(Long docID, Operation op) throws Exception {

        Document doc = getDocument(docID);
        System.out.println(doc.getText());
        effectsRelation = doc.getEffectsRelation();
        historyBuffer = doc.getHistoryBuffer();
        StringBuilder sb = new StringBuilder(doc.getText());

        op = this.integrate(op);
        if (op.getPosition() >= 0 && op.getPosition() <= doc.getText().length()) {
            if (op.getType().equals("ins")) {
                sb.insert(op.getPosition(), op.getCharacter());
            } else {
                sb.deleteCharAt(op.getPosition());
            }
            op.setStateID(doc.getState());
            doc.incrementState();
            doc.getHistoryBuffer().add(op);
            doc.setText(sb.toString());
        }
        System.out.println(doc.getText());
        return op;
    }


    public Operation integrate(Operation op) throws Exception {
        // Normally this should be a clear split anyway but it also contextually
        // serializes if needed
        // Will have to rethink the necessity of the serial contextualization but it
        // doesn't hurt the performance much
        ArrayList<ArrayList<Operation>> transposedLists = transposePreCon(op, historyBuffer);
        ArrayList<Operation> happened = transposedLists.get(0);
        ArrayList<Operation> concurrent = transposedLists.get(1);
        Operation newOp = null;

        // If no concurrent operations, just return and dont transform
        if (concurrent.size() == 0) {
            newOp = new Operation(op);
            return newOp;
        }

        if (op.getType() == "del") {
            newOp = this.it_sq(op, concurrent);
        } else { // op.type == "ins"

            // fixed list
            ArrayList<Operation> etsos_happened = this.buildETSOS(happened);

            // insertions and deletions that have happened
            ArrayList<ArrayList<Operation>> insDelLists = this.transposeInsDel(etsos_happened);
            // ArrayList<Operation> happenedInsert = insDelLists.get(0);
            ArrayList<Operation> happenedDelete = insDelLists.get(1);

            // all deletions that have happened and concurrent operations
            ArrayList<Operation> newList = (ArrayList<Operation>) Stream
                    .concat(happenedDelete.stream(), concurrent.stream()).collect(Collectors.toList());
            ArrayList<Operation> happenedDelConc = this.buildETSOS(newList);

            // All insertions and deletions
            ArrayList<ArrayList<Operation>> insDels = this.transposeInsDel(happenedDelConc);
            ArrayList<Operation> insertions = insDels.get(0);
            ArrayList<Operation> deletions = insDels.get(1);

            Operation innerOp = this.et_sq(op, happenedDelete); // o'', this is the back backwards

            ArrayList<Operation> allInsDels = (ArrayList<Operation>) Stream
                    .concat(insertions.stream(), deletions.stream()).collect(Collectors.toList());
            newOp = this.it_sq(innerOp, allInsDels);
        }
        return newOp;
    }

    // Transform an operation o1 against o2 such that the effect of o2 is included
    public Operation inclusionTransform(Operation o1, Operation o2) {
        /*-1 = O1 is to the left of O2, so don't transform
         * 0 = Same position, used for double deletions
         * 1 = O1 is to the right of O2, transform*/
        int relationship = get_ER_IT(o1, o2);

        // Clone o1 into newOp1 so we can modify newOp1 without affecting o1
        Operation newOp1 = new Operation(o1);

        if (relationship == 0) { // Same position, double deletion
            newOp1 = new Operation(' ', -1); // position = -1 -> Don't delete, identity operation
        } else {
            if (relationship == 1) { // o2 is to the left of o1
                if (o2.getType().equals("ins")) {// ins = insertion operator
                    newOp1.shiftRight(); // position++
                } else { // o2.type = deletion
                    newOp1.shiftLeft();// position--
                }
            }
        }
        return newOp1;
    }

    private int get_ER_IT(Operation o1, Operation o2) {
        // Check if there is a mapping of o1->o2 or o2->o1
        if (effectsRelation.get(o1.getKey()) == o2.getKey()) {
            return -1;
        }
        if (effectsRelation.get(o2.getKey()) == o1.getKey()) {
            return 1;
        }

        int relationship = 1;

        // If no existing relationship, create a new one
        if (o1.getPosition() < o2.getPosition()) {
            relationship = -1; // I.e. don't transform
            effectsRelation.put(o1.getKey(), o2.getKey());
        } else if (o1.getPosition() == o2.getPosition()) {
            // If two insertions, arbitrarily choose by site id, don't transform
            if (o1.getType().equals("ins") && o2.getType().equals("ins") && o1.getSiteID() < o2.getSiteID()) {
                relationship = -1;
                effectsRelation.put(o1.getKey(), o2.getKey());
            } else if (o1.getType().equals("del") && o2.getType().equals("del")) {
                relationship = 0; // Delete only once
            } else if (o1.getType().equals("ins") && o2.getType().equals("del")) {
                relationship = -1;
                effectsRelation.put(o1.getKey(), o2.getKey());
            }
        }
        return relationship;
    }

    // Transform an operation o2 against o1 such that the effect of o1 is negated
    private Operation exclusionTransform(Operation o2, Operation o1) throws Exception {
        int relationship = get_ER_ET(o1, o2);
        Operation newOp2 = null;
        if (relationship == 0) {
            newOp2 = new Operation(' ', -1);
            throw new Exception("HALT, DOUBLE DELETION");
        } else {
            newOp2 = new Operation(o2);

            if (relationship == -1) {// o1 precedes o2 so transform o2
                if (o1.getType().equals("ins")) {
                    newOp2.shiftLeft(); // Shift index to the left by 1
                } else { // o1.type = deletion
                    newOp2.shiftRight();// Shift index to the right by 1
                }
            }
        }
        return newOp2;
    }

    private int get_ER_ET(Operation o1, Operation o2) {
        if (effectsRelation.get(o1.getKey()) == o2.getKey()) {
            return -1;
        }
        if (effectsRelation.get(o2.getKey()) == o1.getKey()) {
            return 1;
        }

        int relationship;

        if (o1.getPosition() < o2.getPosition()) {
            relationship = -1;
        } else if (o1.getPosition() > o2.getPosition()) {
            relationship = 1;
        } else { // o1.pos == o2.pos
            if (o1.getType().equals("ins") && o2.getType().equals("ins")) {
                relationship = 1;
            } else if (o1.getType().equals("del") && o2.getType().equals("del")) {
                relationship = -1;
            } else if (o1.getType().equals("del") && o2.getType().equals("ins")) {
                relationship = 1;
            } else { // o1.type = ins, o2.type = del
                relationship = 0;
            }
        }

        // record in ER
        switch (relationship) {
            case -1:
                effectsRelation.put(o1.getKey(), o2.getKey());
                break;
            case 1:
                effectsRelation.put(o2.getKey(), o1.getKey());
                break;
        }

        return relationship;
    }

    /*
     * Precondition: Sequence must be IT-safe (i.e. all insertion operations should
     * be before deletion operations)
     *
     */
    private Operation it_sq(Operation op, ArrayList<Operation> sequence) {
        Operation newOp = new Operation(op); // Copy the object
        for (int i = 0; i < sequence.size(); i++) {
            newOp = inclusionTransform(newOp, sequence.get(i));
            if (newOp.getPosition() < 0) {
                break;
            }
        }
        return newOp;
    }

    /*
     * Precondition: Sequence must be ET-safe (i.e. for any two i,j where i<j,
     * either pos(i)<pos(j) or pos(i)=pos(j) but j deletes i's insertion
     */
    private Operation et_sq(Operation op, ArrayList<Operation> sequence) throws Exception {
        Operation newOp = new Operation(op); // Copy the object
        int i = 0;

        // Since our array is sorted by Effects Relation, this first for-loop removes
        // all characters
        // which do not affect op because they are on the right of it. Possible
        // optimisation by not calling ET?
        // Double check in get_er_et?
        for (i = sequence.size() - 1; i >= 0; i--) {
            newOp = this.exclusionTransform(newOp, sequence.get(i));
            if (get_ER_ET(sequence.get(i), newOp) == -1) {
                break;
            }
        }

        // The remaining elements from the last array do affect op
        for (int j = i - 1; j >= 0; j--) {
            if (sequence.get(j).getType().equals("ins")) {
                newOp.shiftLeft();
            } else {
                newOp.shiftRight();
            }
        }
        return newOp;
    }

    // This might be dumb, but i think it's identical??
    private Operation fast_et_sq(Operation op, ArrayList<Operation> sequence) throws Exception {
        Operation newOp = new Operation(op); // Copy the object

        // Possible optimisation by not calling ET?
        // Double check in get_er_et?
        for (int i = sequence.size() - 1; i >= 0; i--) {
            newOp = this.exclusionTransform(newOp, sequence.get(i));
        }

        return newOp;
    }

    // return <op1', op2'>
    private ArrayList<Operation> transpose(Operation o2, Operation o1) throws Exception {
        ArrayList<Operation> transposedOperations = new ArrayList<Operation>();

        if (this.get_ER_ET(o1, o2) == 0) {// Same character, don't transpose, return as is
            transposedOperations.add(0, o1);
            transposedOperations.add(1, o2);
        } else {
            transposedOperations.add(this.exclusionTransform(o1, o2));
            transposedOperations.add(this.inclusionTransform(o2, transposedOperations.get(0)));
        }

        return transposedOperations;
    }

    // Returns a map entry where the key is the transposed operation while the value
    // is the transposed sequence
    private Map.Entry<Operation, ArrayList<Operation>> transposeOSq(ArrayList<Operation> sq, Operation op)
            throws Exception {

        Operation newOp = new Operation(op); // Clone op into newOp
        ArrayList<Operation> newSq = new ArrayList<Operation>();

        // Clone the sequence
        for (Operation operation : sq) {
            newSq.add(new Operation(operation));
        }

        ArrayList<Operation> transposedElements;
        for (int i = sq.size() - 1; i >= 0; i--) {
            transposedElements = transpose(newSq.get(i), newOp);
            newOp = transposedElements.get(0);
            newSq.set(i, transposedElements.get(1));
        }

        Map.Entry<Operation, ArrayList<Operation>> response = new AbstractMap.SimpleEntry<Operation, ArrayList<Operation>>(
                newOp, newSq);
        return response;
    }

    // Given an operation o and a sequence sq, returns the list of all operations
    // that happened before o and the list of all that happened
    // concurrently with o from sq. Currently looks at stateID, might have to
    // revisit TODO
    private ArrayList<ArrayList<Operation>> transposePreCon(Operation op, ArrayList<Operation> sq) throws Exception {
        ArrayList<Operation> happened = new ArrayList<>();
        ArrayList<Operation> concurrent = new ArrayList<>();
        ArrayList<ArrayList<Operation>> sequences = new ArrayList<>();
        for (int i = 0; i < sq.size(); i++) {
            if (sq.get(i).getStateId() >= op.getStateId()) {
                concurrent.add(sq.get(i));
            } else {
                Map.Entry<Operation, ArrayList<Operation>> response = transposeOSq(concurrent, sq.get(i));
                happened.add(response.getKey());
            }
        }
        sequences.add(happened);
        sequences.add(concurrent);
        return sequences;
    }

    // Given a sequence sq of insertions and deletions, returns a list of insertions
    // and a list of deletions.
    // These lists are transposed so that the effects of the initial sequence sq and
    // the effect of sqi+sqd is the same
    // Currently looks at stateID, might have to revisit TODO
    private ArrayList<ArrayList<Operation>> transposeInsDel(ArrayList<Operation> sq) throws Exception {
        ArrayList<Operation> insertions = new ArrayList<>();
        ArrayList<Operation> deletions = new ArrayList<>();
        ArrayList<ArrayList<Operation>> sequences = new ArrayList<>();
        for (int i = 0; i < sq.size(); i++) {
            if (sq.get(i).getType().equals("del")) {
                deletions.add(sq.get(i));
            } else {
                Map.Entry<Operation, ArrayList<Operation>> response = transposeOSq(deletions, sq.get(i));
                insertions.add(response.getKey());
            }
        }
        sequences.add(insertions);
        sequences.add(deletions);
        return sequences;
    }

    // Basically builds a list that is sorted by the index of operations (effects
    // relation)
    private ArrayList<Operation> buildETSOS(ArrayList<Operation> sq) throws Exception {
        if (sq.size() < 1) {
            return sq;
        }
        ArrayList<Operation> newSq = new ArrayList<Operation>();
        Operation op = null;
        boolean flag = true;

        newSq.add(sq.get(0));
        for (int i = 1; i < sq.size(); i++) {
            op = new Operation(sq.get(i));
            flag = false;
            for (int j = newSq.size() - 1; j >= 0; j--) {
                if (flag) {
                    this.effectsRelation.put(newSq.get(j).getKey(), op.getKey());
                } else {
                    //I'M ADDING A == 0 HERE EVEN THOUGH THE ALGORITHM DOESN'T SAY SO. I think it's right.

                    if (this.get_ER_ET(newSq.get(j), op) < 1) { //(newsq[j]<op)||(newsq[j] = op but ins+del)
                        //so if the op is in its place, execute this. Otherwise go to else and transpose


                        // could use some testing but haven't had issues so far
                        ArrayList<Operation> temp = new ArrayList<Operation>();
                        for (int k = 0; k <= j; k++) {
                            temp.add(newSq.get(k));
                        }
                        temp.add(op);
                        for (int k = j + 1; k <= newSq.size() - 1; k++) {
                            temp.add(newSq.get(k));
                        }
                        newSq = temp;


                        flag = true;
                    } else {
                        ArrayList<Operation> transposed = this.transpose(newSq.get(j), op);
                        op = transposed.get(0);
                        newSq.set(j, transposed.get(1));
                    }
                }
            }

            if (!flag) {
                newSq.add(0, op);
            }
        }
        return newSq;
    }
}
