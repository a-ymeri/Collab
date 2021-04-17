package com.tuos.Collab.styletree;

import com.tuos.Collab.document.Document;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
public class StyleTree {
    private ArrayList<Element> elements;

    public StyleTree(Element root) {
        elements = new ArrayList<Element>();
        elements.add(root);
    }

    public StyleTree(ArrayList<Element> elements) {
        this.elements = elements;
    }

    public StyleTree() {
        elements = new ArrayList<Element>();
    }

    public void addElement(Element e) {
        elements.add(e);
    }

    public Element get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    public int[] findPath(int offset) {
        Element element = new Element();
        int[] path = new int[2];

        //A modulo algorithm that finds the node where an offset belongs in and subtracts the previous node sizes
        for (int i = 0; i < this.size(); i++) {
            if (offset < this.get(i).size()) {
                element = this.get(i);
                path[0] = i;
                break;
            } else {
                offset -= this.get(i).size();
            }
        }
        //Since the previous loop has subtracted the size of the other nodes, the offset is relative to the node
//        Leaf leaf = null;
        for (int i = 0; i < element.size(); i++) {
            int leafSize = element.getLeaf(i).getSize() + 2; //account for opening and closing tag
            if (offset < leafSize) {
//                leaf = element.getLeaf(i);
                path[1] = i;
                break;
            } else {
                offset -= leafSize;
            }
        }

        return path;
    }

    public Pointer[] findPaths(int start, int end) {
        Element startElement = new Element();
        Element endElement = new Element();
//        int[][] path = new int[2][2];
        Pointer startingPointer = new Pointer();
        Pointer endingPointer = new Pointer();
        //A modulo algorithm that finds the node where an offset belongs in and subtracts the previous node sizes
        for (int i = 0; i < this.size(); i++) {

            //Here i use <. This means that a selection of 3 where size is 3 should be considered to be the next child.
            //This is because for all intents and purposes, the selection is to the right and is same-child editing
            if (start < this.get(i).size()) { //starting element found
                startElement = this.get(i);
                startingPointer.setElement(i);

                for (int j = i; j < this.size(); j++) {

                    //Here i use <=. This means that a selection of 3 where size is 3 should be considered to be the same child.
                    //This is because for all intents and purposes, this is the end of a selection and it only includes same-child letters.
                    if (end <= this.get(j).size()) { //ending element found, [1,0]
                        endElement = this.get(j);
                        endingPointer.setElement(j);
                        break;
                    } else {
                        end -= this.get(j).size();
                    }
                }
                break;
            } else {
                start -= this.get(i).size();
                end -= this.get(i).size();
            }
        }

        //Since the previous loop has subtracted the size of the other nodes, the offset is relative to the node
//        Leaf leaf = null;
        if (startingPointer.getElement() == endingPointer.getElement()) { // Same element, one loop


            for (int i = 0; i < startElement.size(); i++) {
                if (start < startElement.getLeaf(i).getSize()) { //starting child found, [0,1]
                    startingPointer.setChild(i);

                    for (int j = i; j < startElement.size(); j++) {
                        if (end <= endElement.getLeaf(j).getSize()) { //ending child found, [1,1]
                            endingPointer.setChild(j);
                            break;
                        } else {
                            end -= (endElement.getLeaf(j).getSize());
                        }
                    }
                    break;
                } else {
                    start -= (startElement.getLeaf(i).getSize());
                    end -= (startElement.getLeaf(i).getSize());
                }
            }
            startingPointer.setOffset(start);
            endingPointer.setOffset(end);
        } else {//Different element, two loops
            int[] startingChildAndOffset = findChild(startElement, start);
            int[] endingChildAndOffset = findChild(endElement, end);
            startingPointer.setChild(startingChildAndOffset[0]);
            startingPointer.setOffset(startingChildAndOffset[1]);
            endingPointer.setChild(endingChildAndOffset[0]);
            endingPointer.setOffset(endingChildAndOffset[1]);
        }
        Pointer[] paths = {startingPointer, endingPointer};
        return paths;
    }


    private int[] findChild(Element element, int position) {
        //TODO: BUGGED, IDK HOW TO FIX THIS YET, WILL HAVE TO REVISIT
        int path[] = new int[2];
        for (int i = 0; i < element.size(); i++) {

            if (position <= element.getLeaf(i).getSize()) { //starting child found, [0,1]
                path[0] = i;
                break;
            } else {
                position -= element.getLeaf(i).getSize();
            }
        }
        path[1] = position;
        return path;
    }

    public Element findElement(int offset) {

        Element element = new Element();


        //A modulo algorithm that finds the node where an offset belongs in and subtracts the previous node sizes
        for (int i = 0; i < this.size(); i++) {
            if (offset < this.get(i).size()) {
                element = this.get(i);
                break;
            } else {
                offset -= this.get(i).size();
            }
        }
        return element;
    }

    public void update(int start, int end, String type, boolean isAdd) {
        Pointer[] pointers = findPaths(start, end);
        Pointer startPointer = pointers[0];
        Pointer endPointer = pointers[1];

        Element startingElement = this.get(startPointer.getElement());
        Element endingElement = this.get(endPointer.getElement());
        Leaf startingLeaf = startingElement.getLeaf(startPointer.getChild());
        Leaf endingLeaf = endingElement.getLeaf(endPointer.getChild());

        ArrayList<Leaf> affectedLeaves = new ArrayList<>();
        //ArrayList<Leaf> temp = new ArrayList<>();

        if (startPointer.getOffset() > 0) {
            //startingElement.remove(startPointer.getChild());
            Leaf newLeaf = splitNode(startingLeaf, startPointer.getOffset());
            startingElement.add(startPointer.getChild()+1, newLeaf);
            //One new element added, shift endingLeaf's element
            if (startPointer.getChild() == endPointer.getChild()) {
                endingLeaf = newLeaf;
                //If you slice a size 10 into 3+7, the second slice on that 10 should be on the 7, and it should be shifted to the left by 3
                endPointer.shiftOffsetLeft(startPointer.getOffset());
            }
            startPointer.shiftChildRight(1);
            startingLeaf = newLeaf;
            endPointer.shiftChildRight(1);
//            endingLeaf = endingElement.getLeaf(endPointer.getChild());
        }


        if (endPointer.getOffset() < endingLeaf.getSize()) {
            //endingElement.remove(endPointer.getChild());
//
            Leaf newLeaf = splitNode(endingLeaf, endPointer.getOffset());
            endingElement.add(endPointer.getChild()+1, newLeaf);
            //affectedLeaves.add(temp.get(0)); //The middle element
        }

        for(int i = startPointer.getChild(); i<=endPointer.getChild(); i++){
            if(isAdd){
                startingElement.getLeaf(i).addType(type);
            }else{
                startingElement.getLeaf(i).removeType(type);
            }
        }

        /*
            This part of the function is responsible for merging duplicate cells, if and only if all their types are the same
         */
        int startMerge = Math.max(0,startPointer.getChild()-1);
        int endMerge = Math.min(startingElement.getLeaves().size()-1,endPointer.getChild()+1);
        for(int i = endMerge; i>startMerge; i--){

            Leaf rightNode = startingElement.getLeaf(i);
            Leaf leftNode = startingElement.getLeaf(i-1);
            if(leftNode.getTypes().equals(rightNode.getTypes()) || leftNode.getSize()==0 || rightNode.getSize()==0){

                leftNode.merge(rightNode);
                startingElement.remove(i);
            }
        }
        System.out.println("lezgo");
    }

    /*
        Given a Leaf and an offset, splits the leaf at the specified offset and returns the new leaf.
        The original leaf is modified.
     */
    private Leaf splitNode(Leaf leaf, int offset) {
        SortedSet se = new TreeSet<String>(leaf.getTypes());
        Leaf newLeaf = new Leaf(leaf.getSize() - offset, se);
        leaf.setSize(offset);
        return newLeaf;
    }

//    private void setNode(Leaf leaf, String type) {
//        leaf.addType(type);
//    }

    private Leaf mergeNode(Leaf leaf1, Leaf leaf2) {
        return new Leaf(leaf1.getSize() + leaf2.getSize(), leaf1.getTypes());
    }

//    public Leaf findNode(Integer offset) {
//        //Convert primitive data
//        Element element = new Element();
//
//
//        //A modulo algorithm that finds the node where an offset belongs in and subtracts the previous node sizes
//        for (int i = 0; i < this.size(); i++) {
//            if (offset < this.get(i).size()) {
//                element = this.get(i);
//                break;
//            } else {
//                offset -= this.get(i).size();
//            }
//        }
//        //Since the previous loop has subtracted the size of the other nodes, the offset is relative to the node
//        Leaf leaf = null;
//        for (int i = 0; i < element.size(); i++) {
//            if (offset < element.getLeaf(i).getSize()) {
//                leaf = element.getLeaf(i);
//                break;
//            } else {
//                offset -= element.getLeaf(i).getSize();
//            }
//        }
//
//        return leaf;
//        //doc.getStyleTree().getElement(0).getLeaf()
//    }


//    public void increaseLeafSize(Leaf leaf, int size){
//        leaf.increaseSize(size);
//        this.size += size;
//    }

}
