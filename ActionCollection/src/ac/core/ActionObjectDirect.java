/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.common.*;
import elsu.database.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.sql.rowset.*;

/**
 *
 * @author dhaliwal-admin
 */
public abstract class ActionObjectDirect {

    public static WebRowSet View(DatabaseManager dbManager, String SQLStmt, String whereClause, DatabaseDataTypes[] valueDataTypes, Object[] values) throws Exception {
        WebRowSet result = null;

        try {
            // if dbmanager is null, exit
            if (dbManager == null) {
                throw new Exception("dbManager cannot be null.");
            }

            // if sql statement is null, exit
            if ((SQLStmt == null) || (SQLStmt.isEmpty())) {
                throw new Exception("SQLStmt cannot be null.");
            }

            String sql = SQLStmt;
            if ((whereClause != null) && (!whereClause.isEmpty())) {
                sql += " WHERE " + whereClause;
            }

            if (((valueDataTypes == null) && (values != null))
                    || ((valueDataTypes != null) && (values == null))
                    || ((valueDataTypes != null) && (values != null) && (valueDataTypes.length != values.length))) {
                throw new Exception("dataTypes or values is null (or) array lengths do not match.");
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            if (values != null) {
                int paramId = 0;
                for (Object o : values) {
                    paramId++;

                    dbParams.add(new DatabaseParameter("param" + paramId, valueDataTypes[paramId - 1],
                            o));
                }
            }

            result = dbManager.getDataXML(sql, dbParams);
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, View(), " + ex.getMessage());
        }

        return result;
    }

    public static WebRowSet Cursor(DatabaseManager dbManager, String procedure, DatabaseDataTypes[] valueDataTypes, Object[] values) throws Exception {
        WebRowSet result = null;
        Map<String, Object> spResult = null;

        try {
            // if dbmanager is null, exit
            if (dbManager == null) {
                throw new Exception("dbManager cannot be null.");
            }

            // if sql statement is null, exit
            if ((procedure == null) || (procedure.isEmpty())) {
                throw new Exception("procedure cannot be null.");
            }

            if ((valueDataTypes == null) || (values == null) || (valueDataTypes.length != values.length)) {
                throw new Exception("dataTypes or values is null (or) array lengths do not match.");
            }

            String sql = "{call " + procedure + "("
                    + StringStack.padString("", valueDataTypes.length + 1, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            int paramId = 0;
            for (Object o : values) {
                paramId++;

                dbParams.add(new DatabaseParameter("param" + paramId, valueDataTypes[paramId - 1],
                        o));
            }

            result = dbManager.getDataXMLViaCursor(sql, dbParams);
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, Cursor(), " + ex.getMessage());
        }

        return result;
    }

    public static long Execute(DatabaseManager dbManager, String procedure, DatabaseDataTypes[] valueDataTypes, Object[] values) throws Exception {
        long result = 0;
        Map<String, Object> spResult = null;

        try {
            // if dbmanager is null, exit
            if (dbManager == null) {
                throw new Exception("dbManager cannot be null.");
            }

            // if sql statement is null, exit
            if ((procedure == null) || (procedure.isEmpty())) {
                throw new Exception("procedure cannot be null.");
            }

            if ((valueDataTypes == null) || (values == null) || (valueDataTypes.length != values.length)) {
                throw new Exception("dataTypes or values is null (or) array lengths do not match.");
            }

            String sql = "{call " + procedure + "("
                    + StringStack.padString("", valueDataTypes.length + 3, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            int paramId = 0;
            for (Object o : values) {
                paramId++;

                dbParams.add(new DatabaseParameter("param" + paramId, valueDataTypes[paramId - 1],
                        o));
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("count", DatabaseDataTypes.dtlong, true));
            dbParams.add(new DatabaseParameter("errorId", DatabaseDataTypes.dtlong, true));
            dbParams.add(new DatabaseParameter("status", DatabaseDataTypes.dtstring, true));

            spResult = dbManager.executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, Execute(), " + ex.getMessage());
        }

        return result;
    }

    public static HashMap<String, Object> toHashMap(WebRowSet rowSet) throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        // loop through the rowset and return all columns/values
        if ((rowSet != null) && (rowSet.size() == 1)) {
            ResultSetMetaData rsmd = rowSet.getMetaData();

            int maxColumns = rsmd.getColumnCount();
            for (int i = 1; i <= maxColumns; i++) {
                map.put(rsmd.getColumnName(i), rowSet.getObject(i));
            }
        }

        return map;
    }

    public static HashMap<String, Object> toHashMap(WebRowSet rowSet, String[] columns) throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        // loop through the rowset and return all columns/values
        if ((rowSet != null) && (rowSet.size() == 1)) {
            ResultSetMetaData rsmd = rowSet.getMetaData();
            List<String> cList = Arrays.asList(columns);

            int maxColumns = rsmd.getColumnCount();
            for (int i = 1; i <= maxColumns; i++) {
                if (cList.indexOf(rsmd.getColumnName(i)) > -1) {
                    map.put(rsmd.getColumnName(i), rowSet.getObject(i));
                }
            }
        }

        return map;
    }

    public static String toXML(WebRowSet rowSet) throws Exception {
        String result = "";

        if (rowSet != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            rowSet.writeXml(baos);
            result = baos.toString();
        } else {
            throw new Exception("ActionObject.toXML(), empty rowset provided.");
        }

        return result;
    }
}