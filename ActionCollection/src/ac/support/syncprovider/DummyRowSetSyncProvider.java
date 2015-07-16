/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.support.syncprovider;

import com.sun.rowset.internal.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author dhaliwal-admin
 */
public class DummyRowSetSyncProvider extends SyncProvider implements Serializable {
    private static final long serialVersionUID = -5411660486872857600L;

    private RowSetReader _reader;
    private TransactionalWriter _writer;

    private String _providerID = "ac.support.syncprovider.DummyRowSetSyncProvider";
    private String _vendorName = "Open Source (Action Objects)";
    private String _versionNumber = "1.0";

    public DummyRowSetSyncProvider() {
        this._providerID = this.getClass().getName();
        this._reader = new DummyRowSetReader();
        this._writer = new DummyRowSetWriter();
    }

    @Override
    public String getProviderID() {
        return this._providerID;
    }

    @Override
    public RowSetReader getRowSetReader() {
        return this._reader;
    }

    @Override
    public RowSetWriter getRowSetWriter() {
        return this._writer;
    }

    @Override
    public int getProviderGrade() {
        return SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT;
    }

    @Override
    public void setDataSourceLock(int datasource_lock) throws SyncProviderException {
        if(datasource_lock != SyncProvider.DATASOURCE_NO_LOCK ) {
          throw new SyncProviderException("lock type not supported.");
        }
    }

    @Override
    public int getDataSourceLock() throws SyncProviderException {
        return SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT;
    }

    @Override
    public int supportsUpdatableView() {
        return SyncProvider.NONUPDATABLE_VIEW_SYNC;
    }

    @Override
    public String getVersion() {
        return this._versionNumber;
    }

    @Override
    public String getVendor() {
        return this._vendorName;
    }
}
