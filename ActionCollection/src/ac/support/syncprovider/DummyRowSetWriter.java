/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.support.syncprovider;

import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author dhaliwal-admin
 */
public class DummyRowSetWriter implements TransactionalWriter {
    
    @Override
    public void commit() throws SQLException {
        return;
    }

    @Override
    public void rollback() throws SQLException {
        return;
    }

    @Override
    public void rollback(Savepoint s) throws SQLException {
        return;
    }

    @Override
    public boolean writeData(RowSetInternal caller) throws SQLException {
        return true;
    }

}
