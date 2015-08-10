/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.factory.objects;

import ac.core.*;
import elsu.common.StatusType;
import elsu.database.*;
import elsu.support.*;
import java.util.EventObject;

/**
 *
 * @author dhaliwal-admin
 */
public class SiteControlStatus extends ActionObject {

    public SiteControlStatus(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // retrieve the action configuration from the actionobject config for
        // the class name
        super(config, dbManager);
    }

    @Override
    public Object EventHandler(Object sender, StatusType status, String message, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
