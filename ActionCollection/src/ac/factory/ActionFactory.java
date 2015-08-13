/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.factory;

import ac.core.*;
import elsu.common.*;
import elsu.database.*;
import elsu.support.*;
import java.lang.reflect.*;
import java.util.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author ss.dhaliwal
 */
public class ActionFactory extends AbstractEventPublisher implements IEventPublisher, IEventSubscriber {

    private ConfigLoader _config = null;
    private DatabaseManager _dbManager = null;

    public ActionFactory() throws Exception {
        setConfig();
        setDbManager();

        initialize();

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", ActionFactory(), "
                + "contructor completed.", null);
    }

    public ActionFactory(String config) throws Exception {
        setConfig(config);
        setDbManager();

        initialize();

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", ActionFactory(), "
                + "contructor completed.", null);
    }

    public ActionFactory(String config, String[] suppresspath) throws Exception {
        setConfig(config, suppresspath);
        setDbManager();

        initialize();

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", ActionFactory(), "
                + "contructor completed.", null);
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

        notifyListeners(new EventObject(this), StatusType.INFORMATION,
                getClass().toString() + ", initialize(), "
                + "initialization completed.", null);
    }

    public ConfigLoader getConfig() {
        return this._config;
    }

    private void setConfig() {
        try {
            this._config = new ConfigLoader("", new String[]{
                "application.framework.attributes.key.",
                "application.actions.action.", "application.actionExtensions."});
        } catch (Exception ex) {

        }
    }

    private void setConfig(String config) {
        try {
            this._config = new ConfigLoader(config,
                    new String[]{
                        "application.framework.attributes.key.",
                        "application.actions.action.", "application.actionExtensions."});
        } catch (Exception ex) {

        }
    }

    private void setConfig(String config, String[] suppressPath) {
        try {
            this._config = new ConfigLoader(config, suppressPath);
        } catch (Exception ex) {

        }
    }

    public DatabaseManager getDbManager() {
        return this._dbManager;
    }

    private void setDbManager() {
        if (this._dbManager == null) {
            String dbDriver
                    = getConfig().getProperties().get(
                            "service.database.driver").toString();
            String dbConnectionString
                    = getConfig().getProperties().get(
                            "service.database.connectionString").toString();
            int maxPool = 5;
            try {
                maxPool = Integer.parseInt(
                        getConfig().getProperties().get(
                                "service.database.max.pool").toString());
            } catch (Exception ex) {
                maxPool = 5;
            }

            String dbUser
                    = getConfig().getProperties().get(
                            "service.database.user").toString();
            String dbPassword
                    = getConfig().getProperties().get(
                            "service.database.password").toString();

            // capture any exceptions to prevent resource leaks
            try {
                // create the database manager
                this._dbManager = new DatabaseManager(
                        dbDriver,
                        dbConnectionString, maxPool,
                        dbUser,
                        dbPassword);

                // connect the event notifiers
                this._dbManager.addEventListener(this);
            } catch (Exception ex) {
                // log error for tracking
                getConfig().logError(getClass().toString() + ", setDbManager(), "
                        + ex.getMessage());
            }

            notifyListeners(new EventObject(this), StatusType.INFORMATION,
                    getClass().toString() + ", setDbManager(), "
                    + "dbManager initialized.", null);
        }
    }

    public IAction getActionObject(String className) throws Exception {
        IAction result = null;
        String classPath = getConfig().getProperty(className).toString();

        if (classPath != null) {
            // using reflection, load the class for the service
            Class<?> actionClass = Class.forName(classPath);

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

            // check if the instance is typeof IEventPublisher
            // - if yes, then subscribe to its events
            if (result instanceof IEventPublisher) {
                ((IEventPublisher) result).addEventListener(this);
            }

            notifyListeners(new EventObject(this), StatusType.INFORMATION,
                    getClass().toString() + ", getClassByName(), "
                    + "class (" + className + "/" + classPath + ") instantiated.", null);
        } else {
            notifyListeners(new EventObject(this), StatusType.ERROR,
                    getClass().toString() + ", getClassByName(), "
                    + "class (" + className + "/null) not found.", null);
        }

        // return new class
        return result;
    }

    public String getSyncProvider() {
        return getConfig().getProperty("rowset.sync.provider").toString();
    }

    @Override
    public synchronized Object EventHandler(Object sender, IStatusType status, String message, Object o) {
        switch (StatusType.valueOf(status.getName())) {
            case DEBUG:
                getConfig().logDebug(message);
                break;
            case ERROR:
                getConfig().logError(message);
                break;
            case INFORMATION:
                getConfig().logInfo(message);
                break;
            default:
                break;
        }

        return null;
    }
}
