/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package actionunittest;

import ac.core.*;
import ac.factory.objects.*;
import javax.sql.rowset.*;
import ac.factory.*;
import elsu.database.*;
import javax.sql.rowset.spi.*;

/**
 *
 * @author dhaliwal-admin
 */
public class ActionUnitTest {

    public ActionFactory af = null;

    public ActionUnitTest() {
        try {
            af = new ActionFactory();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public ActionUnitTest(String config) {
        try {
            af = new ActionFactory(config);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test system identification object
    public void TestSystemIdentification() {
        SystemIdentification si = null;

        if (af != null) {
            try {
                si = (SystemIdentification) af.getClassByName("ac.factory.objects.SystemIdentification");
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
                si = (Site) af.getClassByName("ac.factory.objects.Site");
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
            TestRefresh(si, "SITE_ID = ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtint}, new Object[]{909});

            // test action object update for one site
            try {
                TestUpdate(si, new long[]{838L}, "SITE", "_RST");
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

            // test action object for insert
            TestInsert(si, new Object[]{505.0D, null, null, "SSD", "DHALIWAL", null, "ROHN", null, "V4", 0L, null, "PRIMARY", null, "ESD MORICHES", null, null, null, null, null, null});

            // test action object for insert with multiple sites
            TestInsert(si, new long[]{830, 838}, new String[]{"SITE_ID", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, 4L, 2L});

            // test action object for insert with single site column change
            TestInsert(si, new long[]{111}, new String[]{"SITE_ID", "SITE", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, "DHALIWAL2", 4L, 2L});
        }
    }

    // test refresh for action object (no params)
    public void TestRefresh(ActionObject ao) {
        try {
            ao.Refresh(null, null);

            WebRowSet wrs = ao.getRowSet();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.size());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test refresh for action object using id
    public void TestRefresh(ActionObject ao, long id) {
        try {
            if (ao != null) {
                ao.Refresh(id);

                WebRowSet wrs = ao.getRowSet();
                System.out.println(ao.toXML());
                System.out.println(".. records selected: " + wrs.size());
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

                WebRowSet wrs = ao.getRowSet();
                System.out.println(ao.toXML());
                System.out.println(".. records selected: " + wrs.size());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    // test refresh for action object with whereclause/params
    public void TestRefresh(ActionObject ao, String whereClause, Object[] values) {
        try {
            ao.Refresh(whereClause, values);

            WebRowSet wrs = ao.getRowSet();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.size());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    // test refresh action object with whereclause overriding value types
    public void TestRefresh(ActionObject ao, String whereClause, DatabaseDataTypes[] valueDataTypes, Object[] values) {
        try {
            ao.Refresh(whereClause, valueDataTypes, values);

            WebRowSet wrs = ao.getRowSet();
            System.out.println(ao.toXML());
            System.out.println(".. records selected: " + wrs.size());
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
            ao.getRowSet().release();

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
            TestRefresh(ao, id);
            long recordNumber = ao.Insert(columns, values);
            System.out.println(ao.toXML());
            System.out.println(".. record# inserted: " + recordNumber);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ActionUnitTest aut = null;

        aut = new ActionUnitTest();

        aut.TestSystemIdentification();
        aut.TestSite();

        // class object direct access test
        if (aut.af != null) {
            try {
                WebRowSet wrs = ActionObjectDirect.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE_STATUS",
                        null, null, null);
                System.out.println(ActionObjectDirect.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"8%"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"9%"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.Cursor(aut.af.getDbManager(),
                        "NCS3.SPS_SITE",
                        new DatabaseDataTypes[]{DatabaseDataTypes.dtarray}, new Object[]{new Long[]{830L, 838L}});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE = ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"DHALIWAL2"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                long siteId = 830L;

                long count = ActionObjectDirect.Execute(aut.af.getDbManager(),
                        "NCS3.SPD_SITE",
                        new DatabaseDataTypes[]{DatabaseDataTypes.dtlong}, new Object[]{siteId});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records affected: " + count);
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
}
