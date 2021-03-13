package com.tuos.Collab.Model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.tuos.Collab.document.Document;
import com.tuos.Collab.operation.DocumentEditService;
import com.tuos.Collab.operation.Operation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


public class Tp2Test {

    @InjectMocks
    DocumentEditService documentEditService;

//    @Mock
//    EmployeeDao dao;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllEmployeesTest() throws Exception {
        Document d = new Document("a", "document text2");
        ArrayList<Operation> ops = new ArrayList<Operation>();
        //public Operation(char character, int position, long siteId, int stateId, String type)
        ops.add(new Operation('a', 3, 1614768999811l, 0, "ins"));
        ops.add(new Operation('a', 11, 1614768999064l, 0, "ins"));
        ops.add(new Operation('s', 4, 1614768999811l, 1, "ins"));
        ops.add(new Operation('s', 13, 1614768999064l, 2, "ins"));
        ops.add(new Operation('d', 5, 1614768999811l, 3, "ins"));
        ops.add(new Operation('d', 15, 1614768999064l, 4, "ins"));
        ops.add(new Operation('s', 4, 1614768999064l, 6, "del"));
        ops.add(new Operation('a', 3, 1614768999064l, 7, "del"));
        ops.add(new Operation('c', 2, 1614768999064l, 8, "del"));
        ops.add(new Operation('d', 11, 1614768999811l, 8, "ins"));
        ops.add(new Operation('o', 1, 1614768999064l, 9, "del"));

        //when(dao.getEmployeeList()).thenReturn(list);

        //test
        for (Operation op : ops) {
            documentEditService.update(1l, op);
        }

        assertEquals(d.getText(), "a");
        //verify(dao, times(1)).getEmployeeList();
    }

//    @Test
//    public void getEmployeeByIdTest()
//    {
//        when(dao.getEmployeeById(1)).thenReturn(new EmployeeVO(1,"Lokesh","Gupta","user@email.com"));
//
//        EmployeeVO emp = manager.getEmployeeById(1);
//
//        assertEquals("Lokesh", emp.getFirstName());
//        assertEquals("Gupta", emp.getLastName());
//        assertEquals("user@email.com", emp.getEmail());
//    }
//
//    @Test
//    public void createEmployeeTest()
//    {
//        EmployeeVO emp = new EmployeeVO(1,"Lokesh","Gupta","user@email.com");
//
//        manager.addEmployee(emp);
//
//        verify(dao, times(1)).addEmployee(emp);
//    }
}