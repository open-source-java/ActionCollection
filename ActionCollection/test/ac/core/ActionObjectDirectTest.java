/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.database.DatabaseDataTypes;
import elsu.database.DatabaseManager;
import java.util.HashMap;
import javax.sql.rowset.WebRowSet;
import junit.framework.TestCase;

/**
 *
 * @author dhaliwal-admin
 */
public class ActionObjectDirectTest extends TestCase {
    
    public ActionObjectDirectTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of View method, of class ActionObjectDirect.
     */
    public void testView() throws Exception {
        System.out.println("View");
        DatabaseManager dbManager = null;
        String SQLStmt = "";
        String whereClause = "";
        DatabaseDataTypes[] valueDataTypes = null;
        Object[] values = null;
        WebRowSet expResult = null;
        WebRowSet result = ActionObjectDirect.View(dbManager, SQLStmt, whereClause, valueDataTypes, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Cursor method, of class ActionObjectDirect.
     */
    public void testCursor() throws Exception {
        System.out.println("Cursor");
        DatabaseManager dbManager = null;
        String procedure = "";
        DatabaseDataTypes[] valueDataTypes = null;
        Object[] values = null;
        WebRowSet expResult = null;
        WebRowSet result = ActionObjectDirect.Cursor(dbManager, procedure, valueDataTypes, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Execute method, of class ActionObjectDirect.
     */
    public void testExecute() throws Exception {
        System.out.println("Execute");
        DatabaseManager dbManager = null;
        String procedure = "";
        DatabaseDataTypes[] valueDataTypes = null;
        Object[] values = null;
        long expResult = 0L;
        long result = ActionObjectDirect.Execute(dbManager, procedure, valueDataTypes, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHashMap method, of class ActionObjectDirect.
     */
    public void testToHashMap_WebRowSet() throws Exception {
        System.out.println("toHashMap");
        WebRowSet rowSet = null;
        HashMap<String, Object> expResult = null;
        HashMap<String, Object> result = ActionObjectDirect.toHashMap(rowSet);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHashMap method, of class ActionObjectDirect.
     */
    public void testToHashMap_WebRowSet_StringArr() throws Exception {
        System.out.println("toHashMap");
        WebRowSet rowSet = null;
        String[] columns = null;
        HashMap<String, Object> expResult = null;
        HashMap<String, Object> result = ActionObjectDirect.toHashMap(rowSet, columns);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class ActionObjectDirect.
     */
    public void testToXML() throws Exception {
        System.out.println("toXML");
        WebRowSet rowSet = null;
        String expResult = "";
        String result = ActionObjectDirect.toXML(rowSet);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
