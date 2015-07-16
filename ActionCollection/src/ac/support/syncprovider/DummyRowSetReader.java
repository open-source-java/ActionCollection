/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.support.syncprovider;

import java.sql.*;
import javax.sql.*;

/**
 *
 * @author dhaliwal-admin
 */
public class DummyRowSetReader implements RowSetReader {
    
    @Override
    public void readData(RowSetInternal caller) throws SQLException {
        return;
    }

}
