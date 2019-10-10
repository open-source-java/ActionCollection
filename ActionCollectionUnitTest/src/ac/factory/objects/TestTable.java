package ac.factory.objects;

import elsu.events.*;
import ac.core.*;
import elsu.database.*;
import elsu.database.rowset.EntityDescriptor;
import elsu.support.*;

public class TestTable extends ActionObject {

    public TestTable(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // retrieve the action configuration from the actionobject config for
        // the class name
        super(config, dbManager);
    }

    @Override
    public Object EventHandler(Object sender, IEventStatusType status, String message, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	@Override
	public EntityDescriptor Refresh(long[] id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
