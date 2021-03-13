package com.tuos.Collab.operation;

public class Operation {

	private char character;
	private int position;
	private long siteID;
	private String type;
	private int stateID;
	
	//constructor for insertion operations
	public Operation(char character, long siteId, int position, String type) {
		this.character = character;
		this.siteID = siteId;
		this.position = position;
		this.type = type;
	}
	
	public Operation(char character, int position, long siteId, int stateId, String type) {
		this.character = character;
		this.siteID = siteId;
		this.position = position;
		this.type = type;
		this.stateID = stateId;
	}
	
	//constructor for identity operations
	public Operation(char character, int position) {
		this.character = character;
		this.position = position;
		
	}
	//constructor for deletion operations
	public Operation(long siteId, int position, String type) {
		this.siteID = siteId;
		this.position = position;
		this.type = type;
	}
	
	//constructor for deletion operations
	public Operation(Operation toClone) {
		this.character = toClone.getCharacter();
		this.siteID = toClone.getSiteID();
		this.position = toClone.getPosition();
		this.type = toClone.getType();
		this.stateID = toClone.getStateId();
	}
	
	public long getSiteID() {
		return siteID;
	}

//	public void setSiteId(int siteId) {
//		this.siteId = siteId;
//	}

	public void setStateID(int stateId) {
		this.stateID = stateId;
	}
	@Override
	public String toString() {
		return "Operation [character=" + character + ", position=" + position + ", siteId=" + siteID + ", type=" + type
				+ ", stateId=" + stateID + "]";
	}
	

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
		return this.stateID;
	}
	
	public OperationKey getKey() {
		return new OperationKey(siteID, stateID);
	}
//	public void setIndex(int index) {
//		this.index = index;
//	}
}
