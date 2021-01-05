package com.tuos.Collab.Model;

public class Operation {

	private char character;
	private int position;
	private int siteId;
	private String type;
	private int stateId;
	
	//constructor for insertion operations
	public Operation(char character, int siteId, int position, String type) {
		this.character = character;
		this.siteId = siteId;
		this.position = position;
		this.type = type;
	}
	
	//constructor for identity operations
	public Operation(char character, int position) {
		this.character = character;
		this.position = position;
		
	}
	//constructor for deletion operations
	public Operation(int siteId, int position, String type) {
		this.siteId = siteId;
		this.position = position;
		this.type = type;
	}
	
	//constructor for deletion operations
	public Operation(Operation toClone) {
		this.character = toClone.getCharacter();
		this.siteId = toClone.getSiteId();
		this.position = toClone.getPosition();
		this.type = toClone.getType();
	}
	
	public int getSiteId() {
		return siteId;
	}

//	public void setSiteId(int siteId) {
//		this.siteId = siteId;
//	}

	public char getCharacter() {
		return character;
	}

//	public void setCharacter(char character) {
//		this.character = character;
//	}

	public int getPosition() {
		return position;
	}
	
	public void shiftRight() {
		position++;
	}
	
	public void shiftLeft() {
		position--;
	}

	public String getType() {
		return type;
	}
	
	public int getStateId() {
		return this.stateId;
	}
	
	public OperationKey getKey() {
		return new OperationKey(siteId, stateId);
	}
//	public void setIndex(int index) {
//		this.index = index;
//	}
}
