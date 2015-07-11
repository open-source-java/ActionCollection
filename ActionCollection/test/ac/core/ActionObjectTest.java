/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import ac.config.ActionConfig;
import elsu.database.DatabaseDataTypes;
import elsu.database.DatabaseManager;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.WebRowSet;
import junit.framework.TestCase;

/**
 *
 * @author dhaliwal-admin
 */
public class ActionObjectTest extends TestCase {

    public ActionObjectTest(String testName) {
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
     * Test of finalize method, of class ActionObject.
     */
    public void testFinalize() throws Exception {
        System.out.println("finalize");
        ActionObject instance = null;

        try {
            instance.finalize();
        } catch (Throwable ex) {
            //Logger.getLogger(ActionObjectTest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRowSet method, of class ActionObject.
     */
    public void testGetRowSet() {
        System.out.println("getRowSet");
        ActionObject instance = null;
        WebRowSet expResult = null;
        WebRowSet result = instance.getRowSet();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRowSet method, of class ActionObject.
     */
    public void testSetRowSet_WebRowSet() {
        System.out.println("setRowSet");
        WebRowSet rowSet = null;
        ActionObject instance = null;
        instance.setRowSet(rowSet);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRowSet method, of class ActionObject.
     */
    public void testSetRowSet_0args() throws Exception {
        System.out.println("setRowSet");
        ActionObject instance = null;
        instance.setRowSet();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConfig method, of class ActionObject.
     */
    public void testGetConfig() {
        System.out.println("getConfig");
        ActionObject instance = null;
        ConfigLoader expResult = null;
        ConfigLoader result = instance.getConfig();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDbManager method, of class ActionObject.
     */
    public void testGetDbManager() {
        System.out.println("getDbManager");
        ActionObject instance = null;
        DatabaseManager expResult = null;
        DatabaseManager result = instance.getDbManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getActionConfig method, of class ActionObject.
     */
    public void testGetActionConfig() {
        System.out.println("getActionConfig");
        ActionObject instance = null;
        ActionConfig expResult = null;
        ActionConfig result = instance.getActionConfig();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnCount method, of class ActionObject.
     */
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
        ActionObject instance = null;
        int expResult = 0;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumns method, of class ActionObject.
     */
    public void testGetColumns() {
        System.out.println("getColumns");
        ActionObject instance = null;
        List<String> expResult = null;
        List<String> result = instance.getColumns();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnDataTypes method, of class ActionObject.
     */
    public void testGetColumnDataTypes() {
        System.out.println("getColumnDataTypes");
        ActionObject instance = null;
        List<DatabaseDataTypes> expResult = null;
        List<DatabaseDataTypes> result = instance.getColumnDataTypes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSQLSelect method, of class ActionObject.
     */
    public void testGetSQLSelect() {
        System.out.println("getSQLSelect");
        ActionObject instance = null;
        String expResult = "";
        String result = instance.getSQLSelect();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSQLSelect method, of class ActionObject.
     */
    public void testSetSQLSelect() {
        System.out.println("setSQLSelect");
        String procedure = "";
        ActionObject instance = null;
        instance.setSQLSelect(procedure);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Refresh method, of class ActionObject.
     */
    public void testRefresh_long() throws Exception {
        System.out.println("Refresh");
        long id = 0L;
        ActionObject instance = null;
        WebRowSet expResult = null;
        WebRowSet result = instance.Refresh(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Refresh method, of class ActionObject.
     */
    public void testRefresh_longArr() throws Exception {
        System.out.println("Refresh");
        long[] id = null;
        ActionObject instance = null;
        WebRowSet expResult = null;
        WebRowSet result = instance.Refresh(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Refresh method, of class ActionObject.
     */
    public void testRefresh_String_ObjectArr() throws Exception {
        System.out.println("Refresh");
        String whereClause = "";
        Object[] values = null;
        ActionObject instance = null;
        WebRowSet expResult = null;
        WebRowSet result = instance.Refresh(whereClause, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Refresh method, of class ActionObject.
     */
    public void testRefresh_3args() throws Exception {
        System.out.println("Refresh");
        String whereClause = "";
        DatabaseDataTypes[] valueDataTypes = null;
        Object[] values = null;
        ActionObject instance = null;
        WebRowSet expResult = null;
        WebRowSet result = instance.Refresh(whereClause, valueDataTypes, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Update method, of class ActionObject.
     */
    public void testUpdate_long() throws Exception {
        System.out.println("Update");
        long id = 0L;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Update(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Update method, of class ActionObject.
     */
    public void testUpdate_longArr() throws Exception {
        System.out.println("Update");
        long[] id = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Update(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Update method, of class ActionObject.
     */
    public void testUpdate_3args() throws Exception {
        System.out.println("Update");
        long id = 0L;
        String[] columns = null;
        Object[] values = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Update(id, columns, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Update method, of class ActionObject.
     */
    public void testUpdate_long_HashMap() throws Exception {
        System.out.println("Update");
        long id = 0L;
        HashMap<String, Object> row = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Update(id, row);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Delete method, of class ActionObject.
     */
    public void testDelete_long() throws Exception {
        System.out.println("Delete");
        long id = 0L;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Delete(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Delete method, of class ActionObject.
     */
    public void testDelete_longArr() throws Exception {
        System.out.println("Delete");
        long[] id = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Delete(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Delete method, of class ActionObject.
     */
    public void testDelete_String_ObjectArr() throws Exception {
        System.out.println("Delete");
        String whereClause = "";
        Object[] values = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Delete(whereClause, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Insert method, of class ActionObject.
     */
    public void testInsert_ObjectArr() throws Exception {
        System.out.println("Insert");
        Object[] values = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Insert(values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Insert method, of class ActionObject.
     */
    public void testInsert_StringArr_ObjectArr() throws Exception {
        System.out.println("Insert");
        String[] columns = null;
        Object[] values = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Insert(columns, values);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Insert method, of class ActionObject.
     */
    public void testInsert_HashMap() throws Exception {
        System.out.println("Insert");
        HashMap<String, Object> row = null;
        ActionObject instance = null;
        long expResult = 0L;
        long result = instance.Insert(row);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHaspMap method, of class ActionObject.
     */
    public void testToHaspMap_0args() throws Exception {
        System.out.println("toHaspMap");
        ActionObject instance = null;
        HashMap<String, Object> expResult = null;
        HashMap<String, Object> result = instance.toHaspMap();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHaspMap method, of class ActionObject.
     */
    public void testToHaspMap_StringArr() throws Exception {
        System.out.println("toHaspMap");
        String[] columns = null;
        ActionObject instance = null;
        HashMap<String, Object> expResult = null;
        HashMap<String, Object> result = instance.toHaspMap(columns);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toXML method, of class ActionObject.
     */
    public void testToXML() throws Exception {
        System.out.println("toXML");
        ActionObject instance = null;
        String expResult = "";
        String result = instance.toXML();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class ActionObjectImpl extends ActionObject {

        public ActionObjectImpl() throws Exception {
            super(null, null);
        }
    }

}
