package com.tuos.Collab.Model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class Document {
	String text;
	ArrayList<Operation> historyBuffer;
	HashMap<OperationKey, OperationKey> effectsRelation;

	public Document(String text) {
		this.text = text;
		effectsRelation = new HashMap<OperationKey, OperationKey>();
	}

	public void update(Operation op) {
		StringBuilder sb = new StringBuilder(text);
		if (op.getType().equals("ins")) {
			sb.insert(op.getPosition(), op.getCharacter());
		} else {
			sb.deleteCharAt(op.getPosition());
		}
		text = sb.toString();
	}

	public String getText() {
		return text;
	}

	// Transform an operation o1 against o2 such that the effect of o2 is included
	private Operation inclusionTransform(Operation o1, Operation o2) {
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
					newOp1.shiftRight();// position--
				}
			}
		}
		return newOp1;
	}

	private int get_ER_IT(Operation o1, Operation o2) {
		// Check if there is a mapping of o1->o2 or o2->o1
		if (effectsRelation.get(o1.getKey()) != null) {
			return -1;
		}
		if (effectsRelation.get(o2.getKey()) != null) {
			return 1;
		}

		int relationship = 1;

		// If no existing relationship, create a new one
		if (o1.getPosition() < o2.getPosition()) {
			relationship = -1; // I.e. don't transform
			effectsRelation.put(o1.getKey(), o2.getKey());
		} else if (o1.getPosition() == o2.getPosition()) {
			// If two insertions, arbitrarily choose by site id, don't transform
			if (o1.getType().equals("ins") && o2.getType().equals("ins") && o1.getSiteId() < o2.getSiteId()) {
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
			throw new Exception("HALT, DOUBLE DELETION");
		} else {
			newOp2 = new Operation(o2);

			if (relationship < -1) {// o1 precedes o2 so transform o2
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
		if (effectsRelation.get(o1.getKey()) != null) {
			return -1;
		}
		if (effectsRelation.get(o2.getKey()) != null) {
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

	public static void main(String[] args) {
		Document d = new Document("ardit");
		Document d2 = new Document("ardit");

		Operation op1 = new Operation('c', 0, 0, "ins"); // cardit
		Operation op2 = new Operation(0, 1, "del"); // adit

		System.out.println(d.getText());
		d.update(op1);
		d.update(d.inclusionTransform(op2, op1));
		System.out.println(d.getText());

		System.out.println("------------------");

		System.out.println(d2.getText());
		d2.update(op1);
		d2.update(op2);
		System.out.println(d2.getText());

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
			if (newOp.getCharacter() == ' ') {
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
		for (int j = i - 1; j >= 0; i--) {
			if (sequence.get(j).getType().equals("ins")) {
				newOp.shiftLeft();
			} else {
				newOp.shiftRight();
			}
		}
		return newOp;
	}

	// This might be retarded, but i think it's identical??
	private Operation fast_et_sq(Operation op, ArrayList<Operation> sequence) throws Exception {
		Operation newOp = new Operation(op); // Copy the object

		// Possible optimisation by not calling ET?
		// Double check in get_er_et?
		for (int i = sequence.size() - 1; i >= 0; i--) {
			newOp = this.exclusionTransform(newOp, sequence.get(i));
		}

		return newOp;
	}

	//return <op1', op2'>
	private ArrayList<Operation> transpose(Operation o2, Operation o1) throws Exception {
		ArrayList<Operation> transposedOperations = new ArrayList<Operation>();

		if (this.get_ER_ET(o1, o2) == 0) {// Same character, don't transpose, return as is
			transposedOperations.add(0, o1);
			transposedOperations.add(1, o2);
		} else {
			transposedOperations.add(this.exclusionTransform(o1, o2));
			transposedOperations.add(this.inclusionTransform(o2, o1));
		}

		return transposedOperations;
	}

	// Returns a map entry where the key is the transposed operation while the valye
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
	// after o from sq. Currently looks at stateID, might have to revisit TODO
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
			if (sq.get(i).getType().equals("del")){
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
	
	private ArrayList<Operation> buildETSOS(ArrayList<Operation> sq) throws Exception {
		if (sq.size()<1) {
			return sq;
		}
		ArrayList<Operation> newSq = new ArrayList<Operation>();
		Operation op = null;
		boolean flag = true;
		
		newSq.add(sq.get(0));
		for(int i = 0; i<sq.size(); i++) {
			op = sq.get(i);
			flag = false;
			for(int j = newSq.size()-1;j>=0; j--) {
				if(flag) {
					this.effectsRelation.put(newSq.get(j).getKey(), op.getKey());
				}else {
					if(this.get_ER_ET(newSq.get(j), op)==-1) { // if sq[j] is to the left of op
						
						//PLEASE TEST THIS
						List<Operation> firstHalf = newSq.subList(0, j);
						List<Operation> secondHalf = newSq.subList(j+1, newSq.size()-1);
						newSq = (ArrayList<Operation>) firstHalf;
						newSq.add(op);
						newSq.addAll(secondHalf);
						
						flag = true;
					}else {
						ArrayList<Operation> transposed = this.transpose(newSq.get(j), op);
						op = transposed.get(0);
						newSq.set(j, transposed.get(1));
					}
				}
			}
			
			if(!flag) {
				newSq.add(0,op);
			}
		}
		return newSq;
	}
}
