/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.core;

import elsu.database.*;
import elsu.database.rowset.*;
import java.util.*;
import javax.sql.rowset.*;

/**
 *
 * @author dhaliwal-admin
 */
public interface IAction {
    EntityDescriptor Append(EntityDescriptor wrs) throws Exception;
    
    EntityDescriptor Refresh() throws Exception;
    EntityDescriptor Refresh(long id) throws Exception;
    EntityDescriptor Refresh(long[] id) throws Exception;
    EntityDescriptor Refresh(String whereClause, Object[] values) throws Exception;
    EntityDescriptor Refresh(String whereClause, int[] valueDataTypes, Object[] values) throws Exception;
    EntityDescriptor Refresh(String[] columns) throws Exception;
    EntityDescriptor Refresh(String[] columns, String whereClause, Object[] values) throws Exception;
    EntityDescriptor Refresh(String[] columns, String whereClause, int[] valueDataTypes, Object[] values) throws Exception;

    long Update(long id) throws Exception;
    long Update(long[] id) throws Exception;
    long Update(long id, String[] columns, Object[] values) throws Exception;
    long Update(long id[], String[] columns, Object[] values) throws Exception;
    long Update(long id, HashMap<String, Object> row) throws Exception;
    long Update(long id, String procedure, String[] columns, Object[] values) throws Exception;
    long Update(long id[], String procedure, String[] columns, Object[] values) throws Exception;
    long Update(long id, String procedure, HashMap<String, Object> row) throws Exception;

    long Delete(long id) throws Exception;
    long Delete(long[] id) throws Exception;
    long Delete(String whereClause, Object[] values) throws Exception;
    
    long Insert(Object[] values) throws Exception;
    long Insert(long id, String[] columns, Object[] values) throws Exception;
    long Insert(long id, HashMap<String, Object> row) throws Exception;
    
    HashMap<String, Object> toHaspMap() throws Exception;
    HashMap<String, Object> toHaspMap(String[] columns) throws Exception;

    String toXML() throws Exception;
}
