package com.tuos.Collab.styletree;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Getter
@Setter
public class Element {
    private ArrayList<Leaf> leaves;
    private int size;
    public Element(){
        leaves = new ArrayList<Leaf>();
        size = 2;
    }

    public void addLeaf(Leaf leaf){
        leaves.add(leaf);
        size += leaf.getSize();
    }

    public Leaf getLeaf(int index) {
        return leaves.get(index);
    }

    public int size() {
        return size;
    }

    public void remove(int i) {
        leaves.remove(i);
    }

    public void add(int i, Leaf newLeaf) {
        leaves.add(i,newLeaf);
    }


//    public void update(Pointer start, Pointer end, String type){
//        Leaf startChild = leaves.get(start.getChild());
//        Leaf endChild = leaves.get(end.getChild());
//        int startOffset = start.getOffset();
//        int endOffset = end.getOffset();
//        if(start.getChild() == end.getChild()){
//            if(startOffset == 0 && (endOffset > 0 && endOffset<endChild.getSize())){
//
//            }else if(){
//
//            }
//        }
//    }
//
//    public ArrayList<Leaf> newLeaves(Pointer start, Pointer end, String type){
//        Leaf startChild = leaves.get(start.getChild());
//        Leaf endChild = leaves.get(end.getChild());
//        int startOffset = start.getOffset();
//        int endOffset = end.getOffset();
//        ArrayList<Leaf> newLeaves = new ArrayList<>();
//        if(start.getChild() == end.getChild()){
//            if(startOffset == 0 && (endOffset > 0 && endOffset<startChild.getSize())){ //start-half
//                newLeaves.add(new Leaf(endOffset-startOffset,type));
//                newLeaves.add(new Leaf(startChild.getSize()-endOffset,startChild.getType()));
//                return newLeaves;
//            }else if(startOffset == 0 && endOffset == startChild.getSize()){
//
//            }
//        }
//    }

}
