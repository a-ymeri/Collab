package com.tuos.Collab.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class Tp2Test {

	@Test
	void tp2test() throws Exception {
		// TP2 TEST, ARBITRARY TRANSFORMATION PATHS
		Document d = new Document("abc");
		Document d1 = new Document("abc");
		Document d2 = new Document("abc");

		Operation op1 = new Operation('x', 2, 1, 1, "ins"); // char, position, siteid, stateid, op
		Operation op2 = new Operation('y', 1, 2, 1, "ins");
		Operation op3 = new Operation('b', 1, 3, 1, "del"); // b is irrelevant
//		Operation op4 = new Operation('d', 4, 4, 1, "ins");
//		Operation op5 = new Operation('r', 1, 5, 1, "ins");
//		Operation op6 = new Operation('a', 5, 6, 1, "ins");

		d.update(op1);
		d.update(op2);
		d.update(op3);

		d1.update(op1);
		d1.update(op2);
		d1.update(op3);

		d2.update(op1);
		d2.update(op2);
		d2.update(op3);
		String text = d.getText();
		String text1 = d1.getText();
		String text2 = d2.getText();
		assertEquals(text, "ayxc");
		assertEquals(text1, "ayxc");
		assertEquals(text2, "ayxc");
	}

	@Test
	void basicTest() throws Exception {
		Document d = new Document("ab");
		Operation op1 = new Operation('x', 1, 1, 0, "ins"); // char, position, siteid, stateid, op
		Operation op2 = new Operation(' ', 1, 2, 0, "ins");
		d.update(op1);
		d.update(op2);
		String text = d.getText();
		assertEquals(text, "ax b"); // decided by site ID
	}

	void test2() throws Exception {
		Document d = new Document("ab");
		Operation op1 = new Operation('x', 1, 1, 0, "del"); // char, position, siteid, stateid, op
		Operation op2 = new Operation('a', 1, 2, 1, "del");
//		Operation op3 = new Operation('a', 2, 3, 1, "ins");
//		Operation op4 = new Operation('d', 4, 4, 1, "ins");
//		Operation op5 = new Operation('r', 1, 5, 1, "ins");
//		Operation op6 = new Operation('a', 5, 6, 1, "ins");

		d.update(op1);
		d.update(op2);
		String text = d.getText();
		assertEquals(text, "a");
	}

	@Test
	void newTest() throws Exception {
		Document d = new Document("123456789");
		Operation op1 = new Operation('a', 2, 1, 0, "ins");
		Operation op2 = new Operation('b', 3, 1, 1, "ins");
		Operation op3 = new Operation('c', 4, 1, 2, "ins");
		Operation op4 = new Operation('a', 7, 2, 0, "ins");

		
		Operation op5 = new Operation('b', 8, 2, 4, "ins");
		op5 = d.inclusionTransform(op5, op1);
		op5 = d.inclusionTransform(op5, op2);
		op5 = d.inclusionTransform(op5, op3);
		
		Operation op6 = new Operation('c', 9, 2, 5, "ins");
		op6 = d.inclusionTransform(op6, op1);
		op6 = d.inclusionTransform(op6, op2);
		op6 = d.inclusionTransform(op6, op3);

		ArrayList<Operation> ops = new ArrayList<Operation>();

		ops.add(op1);
		ops.add(op2);
		ops.add(op3);
		ops.add(op4);
		ops.add(op5);
		ops.add(op6);

		System.out.println(d.text);
		for (Operation o : ops) {
			System.out.println(o.getPosition());
			d.update(o);
			System.out.println(d.text);
		}

		String text = d.getText();
		System.out.println(d.getHistoryBuffer().toString());
		assertEquals(text, "12abc34567abc89");

	}

	@Test
	void arbitraryTest() throws Exception {
		/*
		 {  type=insert_text, path=[0, 0], offset=9, text=a, siteID=1611920476497, stateID=0}
			123456789a
			{type=insert_text, path=[0, 0], offset=9, text=a, siteID=1611920476497, stateID=0}
			------------------------------
			{type=insert_text, path=[0, 0], offset=10, text=a, siteID=1611920476497, stateID=1}
			123456789aa
			{type=insert_text, path=[0, 0], offset=10, text=a, siteID=1611920476497, stateID=1}
			------------------------------
			{type=insert_text, path=[0, 0], offset=11, text=a, siteID=1611920476497, stateID=2}
			123456789aaa
			{type=insert_text, path=[0, 0], offset=11, text=a, siteID=1611920476497, stateID=2}
			------------------------------
			{type=remove_text, path=[0, 0], offset=7, text=8, siteID=1611920475690, stateID=2}
			12345679aaa
			{type=remove_text, path=[0, 0], offset=7, text=8, siteID=1611920475690, stateID=3}
			------------------------------
			{type=insert_text, path=[0, 0], offset=12, text=a, siteID=1611920476497, stateID=3}
			12345679aaa
			{type=insert_text, path=[0, 0], offset=13, text=a, siteID=1611920476497, stateID=3}
		 */

		// char, position, siteid, stateid, op
		Document d = new Document("123456789");
		Operation op1 = new Operation('a', 9, 1, 0, "ins");
		Operation op2 = new Operation('a', 10, 1, 1, "ins");
		Operation op3 = new Operation('a', 11, 1, 2, "ins");
		Operation op4 = new Operation('8', 7, 2, 2, "del");
		Operation op5 = new Operation('a', 12, 1, 3, "ins");
		
		ArrayList<Operation> ops = new ArrayList<Operation>();

		ops.add(op1);
		ops.add(op2);
		ops.add(op3);
		ops.add(op4);
		ops.add(op5);
		
		for (Operation o : ops) {
			d.update(o);
			System.out.println(d.text);
		}

		String text = d.getText();
//		System.out.println(d.getHistoryBuffer().toString());
		assertEquals(text, "12345679aaaa");
		
	}

}
