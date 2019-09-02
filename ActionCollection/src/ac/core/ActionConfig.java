/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.support.*;

/**
 *
 * @author ss.dhaliwal
 */
public class ActionConfig {

    private String _classId = "";
    private String _columns = "";
    private String _columnDataTypes = "";
    private String _SQLSelect = "";
    private String _SQLCursor = "";
    private String _SQLUpdate = "";
    private String _SQLDelete = "";
    private String _SQLInsert = "";
    private String _orderBy = "";

    private String _primaryId = "";

    public ActionConfig() {
    }

    public ActionConfig(String classId, String columns, String columnDataTypes,
            String SQLSelect, String SQLCursor, String SQLUpdate, String SQLDelete,
            String SQLInsert, String primaryId, String orderBy) {
        setClassId(classId);
        setColumns(columns);
        setColumnDataTypes(columnDataTypes);
        setSQLSelect(SQLSelect);
        setSQLCursor(SQLCursor);
        setSQLUpdate(SQLUpdate);
        setSQLDelete(SQLDelete);
        setSQLInsert(SQLInsert);
        setPrimaryId(primaryId);
        setOrderBy(orderBy);
    }

    public String getClassId() {
        return this._classId;
    }

    protected void setClassId(String classId) {
        this._classId = classId;
    }

    public String getColumns() {
        return this._columns;
    }

    protected void setColumns(String columns) {
        this._columns = columns;
    }

    public String getColumnDataTypes() {
        return this._columnDataTypes;
    }

    protected void setColumnDataTypes(String columnDataTypes) {
        this._columnDataTypes = columnDataTypes;
    }

    public String getSQLSelect() {
        return this._SQLSelect;
    }

    protected void setSQLSelect(String SQLSelect) {
        this._SQLSelect = SQLSelect;
    }

    public String getSQLCursor() {
        return this._SQLCursor;
    }

    protected void setSQLCursor(String SQLCursor) {
        this._SQLCursor = SQLCursor;
    }

    public String getSQLUpdate() {
        return this._SQLUpdate;
    }

    protected void setSQLUpdate(String SQLUpdate) {
        this._SQLUpdate = SQLUpdate;
    }

    public String getSQLDelete() {
        return this._SQLDelete;
    }

    protected void setSQLDelete(String SQLDelete) {
        this._SQLDelete = SQLDelete;
    }

    public String getSQLInsert() {
        return this._SQLInsert;
    }

    protected void setSQLInsert(String SQLInsert) {
        this._SQLInsert = SQLInsert;
    }

    public String getPrimaryId() {
        return this._primaryId;
    }

    protected void setPrimaryId(String primaryId) {
        this._primaryId = primaryId;
    }

    public String getOrderBy() {
        return this._orderBy;
    }

    protected void setOrderBy(String orderBy) {
        this._orderBy = orderBy;
    }

    public static ActionConfig LoadConfig(ConfigLoader config, String actionClass)
            throws Exception {
        ActionConfig ac = new ActionConfig();

        String classId = config.getKeyByValue(actionClass).replace(".class", "");
        String[] select = config.getProperty(classId + ".SQL.select").toString().split(";");

        ac.setClassId(classId);
        ac.setColumns(config.getProperty(classId + ".SQL.columns").toString());
        try {
            ac.setColumnDataTypes(config.getProperty(classId + ".SQL.columnTypes").toString());
        } catch (Exception exi) {
        }
        ac.setSQLSelect(select[0]);
        if (select.length > 1) {
            ac.setSQLCursor(select[1]);
        }
        try {
            ac.setSQLDelete(config.getProperty(classId + ".SQL.delete").toString());
        } catch (Exception exi) {
        }
        try {
            ac.setSQLInsert(config.getProperty(classId + ".SQL.insert").toString());
        } catch (Exception exi) {
        }
        try {
            ac.setSQLUpdate(config.getProperty(classId + ".SQL.update").toString());
        } catch (Exception exi) {
        }
        try {
            ac.setPrimaryId(config.getProperty(classId + ".SQL.index.key.primary").toString());
        } catch (Exception exi) {
        }
        try {
            ac.setOrderBy(config.getProperty(classId + ".SQL.orderBy").toString());
        } catch (Exception exi) {
        }

        return ac;
    }
}
