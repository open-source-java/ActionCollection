/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.factory.objects;

import ac.core.*;
import elsu.database.*;

/**
 *
 * @author ss.dhaliwal
 */
public class Site extends ActionObject {

    public Site(ConfigLoader config, DatabaseManager dbManager) throws Exception {
        // retrieve the action configuration from the actionobject config for
        // the class name
        super(config, dbManager);
    }
}
