package ac.factory.objects;

import elsu.events.*;
import ac.core.*;
import elsu.database.*;
import elsu.support.*;

public class MySQLTable extends ActionObject {

    public MySQLTable(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // retrieve the action configuration from the actionobject config for
        // the class name
        super(config, dbManager);
    }

    @Override
    public Object EventHandler(Object sender, IEventStatusType status, String message, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
