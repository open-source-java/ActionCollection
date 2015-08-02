/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.common.*;
import elsu.database.*;
import elsu.support.*;
import java.util.*;
import javax.sql.rowset.*;
import java.sql.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author dhaliwal-admin
 */
public abstract class ActionObject extends AbstractEventPublisher implements IAction, IEventPublisher, IEventSubscriber {

    private ConfigLoader _config = null;
    private DatabaseManager _dbManager = null;
    private String _syncProvider = "";
    private ActionConfig _actionConfig = null;
    private WebRowSet _rowSet = null;
    private List<String> _columns = new ArrayList<>();
    private List<DatabaseDataType> _columnDataTypes = new ArrayList<>();
    private List<String> _dataTypes = new ArrayList<>();
    private List<String> _dataTypesClass = new ArrayList<>();
    private String _selectProcedure = "";
    private String _orderBy = "";

    public ActionObject(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // load the initial values for the object from config
        this._config = config;
        this._actionConfig = ActionConfig.LoadConfig(config, this.getClass().toString().replaceFirst("class ", ""));

        //if column definition is empty
        try {
            this._columns = Arrays.asList(getActionConfig().getColumns().replaceAll(" ", "").split(","));
        } catch (Exception ex) {
            throw new Exception(ex);
        }
        try {
            List<String> dt = Arrays.asList(getActionConfig().getColumnDataTypes().replaceAll(" ", "").split(","));

            for (String s : dt) {
                this._columnDataTypes.add(DatabaseDataType.valueOf(("dt" + s).toLowerCase()));
            }
        } catch (Exception exi) {
            this._columnDataTypes.clear();
        }

        setSQLSelect(getActionConfig().getSQLSelect());

        this._dbManager = dbManager;
        discoverColumnsDataTypes();

        this._syncProvider = getConfig().getProperty("rowset.sync.provider").toString();

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", ActionObject(), "
                + "contructor completed.", null);
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (this._rowSet != null) {
                WebRowSet wrs = this._rowSet;
                this._rowSet = null;

                wrs.release();
                wrs.close();
            }

            // deregister sync proviert
            if ((this._syncProvider != null) && (!this._syncProvider.isEmpty())) {
                SyncFactory.unregisterProvider(this._syncProvider);
            }
        } catch (Exception exi) {
        } finally {
            super.finalize();
        }
    }

    public WebRowSet getRowSet() {
        return this._rowSet;
    }

    protected void setRowSet(WebRowSet rowSet) throws Exception {
        // clear old to allow GC to select on first run
        if (getRowSet() != null) {
            try {
                WebRowSet wrs = this._rowSet;
                this._rowSet = null;

                wrs.release();
                wrs.close();
            } catch (Exception exi) {
            }
        }

        // set the sync provider to custom provider if defined
        if (rowSet != null) {
            rowSet.setSyncProvider(getSyncProvider());
        }

        this._rowSet = rowSet;
    }

    protected void setRowSet() throws Exception {
        RowSetFactory rowSetFactory = RowSetProvider.newFactory();
        setRowSet(rowSetFactory.createWebRowSet());
    }

    public ConfigLoader getConfig() {
        return this._config;
    }

    public DatabaseManager getDbManager() {
        return this._dbManager;
    }

    public ActionConfig getActionConfig() {
        return this._actionConfig;
    }

    public int getColumnCount() {
        return getColumns().size();
    }

    public List<String> getColumns() {
        return this._columns;
    }

    public List<DatabaseDataType> getColumnDataTypes() {
        return this._columnDataTypes;
    }

    public String getSyncProvider() {
        return this._syncProvider;
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

            WebRowSet result = getDbManager().getDataXML(sql, null);

            // parse the webresult and populate the datatypes for each column
            ResultSetMetaData rsmd = result.getMetaData();
            int maxColumns = rsmd.getColumnCount();

            this._dataTypes.clear();
            this._dataTypesClass.clear();
            for (int i = 0; i < maxColumns; i++) {
                this._dataTypes.add(rsmd.getColumnTypeName(i + 1));
                this._dataTypesClass.add(rsmd.getColumnClassName(i + 1));
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", discoverColumnsDataTypes(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
            notifyListeners(new EventObject(this), StatusType.ERROR,
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
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", setSQLSelect(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);
        }
    }

    public String getOrderBy() {
        return this._orderBy;
    }

    @Override
    public WebRowSet Append(WebRowSet wrs) throws Exception {
        WebRowSet result = getRowSet();
        boolean matchOk = false;

        try {
            // if current rowset is null, exit
            if (result == null) {
                throw new Exception("object rowset not initialized.");
            }

            // if argument is null, then exit
            if ((wrs == null) || (wrs.size() == 0)) {
                throw new Exception("parameter rowset not initialized (or) no records in rowset.");
            }

            // compare metadata from both, if they do not match, exit
            ResultSetMetaData oRSMD = result.getMetaData();
            ResultSetMetaData pRSMD = wrs.getMetaData();

            if (oRSMD.getColumnCount() != pRSMD.getColumnCount()) {
                throw new Exception("source and destination rowsets do not have same columns.");
            }

            for (int i = 1; i <= oRSMD.getColumnCount(); i++) {
                // compare columnname, columnprecision, columnscale, columntype
                if ((oRSMD.getColumnClassName(i).equals(pRSMD.getColumnClassName(i)))
                        && (oRSMD.getColumnName(i).equals(pRSMD.getColumnName(i)))
                        && (oRSMD.getColumnType(i) == pRSMD.getColumnType(i))
                        && (oRSMD.getPrecision(i) == pRSMD.getPrecision(i))
                        && (oRSMD.getScale(i) == pRSMD.getScale(i))) {
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

            // copy data from wrs to object rowset
            wrs.beforeFirst();
            while (wrs.next()) {
                result.afterLast();
                result.moveToInsertRow();

                for (int i = 1; i <= oRSMD.getColumnCount(); i++) {
                    result.updateObject(i, wrs.getObject(i));
                }

                result.insertRow();
                result.moveToCurrentRow();
                result.acceptChanges();
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Append(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Append(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh() throws Exception {
        return Refresh(null, null);
    }

    @Override
    public WebRowSet Refresh(long id) throws Exception {
        WebRowSet result = null;

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
            dbParams.add(new DatabaseParameter("param1", DatabaseDataType.dtlong,
                    id));

            result = getDbManager().getDataXML(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh(long[] id) throws Exception {
        WebRowSet result = null;

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
            dbParams.add(new DatabaseParameter("param1", DatabaseDataType.dtarray,
                    id));

            result = getDbManager().getDataXMLViaCursor(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh(String whereClause, Object[] values) throws Exception {
        WebRowSet result = null;

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

                    dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDataType(o),
                            o));
                }
            }

            result = getDbManager().getDataXML(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh(String whereClause, DatabaseDataType[] valueDataTypes, Object[] values) throws Exception {
        WebRowSet result = null;

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

            result = getDbManager().getDataXML(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh(String[] columns) throws Exception {
        return Refresh(columns, null, null);
    }

    @Override
    public WebRowSet Refresh(String[] columns, String whereClause, Object[] values) throws Exception {
        WebRowSet result = null;

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

                    if (o == null) {
                        getRowSet().updateNull(i + 1);
                    } else {
                        dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDataType(o),
                                o));
                    }
                }
            }

            result = getDbManager().getDataXML(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public WebRowSet Refresh(String[] columns, String whereClause, DatabaseDataType[] valueDataTypes, Object[] values) throws Exception {
        WebRowSet result = null;

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

                    if (o == null) {
                        getRowSet().updateNull(i + 1);
                    } else {
                        dbParams.add(new DatabaseParameter("param" + (i + 1), valueDataTypes[i],
                                o));
                    }
                }
            }

            result = getDbManager().getDataXML(sql, dbParams);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Refresh(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = null;
            throw new Exception(ex);
        }

        setRowSet(result);

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Refresh(), "
                + "WebRowSet result.", result);
        return result;
    }

    @Override
    public long Update(long id) throws Exception {
        long result = 0;
        boolean isRecordValid = false;
        Map<String, Object> spResult = null;

        try {
            // if rowset is null, exit
            if (getRowSet() == null) {
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
            ResultSetMetaData rsmd = getRowSet().getMetaData();
            int maxColumns = rsmd.getColumnCount();
            Object o = null;

            getRowSet().beforeFirst();
            while (getRowSet().next()) {
                if (getRowSet().getLong(getActionConfig().getPrimaryId()) == id) {
                    for (int i = 0; i < maxColumns; i++) {
                        o = getRowSet().getObject(i + 1);

                        // if custom datatypes are defined, use them
                        if (!getColumnDataTypes().isEmpty()) {
                            dbParams.add(new DatabaseParameter("param" + (i + 1), getColumnDataTypes().get(i),
                                    o));
                        } else {
                            dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDataType(o),
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
            dbParams.add(new DatabaseParameter("count", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("errorId", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("status", DatabaseDataType.dtstring, true));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());

            // accept any pending changes for the row
            getRowSet().acceptChanges();
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
            if (getRowSet() == null) {
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
            getRowSet().beforeFirst();
            while (getRowSet().next()) {
                if (getRowSet().getLong(getActionConfig().getPrimaryId()) == id) {
                    for (int i = 0; i < columns.length; i++) {
                        // store the data into the rowset
                        if (values[i] == null) {
                            getRowSet().updateNull(i + 1);
                        } else {
                            getRowSet().updateObject(columns[i], values[i]);
                        }
                    }

                    // update the memory dataset
                    getRowSet().updateRow();

                    isRecordValid = true;
                    break;
                }
            }

            // check if record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // call the overloaded update
            result = Update(id);
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
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
            if (getRowSet() == null) {
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
            notifyListeners(new EventObject(this), StatusType.ERROR,
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
            if (getRowSet() == null) {
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
            List<String> cList = Arrays.asList(columns);

            getRowSet().beforeFirst();
            while (getRowSet().next()) {
                if (getRowSet().getLong(getActionConfig().getPrimaryId()) == id) {
                    for (int i = 0; i < columns.length; i++) {
                        // store the data into the rowset
                        if (values[i] == null) {
                            getRowSet().updateNull(columns[i]);
                        } else {
                            getRowSet().updateObject(columns[i], values[i]);
                        }

                        // update the dbparams with column values
                        if (!getColumnDataTypes().isEmpty()) {
                            dbParams.add(new DatabaseParameter("param" + (i + 1), getColumnDataTypes().get(cList.indexOf(columns[i])),
                                    values[i]));
                        } else {
                            dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDataType(values[i]),
                                    values[i]));
                        }
                    }

                    // update the memory dataset and accept changes
                    getRowSet().updateRow();

                    // add the output parameters for status reporting
                    dbParams.add(new DatabaseParameter("count", DatabaseDataType.dtlong, true));
                    dbParams.add(new DatabaseParameter("errorId", DatabaseDataType.dtlong, true));
                    dbParams.add(new DatabaseParameter("status", DatabaseDataType.dtstring, true));

                    spResult = getDbManager().executeProcedure(sql, dbParams);

                    // check if error occured, report it
                    Long errorCode = Long.parseLong(spResult.get("errorId").toString());
                    if ((errorCode > 0) || (errorCode < 0)) {
                        throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
                    }

                    result = Integer.parseInt(spResult.get("count").toString());
                    getRowSet().acceptChanges();
                    break;
                }
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Update(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Update(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Update(long id[], String procedure, String[] columns, Object[] values) throws Exception {
        long result = 0;

        try {
            // if rowset is null, exit
            if (getRowSet() == null) {
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
            notifyListeners(new EventObject(this), StatusType.ERROR,
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
            if (getRowSet() == null) {
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
            getRowSet().beforeFirst();
            while (getRowSet().next()) {
                if (getRowSet().getLong(getActionConfig().getPrimaryId()) == id) {
                    // store the siteId parameter value
                    dbParams.add(new DatabaseParameter("param1", DatabaseDataType.dtlong,
                            id));

                    // delete the row from the record set
                    getRowSet().deleteRow();

                    isRecordValid = true;
                    break;
                }
            }

            // check if record is valid
            if (!isRecordValid) {
                throw new Exception("record not is present in the selected rowset.");
            }

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("count", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("errorId", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("status", DatabaseDataType.dtstring, true));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            result = Integer.parseInt(spResult.get("count").toString());

            // accept changes to rowset
            getRowSet().acceptChanges();
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", Delete(), "
                + "hashmap result.", spResult);
        return result;
    }

    @Override
    public long Delete(long[] id) throws Exception {
        long result = 0;

        try {
            // if rowset is null, exit
            if (getRowSet() == null) {
                throw new Exception("RowSet is null.");
            }

            // if delete procedure is not defined, exit
            if ((getActionConfig().getSQLDelete() == null) || (getActionConfig().getSQLDelete().isEmpty())) {
                throw new Exception("No delete procedure defined.");
            }

        // call the overloaded method to complete the update; this is assuming
            // the webrowset is multiple records
            for (long value : id) {
                result += Delete(value);
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return total records updated
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
            WebRowSet wrs = Refresh(whereClause, values);

            // if there is no records then skip the process
            if (getRowSet().size() > 0) {
                // collect the primary keys and delete the data
                long[] idList = new long[getRowSet().size()];

                int index = 0;
                getRowSet().beforeFirst();
                while (getRowSet().next()) {
                    idList[index] = getRowSet().getLong(getActionConfig().getPrimaryId());
                    index++;
                }

                // update the database
                result = Delete(idList);
            } else {
                result = 0;
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Delete(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
            if (getRowSet() == null) {
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
            getRowSet().moveToInsertRow();

            Object o = null;
            for (int i = 0; i < getColumnCount(); i++) {
                o = values[i];

                // store the data into the rowset
                if (o == null) {
                    getRowSet().updateNull(i + 1);
                } else {
                    getRowSet().updateObject(i + 1, values[i]);
                }

                // if custom datatypes are defined, use them
                if (!getColumnDataTypes().isEmpty()) {
                    dbParams.add(new DatabaseParameter("param" + (i + 1), getColumnDataTypes().get(i),
                            o));
                } else {
                    dbParams.add(new DatabaseParameter("param" + (i + 1), DatabaseStack.getDataType(o),
                            o));
                }
            }

            // update the rowset
            getRowSet().insertRow();
            getRowSet().moveToCurrentRow();
            getRowSet().acceptChanges();

            // add the output parameters for status reporting
            dbParams.add(new DatabaseParameter("recid", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("errorId", DatabaseDataType.dtlong, true));
            dbParams.add(new DatabaseParameter("status", DatabaseDataType.dtstring, true));

            spResult = getDbManager().executeProcedure(sql, dbParams);

            // check if error occured, report it
            Long errorCode = Long.parseLong(spResult.get("errorId").toString());
            if ((errorCode > 0) || (errorCode < 0)) {
                throw new Exception("SQL Exception, " + spResult.get("errorId").toString() + ", " + spResult.get("status").toString());
            }

            // get the record id inserted
            result = Integer.parseInt(spResult.get("recid").toString());

            // accept changes to the rowset
            getRowSet().acceptChanges();
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Insert(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            result = 0;
            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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

            getRowSet().beforeFirst();
            while (getRowSet().next()) {
                if (getRowSet().getLong(getActionConfig().getPrimaryId()) == id) {
                    for (int i = 0; i < columns.length; i++) {
                        getRowSet().updateObject(columns[i], values[i]);
                    }

                    // populate the row values to insert into data
                    rowValues = new Object[getColumnCount()];
                    for (int i = 0; i < getColumnCount(); i++) {
                        rowValues[i] = getRowSet().getObject(i + 1);
                    }

                    // call the overloaded function
                    result = Insert(rowValues);
                    break;
                }
            }
        } catch (Exception ex) {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", Insert(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        // return the value of the count field
        notifyListeners(new EventObject(this), StatusType.INFORMATION,
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
        return ActionObjectStack.toHashMap(getRowSet());
    }

    @Override
    public HashMap<String, Object> toHaspMap(String[] columns) throws Exception {
        return ActionObjectStack.toHashMap(getRowSet(), columns);
    }

    @Override
    public String toXML() throws Exception {
        String result = "";

        try {
            result = ActionObjectStack.toXML(getRowSet());
        } catch (Exception ex) {
            // log error for tracking
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", toXML(), "
                    + GlobalStack.LINESEPARATOR + ex.getMessage(), null);

            throw new Exception(ex);
        }

        return result;
    }
}
