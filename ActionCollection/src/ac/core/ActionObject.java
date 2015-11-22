/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.events.*;
import elsu.common.*;
import elsu.database.*;
import elsu.database.rowset.*;
import elsu.support.*;
import java.util.*;
import java.sql.*;
import javax.sql.rowset.*;

/**
 *
 * @author dhaliwal-admin
 */
public abstract class ActionObject extends AbstractEventManager implements IAction, IEventPublisher, IEventSubscriber {

    private ConfigLoader _config = null;
    private DatabaseManager _dbManager = null;
    private ActionConfig _actionConfig = null;
    private EntityDescriptor _entity = null;
    private List<String> _columns = new ArrayList<>();
    private List<Integer> _columnDataTypes = new ArrayList<>();
    private List<String> _dataTypes = new ArrayList<>();
    private List<String> _dataTypesClass = new ArrayList<>();
    private String _selectProcedure = "";
    private String _orderBy = "";

    public ActionObject(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // load the initial values for the object from config
        this._config = config;
        this._actionConfig = ActionConfig.LoadConfig(config, this.getClass().toString().replaceFirst("class ", ""));

        //if column definition is empty, throw exception
        this._columns = Arrays.asList(getActionConfig().getColumns().toUpperCase().replaceAll(" ", "").split(","));

        //if column datatypes is not empty and count does not meet columns, throw exception
        try {
            if (!getActionConfig().getColumnDataTypes().isEmpty()) {
                List<String> dt = Arrays.asList(getActionConfig().getColumnDataTypes().toUpperCase().replaceAll(" ", "").split(","));

                for (String s : dt) {
                    this._columnDataTypes.add(DatabaseStack.getDbDataType(s));
                }

                if ((dt.size() > 0) && (this._columns.size() != this._columnDataTypes.size())) {
                    throw new Exception("# of columnDataTypes do not match # of columns");
                }
            }
        } catch (Exception ex) {
            this._columnDataTypes.clear();
            throw new Exception(ex);
        }

        setSQLSelect(getActionConfig().getSQLSelect());

        this._dbManager = dbManager;
        discoverColumnsDataTypes();

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", ActionObject(), "
                + "contructor completed.", null);
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (this._entity != null) {
                EntityDescriptor wrs = this._entity;
                this._entity = null;
            }
        } catch (Exception exi) {
        } finally {
            super.finalize();
        }
    }

    public EntityDescriptor getEntity() {
        return this._entity;
    }

    protected void setEntity(EntityDescriptor entity) throws Exception {
        // clear old to allow GC to select on first run
        if (getEntity() != null) {
            try {
                EntityDescriptor wrs = this._entity;
                this._entity = null;
            } catch (Exception exi) {
            }
        }

        this._entity = entity;
    }

    protected ConfigLoader getConfig() {
        return this._config;
    }

    protected DatabaseManager getDbManager() {
        return this._dbManager;
    }

    protected ActionConfig getActionConfig() {
        return this._actionConfig;
    }

    protected int getColumnCount() {
        return getColumns().size();
    }

    protected List<String> getColumns() {
        return this._columns;
    }

    protected List<Integer> getColumnDataTypes() {
        return this._columnDataTypes;
    }

    private void discoverColumnsDataTypes() throws Exception {
        // only execute if it has never been instantiated
        if (this._dataTypes.size() > 0) {
            return;
        }

        try {
            // if select procedure is not defined, exit
            if ((getActionConfig().getSQLSelect() == null) || (getActionConfig().getSQLSelect().isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            // this will always return zero records, just metadata is required
            String sql = getSQLSelect()
                    + " WHERE 1 = 2";

            EntityDescriptor result = getDbManager().getDataED(sql, null);

            // parse the webresult and populate the datatypes for each column
            Map<String, ColumnDescriptor> rsmd = result.getColumns();
            int maxColumns = rsmd.size();

            this._dataTypes.clear();
            this._dataTypesClass.clear();
            for (String field : rsmd.keySet()) {
                this._dataTypes.add(rsmd.get(field).getTypeName());
                this._dataTypesClass.add(rsmd.get(field).getClassName());
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", discoverColumnsDataTypes(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", discoverColumnsDataTypes(), "
                + "column discovery successful.", null);
    }

    public String getSQLSelect() {
        return this._selectProcedure;
    }

    protected String getSQLSelect(String[] columns) {
        String result = "";

        try {
            result = "SELECT ";
            result += CollectionStack.ArrayToString(columns).toUpperCase();
            result += " FROM " + getActionConfig().getSQLSelect().toUpperCase();
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", getSQLSelect(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = "";
        }

        return result;
    }

    protected void setSQLSelect(String procedure) {
        try {
            this._selectProcedure = "SELECT ";
            this._selectProcedure += CollectionStack.ArrayToString((String[]) getColumns().toArray()).toUpperCase();
            this._selectProcedure += " FROM " + procedure.toUpperCase();
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", setSQLSelect(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);
        }
    }

    protected String getOrderBy() {
        return this._orderBy;
    }

    @Override
    public EntityDescriptor Append(EntityDescriptor entity) throws Exception {
        EntityDescriptor result = getEntity();
        boolean matchOk = false;

        try {
            // if current rowset is null, exit
            if (result == null) {
                throw new Exception("object rowset not initialized.");
            }

            // if argument is null, then exit
            if ((entity == null) || (entity.getRowCount() == 0)) {
                throw new Exception("parameter rowset not initialized (or) no records in rowset.");
            }

            // compare metadata from both, if they do not match, exit
            Map<String, ColumnDescriptor> oRSMD = result.getColumns();
            Map<String, ColumnDescriptor> pRSMD = entity.getColumns();

            if (oRSMD.size() != pRSMD.size()) {
                throw new Exception("source and destination rowsets do not have same number of columns.");
            }

            for (String key : oRSMD.keySet()) {
                // compare columnname, columnprecision, columnscale, columntype
                if ((oRSMD.get(key).getClassName().equals(pRSMD.get(key).getClassName()))
                        && (oRSMD.get(key).getName().equals(pRSMD.get(key).getName()))
                        && (oRSMD.get(key).getTypeName().equals(pRSMD.get(key).getTypeName()))
                        && (oRSMD.get(key).getType() == pRSMD.get(key).getType())
                        && (oRSMD.get(key).getPrecision() == pRSMD.get(key).getPrecision())
                        && (oRSMD.get(key).getScale() == pRSMD.get(key).getScale())) {
                    matchOk = true;
                } else {
                    matchOk = false;
                    break;
                }
            }

            // if match is not ok, report error
            if (!matchOk) {
                throw new Exception("columns types (class, name, precision, scale, or type) do not match.");
            }

            // copy data from entity to object rowset
            RowDescriptor newRow = null;
            for (RowDescriptor row : entity.getRows()) {
                newRow = new RowDescriptor(entity.getColumns(), entity.getColumnsById());
                newRow.cloneRow(row);

                result.getRows().add(row);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Append(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Append(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh() throws Exception {
        return Refresh(null, null);
    }

    @Override
    public EntityDescriptor Refresh(long id) throws Exception {
        EntityDescriptor result = null;

        try {
            // if select procedure is not defined, exit
            if ((getActionConfig().getSQLSelect() == null) || (getActionConfig().getSQLSelect().isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            String sql = getSQLSelect()
                    + " WHERE " + getActionConfig().getPrimaryId() + " = ?";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            dbParams.add(new DatabaseParameter("param1", java.sql.Types.BIGINT, id));

            result = getDbManager().getDataED(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh(long[] id) throws Exception {
        EntityDescriptor result = null;
        Map<String, Object> spResult = null;

        try {
            // if select procedure is not defined, exit
            if ((getActionConfig().getSQLCursor() == null) || (getActionConfig().getSQLCursor().isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            // if array is null or empty
            if ((id == null) || (id.length == 0)) {
                throw new Exception("id array is null (or) array length is zero.");
            }

            String sql = "{call " + getActionConfig().getSQLCursor() + "(?, ?)}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            dbParams.add(new DatabaseParameter("param1", java.sql.Types.ARRAY, id));
            dbParams.add(new DatabaseParameter("paramOCursor", java.sql.Types.REF_CURSOR, DatabaseParameterType.OUTPUT));

            spResult = getDbManager().executeProcedure(sql, dbParams);
            result = (EntityDescriptor) spResult.get("paramOCursor");
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh(String whereClause, Object[] values) throws Exception {
        EntityDescriptor result = null;

        try {
            // if select procedure is not defined, exit
            if ((getActionConfig().getSQLSelect() == null) || (getActionConfig().getSQLSelect().isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            String sql = getSQLSelect();
            if ((whereClause != null) && (!whereClause.isEmpty())) {
                sql += " WHERE " + whereClause;

                if ((values == null) || (values.length == 0)) {
                    throw new Exception("values is null (or) array length is zero.");
                }
            }
            if ((getOrderBy() != null) && (!getOrderBy().isEmpty())) {
                sql += " ORDER BY " + getOrderBy();
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    o = values[i];

                    dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDbDataType(o),
                            o));
                }
            }

            result = getDbManager().getDataED(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh(String whereClause, int[] valueDataTypes, Object[] values) throws Exception {
        EntityDescriptor result = null;

        try {
            // if select procedure is not defined, exit
            if ((getActionConfig().getSQLSelect() == null) || (getActionConfig().getSQLSelect().isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            String sql = getSQLSelect();
            if ((whereClause != null) && (!whereClause.isEmpty())) {
                sql += " WHERE " + whereClause;

                // if where clause is specified, then values cannot be null
                if ((valueDataTypes != null)
                        && ((valueDataTypes.length == 0) || (values == null) || (values.length == 0))) {
                    throw new Exception("values array length is zero.");
                }
            }
            if ((getOrderBy() != null) && (!getOrderBy().isEmpty())) {
                sql += " ORDER BY " + getOrderBy();
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    o = values[i];

                    dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                            o));
                }
            }

            result = getDbManager().getDataED(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh(String[] columns) throws Exception {
        return Refresh(columns, null, null);
    }

    @Override
    public EntityDescriptor Refresh(String[] columns, String whereClause, Object[] values) throws Exception {
        EntityDescriptor result = null;

        try {
            String sql = getSQLSelect(columns);

            // if select procedure is not defined, exit
            if ((sql == null) || (sql.isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            if ((whereClause != null) && (!whereClause.isEmpty())) {
                sql += " WHERE " + whereClause;

                // if where clause is specified, then values cannot be null
                if ((values == null) || (values.length == 0)) {
                    throw new Exception("values is null (or) array length is zero.");
                }
            }
            if ((getOrderBy() != null) && (!getOrderBy().isEmpty())) {
                sql += " ORDER BY " + getOrderBy();
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    o = values[i];

                    dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDbDataType(o),
                            o));
                }
            }

            result = getDbManager().getDataED(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public EntityDescriptor Refresh(String[] columns, String whereClause, int[] valueDataTypes, Object[] values) throws Exception {
        EntityDescriptor result = null;

        try {
            String sql = getSQLSelect(columns);

            // if select procedure is not defined, exit
            if ((sql == null) || (sql.isEmpty())) {
                throw new Exception("No select procedure defined.");
            }

            if ((whereClause != null) && (!whereClause.isEmpty())) {
                sql += " WHERE " + whereClause;

                // if where clause is specified, then values cannot be null
                if ((valueDataTypes != null)
                        && ((valueDataTypes.length == 0) || (values == null) || (values.length == 0))) {
                    throw new Exception("values array length is zero.");
                }
            }
            if ((getOrderBy() != null) && (!getOrderBy().isEmpty())) {
                sql += " ORDER BY " + getOrderBy();
            }

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    o = values[i];

                    dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                            o));
                }
            }

            result = getDbManager().getDataED(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setEntity(result);

        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "EntityDescriptor result.", result);
        return result;
    }

    @Override
    public long Update(long id) throws Exception {
        long result = 0;
        boolean isRecordValid = false;
        Map<String, Object> spResult = null;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if update procedure is not defined, exit
            if ((getActionConfig().getSQLUpdate() == null) || (getActionConfig().getSQLUpdate().isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            String sql = "{call " + getActionConfig().getSQLUpdate() + "("
                    + StringStack.padString("", getColumns().size() + 3, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the siteId parameter value
            Object o = null;
            for (RowDescriptor row : getEntity().getRows()) {
                if (Long.valueOf(row.getValue(getActionConfig().getPrimaryId()).toString()) == id) {
                    for (int i = 1; i <= getEntity().getColumnCount(); i++) {
                        o = row.getValue(i);

                        // if custom datatypes are defined, use them
                        if (!getColumnDataTypes().isEmpty()) {
                            dbParams.add(new DatabaseParameter("param" + (i), getColumnDataTypes().get(i - 1),
                                    o));
                        } else {
                            dbParams.add(new DatabaseParameter("param" + (i), DatabaseStack.getDbDataType(o),
                                    o));
                        }
                    }

                    isRecordValid = true;
                    break;
                }
            }

            // check if record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("count", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("errorId", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("status", java.sql.Types.VARCHAR, DatabaseParameterType.OUTPUT));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Update(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Update(long[] id) throws Exception {
        long result = 0;

        // if update procedure is not defined, exit
        try {
            if ((getActionConfig().getSQLUpdate() == null) || (getActionConfig().getSQLUpdate().isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            // call the overloaded method to complete the update; this is assuming
            // the webrowset is multiple records
            for (long value : id) {
                result += Update(value);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Update(), "
                + "multiple value result.", result);
        return result;
    }

    @Override
    public long Update(long id, String[] columns, Object[] values) throws Exception {
        long result = 0;
        boolean isRecordValid = false;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if update procedure is not defined, exit
            if ((getActionConfig().getSQLUpdate() == null) || (getActionConfig().getSQLUpdate().isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            // check if the number of columsn = number of values
            if ((columns == null) || (values == null) || (columns.length != values.length) || (values.length == 0)) {
                throw new Exception("Update, columns or values is null (or) array lengths do not match (or) array length is zero.");
            }

            // update the memory record set with new values
            Object o = null;
            for (RowDescriptor row : getEntity().getRows()) {
                if (Long.valueOf(row.getValue(getActionConfig().getPrimaryId()).toString()) == id) {
                    for (int i = 0; i < columns.length; i++) {
                        row.setValue(columns[i], values[i]);
                    }

                    isRecordValid = true;
                    break;
                }
            }

            // check if not record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // call the overloaded update
            result = Update(id);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        // return total records updated
        return result;
    }

    @Override
    public long Update(long id[], String[] columns, Object[] values) throws Exception {
        long result = 0;
        boolean isRecordValid = false;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if update procedure is not defined, exit
            if ((getActionConfig().getSQLUpdate() == null) || (getActionConfig().getSQLUpdate().isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            // check if the number of columsn = number of values
            if ((columns == null) || (values == null) || (columns.length != values.length) || (values.length == 0)) {
                throw new Exception("Update, columns or values is null (or) array lengths do not match (or) array length is zero.");
            }

            // loop and update each record
            for (long recId : id) {
                result += Update(recId, columns, values);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        return result;
    }

    @Override
    public long Update(long id, HashMap<String, Object> row) throws Exception {
        return Update(id, row.keySet().toArray(new String[0]), row.values().toArray());
    }

    @Override
    public long Update(long id, String procedure, String[] columns, Object[] values) throws Exception {
        long result = 0;
        boolean isRecordValid = false;
        Map<String, Object> spResult = null;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if update procedure is not defined, exit
            if ((procedure == null) || (procedure.isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            // check if the number of columsn = number of values
            if ((columns == null) || (values == null) || (columns.length != values.length)) {
                throw new Exception("Update, columns or values is null (or) array lengths do not match.");
            }

            String sql = "{call " + getActionConfig().getSQLUpdate() + "("
                    + StringStack.padString("", getColumns().size() + 3, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // update the memory record set with new values
            Object o = null;

            for (RowDescriptor row : getEntity().getRows()) {
                if (Long.valueOf(row.getValue(getActionConfig().getPrimaryId()).toString()) == id) {
                    for (int i = 0; i < columns.length; i++) {
                        row.setValue(columns[i], values[i]);

                        o = row.getValue(i);

                        // if custom datatypes are defined, use them
                        if (!getColumnDataTypes().isEmpty()) {
                            dbParams.add(new DatabaseParameter("param" + (i), getColumnDataTypes().get(getColumnDataTypes().indexOf(columns[i])),
                                    o));
                        } else {
                            dbParams.add(new DatabaseParameter("param" + (i), DatabaseStack.getDbDataType(o),
                                    o));
                        }

                        isRecordValid = true;
                        break;
                    }
                }
            }

            // check if not record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("count", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("errorId", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("status", java.sql.Types.VARCHAR, DatabaseParameterType.OUTPUT));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(
                new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Update(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Update(long id[], String procedure, String[] columns, Object[] values) throws Exception {
        long result = 0;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if update procedure is not defined, exit
            if ((procedure == null) || (procedure.isEmpty())) {
                throw new Exception("No update procedure defined.");
            }

            // check if the number of columsn = number of values
            if ((columns == null) || (values == null) || (columns.length != values.length)) {
                throw new Exception("Update, columns or values is null (or) array lengths do not match.");
            }

            // loop and update all the records passed
            for (long recId : id) {
                result += Update(recId, procedure, columns, values);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        return result;
    }

    @Override
    public long Update(long id, String procedure, HashMap<String, Object> row) throws Exception {
        return Update(id, procedure, row.keySet().toArray(new String[0]), row.values().toArray());
    }

    @Override
    public long Delete(long id) throws Exception {
        long result = 0;
        boolean isRecordValid = false;
        Map<String, Object> spResult = null;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if delete procedure is not defined, exit
            if ((getActionConfig().getSQLDelete() == null) || (getActionConfig().getSQLDelete().isEmpty())) {
                throw new Exception("No delete procedure defined.");
            }

            String sql = "{call " + getActionConfig().getSQLDelete() + "(?,?,?,?)}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // make sure the id is part of the select, otherwise reject the call
            for (RowDescriptor row : getEntity().getRows()) {
                if (Long.valueOf(row.getValue(getActionConfig().getPrimaryId()).toString()) == id) {
                    // store the siteId parameter value
                    dbParams.add(new DatabaseParameter("param1", java.sql.Types.BIGINT, id));

                    isRecordValid = true;
                    break;
                }
            }

            // check if record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("count", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("errorId", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("status", java.sql.Types.VARCHAR, DatabaseParameterType.OUTPUT));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Delete(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Delete(long[] id) throws Exception {
        long result = 0;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if delete procedure is not defined, exit
            if ((getActionConfig().getSQLDelete() == null) || (getActionConfig().getSQLDelete().isEmpty())) {
                throw new Exception("No delete procedure defined.");
            }

            // call the overloaded method to complete the update; this is assuming
            // the webrowset is multiple records
            for (long value : id) {
                try {
                    result += Delete(value);
                } catch (Exception ex) {
                    notifyListeners(new EventObject(this), EventStatusType.ERROR,
                            getClass().toString() + ", Delete(), "
                            + GlobalStack.LINESEPARATOR + ex.getMessage(), value);

                    result = 0;
                }
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Delete(), "
                + "multiple value result.", result);
        return result;
    }

    @Override
    public long Delete(String whereClause, Object[] values) throws Exception {
        long result = 0;

        try {
            // if delete procedure is not defined, exit
            if ((getActionConfig().getSQLDelete() == null) || (getActionConfig().getSQLDelete().isEmpty())) {
                throw new Exception("No delete procedure defined.");
            }

            // get the records to delete
            EntityDescriptor wrs = Refresh(whereClause, values);

            // if there is no records then skip the process
            if (getEntity().getRowCount() > 0) {
                // collect the primary keys and delete the data
                long[] idList = new long[getEntity().getRowCount()];

                int index = 0;
                for (RowDescriptor row : getEntity().getRows()) {
                    idList[index++] = Long.valueOf(row.getValue(getActionConfig().getPrimaryId()).toString());
                }

                // update the database
                result = Delete(idList);
            } else {
                result = 0;
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Delete(), "
                + "value result.", result);
        return result;
    }

    @Override
    public long Insert(Object[] values) throws Exception {
        long result = 0;
        Map<String, Object> spResult = null;

        try {
            // if rowset is null, exit
            if ((getEntity() == null) || (getEntity().getRows() == null)) {
                throw new Exception("RowSet is null.");
            }

            // if insert procedure is not defined, exit
            if ((getActionConfig().getSQLInsert() == null) || (getActionConfig().getSQLInsert().isEmpty())) {
                throw new Exception("No insert procedure defined.");
            }

            if ((values == null) || (values.length < getColumns().size())) {
                throw new Exception("Not enough values to insert.");
            }

            String sql = "{call " + getActionConfig().getSQLInsert() + "("
                    + StringStack.padString("", getColumns().size() + 3, "?", ",") + ")}";

            ArrayList<DatabaseParameter> dbParams;
            dbParams = new ArrayList<>();

            // store the parameter value
            Object o = null;
            for (int i = 0; i < getColumnCount(); i++) {
                o = values[i];

                // if custom datatypes are defined, use them
                if (!getColumnDataTypes().isEmpty()) {
                    dbParams.add(new DatabaseParameter("param" + (i + 1), getColumnDataTypes().get(i),
                            o));
                } else {
                    dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDbDataType(o),
                            o));
                }
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("recid", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("errorId", java.sql.Types.BIGINT, DatabaseParameterType.OUTPUT));
            dbParams.add(new DatabaseParameter("status", java.sql.Types.VARCHAR, DatabaseParameterType.OUTPUT));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            // get the record id inserted
            result = Integer.parseInt(spResult.get("recid").toString());
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Insert(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Insert(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Insert(long id, String[] columns, Object[] values) throws Exception {
        long result = 0;
        Object[] rowValues = null;

        try {
            // if insert procedure is not defined, exit
            if ((getActionConfig().getSQLInsert() == null) || (getActionConfig().getSQLInsert().isEmpty())) {
                throw new Exception("No insert procedure defined.");
            }

            // check if the number of columsn = number of values
            if ((columns == null) || (values == null) || (columns.length != values.length)) {
                throw new Exception("Columns or values is null (or) array lengths do not match.");
            }

            // force a refresh on the dataset
            Refresh(id);

            // update the memory record set with new values
            Object o = null;

            if (getEntity().getRowCount() > 0) {
                RowDescriptor row = getEntity().getRows().get(0);
                rowValues = new Object[getEntity().getColumnCount()];

                // store old values into object array for passing to overload
                for (int i = 1; i <= getEntity().getColumnCount(); i++) {
                    rowValues[i - 1] = row.getValue(i);
                }

                //  take new values and replace them in the object array
                int index = 0, j = 0;
                for (String column : columns) {
                    index = getEntity().getColumn(column).getColumnPosition();
                    rowValues[index - 1] = values[j++];
                }

                // call the overloaded function
                result = Insert(rowValues);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", Insert(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), EventStatusType.INFORMATION,
                getClass().toString() + ", Insert(), "
                + "value result.", result);
        return result;
    }

    @Override
    public long Insert(long id, HashMap<String, Object> row) throws Exception {
        return Insert(id, row.keySet().toArray(new String[0]), row.values().toArray());
    }

    @Override
    public HashMap<String, Object> toHaspMap() throws Exception {
        return ActionObjectStack.toHashMap(getEntity().getRows());
    }

    @Override
    public HashMap<String, Object> toHaspMap(String[] columns) throws Exception {
        return ActionObjectStack.toHashMap(getEntity().getRows(), columns);
    }

    @Override
    public String toXML() throws Exception {
        String result = "";

        try {
            result = ActionObjectStack.toXML(getEntity());
        } catch (Exception ex) {
            // log error for tracking
            notifyListeners(new EventObject(this), EventStatusType.ERROR,
                    getClass().toString() + ", toXML(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        return result;
    }
}
