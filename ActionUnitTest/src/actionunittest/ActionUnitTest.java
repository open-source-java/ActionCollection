/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actionunittest;

import elsu.events.*;
import ac.core.*;
import ac.factory.objects.*;
import javax.sql.rowset.*;
import ac.factory.*;
import elsu.database.*;
import elsu.database.rowset.*;

/**
 *
 * @author dhaliwal-admin
 */
public class ActionUnitTest implements IEventSubscriber {

    public ActionFactory af = null;

    public ActionUnitTest() {
        try {
            af = new ActionFactory();
            af.addEventListener(this);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public ActionUnitTest(String config) {
        try {
            af = new ActionFactory(config);
            af.addEventListener(this);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test system identification object
    public void TestSystemIdentification() {
        SystemIdentification si = null;

        if (af != null) {
            try {
                si = (SystemIdentification) af.getActionObject("SystemIdentification.class");
                si.addEventListener(this);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (si != null) {
            TestRefresh(si, 1);
        }
    }

    // test site object
    public void TestSite() {
        Site si = null;

        if (af != null) {
            try {
                si = (Site) af.getActionObject("Site.class");
                si.addEventListener(this);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (si != null) {
            // get two records for the site object
            TestRefresh(si, new long[]{838, 830});

            // get five records for the site object
            TestRefresh(si, new long[]{838, 830, 111, 843, 792});

            // retrieve all records for site with no params
            TestRefresh(si);

            // filter site records for site_id with mask
            TestRefresh(si, "SITE_ID LIKE ?", new Object[]{"9%"});

            // filter site records for site_id with match
            TestRefresh(si, "SITE_ID = ?", new int[]{java.sql.Types.INTEGER}, new Object[]{909});

            // test action object update for one site
            try {
                TestUpdate(si, new long[]{838L}, "SITE", "DETROIT_RST");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            // test action object update for multiple site
            TestUpdate(si, new long[]{838L, 830L}, "SITE", "_RST");

            // test action object update for multiple columns
            TestUpdate(si, 838, new String[]{"SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{4L, 2L});

            // test action object update for multiple columns
            TestUpdate(si, "NCS3.SPU_SITE", new long[]{111, 830}, new String[]{"SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{1L, 2L});

            // test action object delete for site
            TestDelete(si, 838);

            // test action object delete for site
            TestDelete(si, new long[]{830, 838});

            // test action object delete with where clause
            TestDelete(si, "SITE LIKE ?", new Object[]{"DHALIWAL%"});
            TestDelete(si, "SITE IN (?, ?)", new Object[]{"WISCONSIN_REN", "WISCONSIN POINT"});

            // test action object for insert
            TestInsert(si, new Object[]{505.0D, null, null, "SSD", "DHALIWAL", null, "ROHN", null, "V4", 0L, null, "PRIMARY", null, "ESD MORICHES", null, null, null, null, null, null});

            // test action object for insert with multiple sites
            TestInsert(si, new long[]{830, 838}, new String[]{"SITE_ID", "SITE", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, "WISCONSIN_REN", 4L, 2L});

            // test action object for insert with single site column change
            TestInsert(si, new long[]{111}, new String[]{"SITE_ID", "SITE", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, "DHALIWAL2", 4L, 2L});

            // append result from one to other
            TestAppend(si, new long[]{838, 830}, new long[]{111, 843, 792});
        }
    }

    // test refresh for action object (no params)
    public void TestRefresh(ActionObject ao) {
        try {
            ao.Refresh(null, null);

            EntityDescriptor wrs = ao.getEntity();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.getRowCount());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test refresh for action object using id
    public void TestRefresh(ActionObject ao, long id) {
        try {
            if (ao != null) {
                ao.Refresh(id);

                EntityDescriptor wrs = ao.getEntity();
                System.out.println(ao.toXML());
                System.out.println(".. records selected: " + wrs.getRowCount());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test refresh for action object using id array
    public void TestRefresh(ActionObject ao, long[] id) {
        try {
            if (ao != null) {
                ao.Refresh(id);

                EntityDescriptor wrs = ao.getEntity();
                System.out.println(ao.toXML());
                System.out.println(".. records selected: " + wrs.getRowCount());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test refresh for action object with whereclause/params
    public void TestRefresh(ActionObject ao, String whereClause, Object[] values) {
        try {
            ao.Refresh(whereClause, values);

            EntityDescriptor wrs = ao.getEntity();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.getRowCount());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    // test refresh action object with whereclause overriding value types
    public void TestRefresh(ActionObject ao, String whereClause, int[] valueDataTypes, Object[] values) {
        try {
            ao.Refresh(whereClause, valueDataTypes, values);

            EntityDescriptor wrs = ao.getEntity();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.getRowCount());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test update action object
    public void TestUpdate(ActionObject ao, long[] id, String column, String value) {
        try {
            TestUpdate(ao, id, new String[]{column}, new Object[]{value});
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test update action object
    public void TestUpdate(ActionObject ao, long id, String[] columns, Object[] values) {
        try {
            // update record for site id
            TestRefresh(ao, id);

            long count = ao.Update(id, columns, values);
            System.out.println(ao.toXML());
            System.out.println(".. records updated: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test update action object
    public void TestUpdate(ActionObject ao, long[] id, String[] columns, Object[] values) {
        try {
            // update record for site id
            TestRefresh(ao, id);

            long count = ao.Update(id, columns, values);
            System.out.println(ao.toXML());
            System.out.println(".. records updated: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test update action object
    public void TestUpdate(ActionObject ao, String procedure, long[] id, String[] columns, Object[] values) {
        try {
            // update record for site id
            TestRefresh(ao, id);

            long count = ao.Update(id, columns, values);
            System.out.println(ao.toXML());
            System.out.println(".. records updated: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test delete action object for site
    public void TestDelete(ActionObject ao, long id) {
        try {
            TestRefresh(ao, id);

            long count = ao.Delete(id);
            System.out.println(ao.toXML());
            System.out.println(".. records deleted: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test delete action object for multiple sites
    public void TestDelete(ActionObject ao, long[] id) {
        try {
            TestRefresh(ao, id);

            long count = ao.Delete(id);
            System.out.println(ao.toXML());
            System.out.println(".. records deleted: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test delete action object with where clause
    public void TestDelete(ActionObject ao, String whereClause, Object[] values) {
        try {
            long count = ao.Delete(whereClause, values);

            System.out.println(ao.toXML());
            System.out.println(".. records deleted: " + count);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test insert action object
    public void TestInsert(ActionObject ao, Object[] values) {
        try {
            ao.getEntity().clear();

            // insert new record from scratch
            //ANTENNA_HEIGHT,DT_CRTD,DT_UPDT,ICON_NAME,SITE,SITE_ANTENNA_ID,SITE_ANTENNA,SITE_CONFIG_ID,SITE_CONFIG,SITE_ID,SITE_TYPE_ID,SITE_TYPE,UNIT_ID,UNIT,CONTACT_ID,CONTACT,CITYSTATE_ID,CITY,STATE,ZIP
            long recordNumber = ao.Insert(values);
            System.out.println(ao.toXML());
            System.out.println(".. record# inserted: " + recordNumber);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test insert action object with multiple sites
    public void TestInsert(ActionObject ao, long[] id, String[] columns, Object[] values) {
        try {
            // try to insert multiple records with minor changes
            // retrieve only one record info for updating
            TestRefresh(ao, id[0]);

            long recordNumber = ao.Insert(id[0], columns, values);
            System.out.println(ao.toXML());
            System.out.println(".. record# inserted: " + recordNumber);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test append action object
    public void TestAppend(ActionObject ao, long[] firstRS, long[] secondRS) {
        try {
            EntityDescriptor wrs = ActionObjectStack.View(af.getDbManager(),
                    "SELECT ANTENNA_HEIGHT,DT_CRTD,DT_UPDT,ICON_NAME,SITE,SITE_ANTENNA_ID,SITE_ANTENNA,SITE_CONFIG_ID,SITE_CONFIG,SITE_ID,SITE_TYPE_ID,SITE_TYPE,UNIT_ID,UNIT,CONTACT_ID,CONTACT,CITYSTATE_ID,CITY,STATE,ZIP FROM NCS3.VWSITE",
                    "SITE_ID IN (SELECT * FROM TABLE (?))", new int[]{java.sql.Types.ARRAY},
                    new Object[]{secondRS});
            System.out.println(ActionObjectStack.toXML(wrs));

            ao.Refresh(firstRS);
            ao.Append(wrs);

            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + ao.getEntity().getRowCount());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ActionUnitTest aut = null;

        // create the factory class
        aut = new ActionUnitTest();

        aut.TestSystemIdentification();
        //aut.TestSite();

        // class object direct access test
        if (aut.af != null) {
            try {
                EntityDescriptor wrs = ActionObjectStack.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE_STATUS",
                        null, null, null);
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                EntityDescriptor wrs = ActionObjectStack.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new int[]{java.sql.Types.VARCHAR}, new Object[]{"8%"});
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                EntityDescriptor wrs = ActionObjectStack.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new int[]{java.sql.Types.VARCHAR}, new Object[]{"9%"});
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                EntityDescriptor wrs = ActionObjectStack.Cursor(aut.af.getDbManager(),
                        "NCS3.SPS_SITE",
                        new int[]{java.sql.Types.ARRAY}, new Object[]{new Long[]{830L, 838L}});
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                EntityDescriptor wrs = ActionObjectStack.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE = ?", new int[]{java.sql.Types.VARCHAR}, new Object[]{"DHALIWAL2"});
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                long siteId = 830L;

                long count = ActionObjectStack.Execute(aut.af.getDbManager(),
                        "NCS3.SPD_SITE",
                        new int[]{java.sql.Types.BIGINT}, new Object[]{siteId},
                        null);
                //System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records affected: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            
            try {
                EntityDescriptor wrs = ActionObjectStack.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.WEBSET",
                        null, null, null);
                System.out.println(ActionObjectStack.toXML(wrs));
                System.out.println(".. records selected: " + wrs.getRowCount());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (args.length > 0) {
            aut = new ActionUnitTest(args[0]);

            if (aut.af != null) {
                aut.TestSystemIdentification();
            }
        }
    }

    @Override
    public Object EventHandler(Object sender, IEventStatusType status, String message, Object o) {
        switch (EventStatusType.valueOf(status.getName())) {
            case DEBUG:
            case ERROR:
            case INFORMATION:
                System.out.println(status.getName() + ":" + message);
                break;
            default:
                break;
        }

        return null;
    }
}
