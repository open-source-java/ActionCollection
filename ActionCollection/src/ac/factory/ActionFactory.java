/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.factory;

import ac.core.*;
import elsu.database.*;
import java.lang.reflect.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author ss.dhaliwal
 */
public class ActionFactory {

    private ConfigLoader _config = null;
    private DatabaseManager _dbManager = null;

    public ActionFactory() throws Exception {
        setConfig();
        setDbManager();

        initialize();
    }

    public ActionFactory(String config) throws Exception {
        setConfig(config);
        setDbManager();

        initialize();
    }

    private void initialize() throws Exception {
        String syncProvider = getSyncProvider();
        boolean installed = false;

        if ((syncProvider != null) && (!syncProvider.isEmpty())) {
            java.util.Enumeration e = SyncFactory.getRegisteredProviders();
            while (e.hasMoreElements()) {
                e.nextElement();

                if (e.getClass().toString().replaceAll("class ", "").equals(syncProvider)) {
                    installed = true;
                    break;
                }
            }

            if (!installed) {
                SyncFactory.registerProvider(syncProvider);
                
                // log error for tracking
                getConfig().logInfo(getClass().toString() + ", initialize(), "
                        + "sync provider installed.");
            } else {
                // log error for tracking
                getConfig().logError(getClass().toString() + ", initialize(), "
                        + "sync provider already installed.");
            }
        }
    }

    public ConfigLoader getConfig() {
        return this._config;
    }

    private void setConfig() {
        try {
            this._config = new ConfigLoader();
        } catch (Exception ex) {

        }
    }

    private void setConfig(String config) {
        try {
            this._config = new ConfigLoader(config);
        } catch (Exception ex) {

        }
    }

    public DatabaseManager getDbManager() {
        return this._dbManager;
    }

    private void setDbManager() {
        if (this._dbManager == null) {
            String dbDriver
                    = getConfig().getApplicationProperties().get(
                            "service.database.driver").toString();
            String dbConnectionString
                    = getConfig().getApplicationProperties().get(
                            "service.database.connectionString").toString();
            int maxPool = 5;
            try {
                maxPool = Integer.parseInt(
                        getConfig().getApplicationProperties().get(
                                "service.database.max.pool").toString());
            } catch (Exception ex) {
                maxPool = 5;
            }

            String dbUser
                    = getConfig().getApplicationProperties().get(
                            "service.database.user").toString();
            String dbPassword
                    = getConfig().getApplicationProperties().get(
                            "service.database.password").toString();

            // capture any exceptions to prevent resource leaks
            try {
                // create the database manager
                this._dbManager = new DatabaseManager(
                        dbDriver,
                        dbConnectionString, maxPool,
                        dbUser,
                        dbPassword);
            } catch (Exception ex) {
                // log error for tracking
                getConfig().logError(getClass().toString() + ", setDbManager(), "
                        + ex.getMessage());
            }
        }
    }

    public IAction getClassByName(String className) throws Exception {
        IAction result = null;

        for (String key : getConfig().getActionProperties().keySet()) {
            if (key.equals(className)) {
                // using reflection, load the class for the service
                Class<?> actionClass = Class.forName(className);

                // create service constructor discovery type parameter array
                // populate it with the required class types
                Class<?>[] argTypes = {ConfigLoader.class, DatabaseManager.class};

                // retrieve the matching constructor for the service using
                // reflection
                Constructor<?> cons = actionClass.getDeclaredConstructor(
                        argTypes);

                // create parameter array and populate it with values to 
                // pass to the service constructor
                Object[] arguments
                        = {getConfig(), getDbManager()};

                // create new instance of the service using the discovered
                // constructor and parameters
                result = (IAction) cons.newInstance(arguments);
            }
        }

        // return new class
        return result;
    }
    
    public String getSyncProvider() {
        return getConfig().getApplicationProperty("rowset.sync.provider");
    }
}
