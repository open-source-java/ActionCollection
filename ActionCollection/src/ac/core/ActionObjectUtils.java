/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.common.*;
import elsu.database.*;
import elsu.database.rowset.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author dhaliwal-admin
 */
public abstract class ActionObjectUtils {

    public static EntityDescriptor View(Connection conn, String SQLStmt,
            String whereClause, int[] valueDataTypes,
            Object[] values) throws Exception {
        EntityDescriptor result = null;

        try {
            // if conn is null, exit
            if (conn == null) {
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

            result = DatabaseUtils.getEntityDescriptor(conn, sql, dbParams);
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, View(), " + ex.getMessage());
        }

        return result;
    }

    public static EntityDescriptor Cursor(Connection conn,
            String procedure, int[] valueDataTypes, Object[] values)
            throws Exception {
        EntityDescriptor result = null;
        Map<String, Object> spResult = null;

        try {
            // if conn is null, exit
            if (conn == null) {
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
                    + StringUtils.padString("", valueDataTypes.length + 1, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            for (int i = 0; i < values.length; i++) {
                o = values[i];

                dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                        o));
            }
            dbParams.add(new DatabaseParameter("paramOCursor", java.sql.Types.REF_CURSOR, DatabaseParameterType.OUTPUT));

            spResult = DatabaseUtils.executeProcedure(conn, sql, dbParams);
            result = (EntityDescriptor) spResult.get("paramOCursor");
        } catch (Exception ex) {
            throw new Exception("class ActionObjectDirect, Cursor(), " + ex.getMessage());
        }

        return result;
    }

    public static long Execute(Connection conn, String procedure,
            int[] valueDataTypes, Object[] values,
            Map<String, Integer> outputDataTypes) throws Exception {
        long result = 0;
        Map<String, Object> spResult = null;

        try {
            // if conn is null, exit
            if (conn == null) {
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
                    + StringUtils.padString("", valueDataTypes.length, "?", ",");
            if ((outputDataTypes != null) && (outputDataTypes.size() > 0)) {
                sql += StringUtils.padString("", outputDataTypes.size(), "?", ",");
            } else {
                sql += "," + StringUtils.padString("", 3, "?", ",");
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
                    dbParams.add(new DatabaseParameter(param, outputDataTypes.get(param), DatabaseParameterType.OUTPUT));
                }
            } else {
                dbParams.add(new DatabaseParameter("count", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
                dbParams.add(new DatabaseParameter("errorId", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
                dbParams.add(new DatabaseParameter("status", java.sql.Types.VARCHAR, DatabaseParameterType.OUTPUT));
            }

            spResult = DatabaseUtils.executeProcedure(conn, sql, dbParams);

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
