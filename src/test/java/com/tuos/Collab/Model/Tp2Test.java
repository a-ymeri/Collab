package com.tuos.Collab.Model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import com.sun.source.tree.Tree;
import com.tuos.Collab.document.Document;
import com.tuos.Collab.operation.DocumentEditService;
import com.tuos.Collab.operation.Operation;
import com.tuos.Collab.styletree.Element;
import com.tuos.Collab.styletree.Leaf;
import com.tuos.Collab.styletree.Pointer;
import com.tuos.Collab.styletree.StyleTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.Transient;


public class Tp2Test {

    @InjectMocks
    DocumentEditService documentEditService;
    Document document = new Document("testDoc", "<p><strong>123</strong><em>1235</em><strong>12345</strong><em>123456</em></p>");
    StyleTree tree = new StyleTree();
    Leaf l1 = new Leaf(3,"</strong>");
    Leaf l2 = new Leaf(4,"</em>");
    Leaf l3 = new Leaf(5,"</strong>");
    Leaf l4 = new Leaf(6,"</em>");
    Element e1 = new Element();
    Element e2 = new Element();


//    @Mock
//    EmployeeDao dao;

    @Before
    public void init() {
        e1.addLeaf(l1);
        e1.addLeaf(l2);
        e1.addLeaf(l3);
        e1.addLeaf(l4);

        e2.addLeaf(l4);
        e2.addLeaf(l3);
        e2.addLeaf(l2);

        tree.addElement(e1);
        tree.addElement(e2);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sameChildStartEndPathTest(){

        Pointer[] pointers = tree.findPaths(1,4);
        Pointer startingPointer = pointers[0];
        Pointer endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 0);
        assertEquals(endingPointer.getChild(), 1);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 1);
        assertEquals(endingPointer.getOffset(), 1);

        pointers = tree.findPaths(6,10);
        startingPointer = pointers[0];
        endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 1);
        assertEquals(endingPointer.getChild(), 2);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 3);
        assertEquals(endingPointer.getOffset(), 3);
    }

    @Test
    public void sameChildHalfEndPathTest(){

        Pointer[] pointers = tree.findPaths(2,3);
        Pointer startingPointer = pointers[0];
        Pointer endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 0);
        assertEquals(endingPointer.getChild(), 0);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 2);
        assertEquals(endingPointer.getOffset(), 3);

        pointers = tree.findPaths(8,12);
        startingPointer = pointers[0];
        endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 2);
        assertEquals(endingPointer.getChild(), 2);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 1);
        assertEquals(endingPointer.getOffset(), 5);
    }

    @Test
    public void sameChildHalfHalfPathTest(){

        Pointer[] pointers = tree.findPaths(2,3);
        Pointer startingPointer = pointers[0];
        Pointer endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 0);
        assertEquals(endingPointer.getChild(), 0);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 2);
        assertEquals(endingPointer.getOffset(), 3);

        pointers = tree.findPaths(4,5);
        startingPointer = pointers[0];
        endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 1);
        assertEquals(endingPointer.getChild(), 1);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 1);
        assertEquals(endingPointer.getOffset(), 2);
    }

    @Test
    public void sameChildStartHalfPathTest(){

        Pointer[] pointers = tree.findPaths(0,3);
        Pointer startingPointer = pointers[0];
        Pointer endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 0);
        assertEquals(endingPointer.getChild(), 0);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 0);
        assertEquals(endingPointer.getOffset(), 3);

        pointers = tree.findPaths(7,9);
        startingPointer = pointers[0];
        endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 2);
        assertEquals(endingPointer.getChild(), 2);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 0);
        assertEquals(endingPointer.getOffset(), 2);
    }

    @Test
    public void differentChild(){

        Pointer[] pointers = tree.findPaths(1,7);
        Pointer startingPointer = pointers[0];
        Pointer endingPointer = pointers[1];
        assertEquals(startingPointer.getChild(), 0);
        assertEquals(endingPointer.getChild(), 1);
        assertEquals(startingPointer.getElement(), 0);
        assertEquals(endingPointer.getElement(), 0);
        assertEquals(startingPointer.getOffset(), 1);
        assertEquals(endingPointer.getOffset(), 4);

//        pointers = tree.findPaths(6,9);
//        startingPointer = pointers[0];
//        endingPointer = pointers[1];
//        assertEquals(startingPointer.getChild(), 1);
//        assertEquals(endingPointer.getChild(), 1);
//        assertEquals(startingPointer.getElement(), 0);
//        assertEquals(endingPointer.getElement(), 0);
//        assertEquals(startingPointer.getOffset(), 0);
//        assertEquals(endingPointer.getOffset(), 3);
    }

    @Test
    public void splitSameNodeTest(){
        StyleTree styleTree = document.generateTree("<p><strong>123</strong><em>1234</em><strong>12345</strong><em>123456</em></p>");
        styleTree.update(1,2,"span",true);
        assertEquals(styleTree.get(0).getLeaf(0).getSize(), 1);
        assertEquals(styleTree.get(0).getLeaf(1).getSize(), 1);
        assertEquals(styleTree.get(0).getLeaf(2).getSize(), 1);
        TreeSet<String> set = new TreeSet<String>();
        assertEquals(styleTree.get(0).getLeaf(0).getTypes(), setOf("strong"));
        assertEquals(styleTree.get(0).getLeaf(1).getTypes(), setOf("span", "strong"));
        assertEquals(styleTree.get(0).getLeaf(2).getTypes(), setOf("strong"));
    }

    public TreeSet<String> setOf(String ... types){
        TreeSet<String> typeSet = new TreeSet<String>();
        typeSet.addAll(Arrays.asList(types));
        return typeSet;
    }
    @Test
    /*
        This test tests for start-half and half-end cases
     */
    public void splitDifferentNodes(){
        StyleTree styleTree = document.generateTree("<p><span>123</span><em>1234</em><strong>12345</strong><em>123456</em></p>");
        styleTree.update(1,4,"strong",true);
        assertEquals(styleTree.get(0).getLeaf(0).getSize(), 1);
        assertEquals(styleTree.get(0).getLeaf(1).getSize(), 2);
        assertEquals(styleTree.get(0).getLeaf(2).getSize(), 1);
        assertEquals(styleTree.get(0).getLeaf(3).getSize(), 3);
        assertEquals(styleTree.get(0).getLeaf(0).getTypes(), setOf());
        assertEquals(styleTree.get(0).getLeaf(1).getTypes(), setOf("strong"));
        assertEquals(styleTree.get(0).getLeaf(2).getTypes(), setOf("em","strong"));
        assertEquals(styleTree.get(0).getLeaf(3).getTypes(), setOf("em"));


        styleTree = document.generateTree("<p><span>123</span><em>1234</em><strong>12345</strong><em>123456</em></p>");
        styleTree.update(1,10,"strong", true);
        assertEquals(styleTree.get(0).getLeaf(0).getSize(), 1);
        assertEquals(styleTree.get(0).getLeaf(1).getSize(), 2);
        assertEquals(styleTree.get(0).getLeaf(2).getSize(), 4);
        assertEquals(styleTree.get(0).getLeaf(3).getSize(), 5);
        assertEquals(styleTree.get(0).getLeaf(0).getTypes(), setOf());
        assertEquals(styleTree.get(0).getLeaf(1).getTypes(), setOf("strong"));
        assertEquals(styleTree.get(0).getLeaf(2).getTypes(), setOf("em","strong"));
        assertEquals(styleTree.get(0).getLeaf(3).getTypes(), setOf("strong"));
    }

    @Test
    public void testMerge(){
        StyleTree styleTree = document.generateTree("<p><span>123</span><span+strong>456</span+strong><span>789</span></p>");
        styleTree.update(0,3,"strong",true);

        assertEquals(styleTree.get(0).getLeaf(0).getSize(), 6);
        assertEquals(styleTree.get(0).getLeaf(1).getSize(), 3);
        assertEquals(styleTree.get(0).getLeaf(0).getTypes(), setOf("strong"));
        assertEquals(styleTree.get(0).getLeaf(1).getTypes(), setOf());
    }

    @Test
    public void styleAndUnstyle(){
        StyleTree styleTree = document.generateTree("<p><span>1234567890</span></p>");
        styleTree.update(4,6,"strong",true);
        styleTree.update(4,6,"strong",false);
        assertEquals(styleTree.getElements().get(0).getLeaves().size(), 1);
    }
}