/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.common.*;
import elsu.database.*;
import elsu.database.rowset.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.sql.rowset.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author dhaliwal-admin
 */
public abstract class ActionObjectStack {

    public static EntityDescriptor View(DatabaseManager dbManager, String SQLStmt,
            String whereClause, DatabaseDataType[] valueDataTypes,
            Object[] values) throws Exception {
        EntityDescriptor result = null;

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

            if ((valueDataTypes != null)
                    && ((valueDataTypes.length == 0) || (values == null) || (values.length == 0))) {
                throw new Exception("values array length is zero.");
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            if (values != null) {
                Object o = null;
                for (int i = 0; i < values.length; i++) {
                    o = values[i];

                    dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                            o));
                }
            }

            result = dbManager.getDataED(sql, dbParams);
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, View(), " + ex.getMessage());
        }

        return result;
    }

    public static EntityDescriptor Cursor(DatabaseManager dbManager,
            String procedure, DatabaseDataType[] valueDataTypes, Object[] values)
            throws Exception {
        EntityDescriptor result = null;
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
            Object o = null;
            for (int i = 0; i < values.length; i++) {
                o = values[i];

                dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                        o));
            }
            dbParams.add(new DatabaseParameter("paramOCursor", DatabaseDataType.dtcursor, true));

            spResult = dbManager.executeProcedure(sql, dbParams);
            result = (EntityDescriptor) spResult.get("paramOCursor");
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, Cursor(), " + ex.getMessage());
        }

        return result;
    }

    public static long Execute(DatabaseManager dbManager, String procedure,
            DatabaseDataType[] valueDataTypes, Object[] values,
            Map<String, DatabaseDataType> outputDataTypes) throws Exception {
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
                    + StringStack.padString("", valueDataTypes.length, "?", ",");
            if ((outputDataTypes != null) && (outputDataTypes.size() > 0)) {
                sql += StringStack.padString("", outputDataTypes.size(), "?", ",");
            } else {
                sql += "," + StringStack.padString("", 3, "?", ",");
            }
            sql +=  ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            for (int i = 0; i < values.length; i++) {
                o = values[i];

                dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                        o));
            }

            // add the output parameters for status reporting
            if ((outputDataTypes != null) && (outputDataTypes.size() > 0)) {
                for (String param : outputDataTypes.keySet()) {
                    dbParams.add(new DatabaseParameter(param, outputDataTypes.get(param), true));
                }
            } else {
                dbParams.add(new DatabaseParameter("count", DatabaseDataType.dtlong, true));
                dbParams.add(new DatabaseParameter("errorId", DatabaseDataType.dtlong, true));
                dbParams.add(new DatabaseParameter("status", DatabaseDataType.dtstring, true));
            }

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

    public static HashMap<String, Object> toHashMap(ArrayList<RowDescriptor> rows)
            throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        // loop through the entity and return all columns/values
        if ((rows != null) && (rows.size() == 1)) {
            RowDescriptor row = rows.get(0);

            for (int i = 1; i <= row.getColumnCount(); i++) {
                map.put(row.getColumn(i).getName(), row.getValue(i));
            }
        } else {
            throw new Exception("rowSet count > 1");
        }

        return map;
    }

    public static HashMap<String, Object> toHashMap(ArrayList<RowDescriptor> rows,
            String[] columns) throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        // loop through the entity and return all columns/values
        if ((rows != null) && (rows.size() == 1)) {
            RowDescriptor row = rows.get(0);

            String fieldName = "";
            ArrayList<String> columnList = new ArrayList<String>(Arrays.asList(columns));
            for (int i = 1; i <= row.getColumnCount(); i++) {
                fieldName = row.getColumn(i).getName();

                if (columnList.contains(fieldName)) {
                    map.put(row.getColumn(i).getName(), row.getValue(i));
                }
            }
        } else {
            throw new Exception("rowSet count > 1");
        }

        return map;
    }

    public static String toXML(EntityDescriptor entity) throws Exception {
        String result = "";

        if (entity != null) {
            result = entity.toXML();
        } else {
            throw new Exception("ActionObject.toXML(), empty rowset provided.");
        }

        return result;
    }
}
