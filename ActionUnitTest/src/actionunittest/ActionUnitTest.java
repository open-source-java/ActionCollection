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

        // get two records for the site object
        try {
            if (si != null) {
                TestRefresh(si, new long[]{838, 830});
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // get five records for the site object
        try {
            if (si != null) {
                TestRefresh(si, new long[]{838, 830, 111, 843, 792});
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // retrieve all records for site with no params
        try {
            if (si != null) {
                si.Refresh(null, null);

                WebRowSet wrs = si.getRowSet();
                System.out.println(si.toXML());
                System.out.println(".. records selected: " + wrs.size());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // filter site records for site_id with mask
        try {
            if (si != null) {
                si.Refresh("SITE_ID LIKE ?", new Object[]{"9%"});

                WebRowSet wrs = si.getRowSet();
                System.out.println(si.toXML());
                System.out.println(".. records selected: " + wrs.size());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // filter site records for site_id with match
        try {
            if (si != null) {
                si.Refresh("SITE_ID = ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtint}, new Object[]{909});

                WebRowSet wrs = si.getRowSet();
                System.out.println(si.toXML());
                System.out.println(".. records selected: " + wrs.size());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // multiple tests for update, delete and insert
        if (si != null) {
            try {
                // update record for site id
                TestRefresh(si, new long[]{838});

                WebRowSet wrs = si.getRowSet();
                System.out.println(si.toXML());

                wrs.beforeFirst();
                wrs.next();

                String siteName = wrs.getString("SITE") + "_RST";
                wrs.updateString("SITE", siteName);

                long count = si.Update(838);
                System.out.println(".. records updated: " + count);

                count = si.Update(838, new String[]{"SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{4L, 2L});
                System.out.println(si.toXML());
                System.out.println(".. records updated: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            // delete record for site id
            try {
                TestRefresh(si, 830);
                long count = si.Delete(830);
                System.out.println(si.toXML());
                System.out.println(".. records deleted: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            // delete records for multiple site ids
            try {
                TestRefresh(si, new long[]{830, 838});
                long count = si.Delete(new long[]{830, 838});
                System.out.println(si.toXML());
                System.out.println(".. records deleted: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            // delete record with filter
            try {
                long count = si.Delete("SITE LIKE ?", new Object[]{"DHALIWAL%"});
                System.out.println(si.toXML());
                System.out.println(".. records deleted: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                si.getRowSet().release();

                // insert new record from scratch
                //ANTENNA_HEIGHT,DT_CRTD,DT_UPDT,ICON_NAME,SITE,SITE_ANTENNA_ID,SITE_ANTENNA,SITE_CONFIG_ID,SITE_CONFIG,SITE_ID,SITE_TYPE_ID,SITE_TYPE,UNIT_ID,UNIT,CONTACT_ID,CONTACT,CITYSTATE_ID,CITY,STATE,ZIP
                long recordNumber = si.Insert(new Object[]{505.0D, null, null, "SSD", "DHALIWAL", null, "ROHN", null, "V4", 0L, null, "PRIMARY", null, "ESD MORICHES", null, null, null, null, null, null});
                System.out.println(si.toXML());
                System.out.println(".. record# inserted: " + recordNumber);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                // try to insert multiple records with minor changes
                TestRefresh(si, new long[]{830, 838});
                long recordNumber = si.Insert(new String[]{"SITE_ID", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, 4L, 2L});
                System.out.println(si.toXML());
                System.out.println(".. record# inserted: " + recordNumber);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                // try to insert record with change
                TestRefresh(si, new long[]{838});
                long recordNumber = si.Insert(new String[]{"SITE_ID", "SITE", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, "DHALIWAL2", 4L, 2L});
                System.out.println(si.toXML());
                System.out.println(".. record# inserted: " + recordNumber);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ActionUnitTest aut = new ActionUnitTest();

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

            WebRowSet wrs = null;
            try {
                wrs = ActionObjectDirect.View(aut.af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE = ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"DHALIWAL2"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                long siteId = 830L;

                if (wrs != null) {
                    wrs.beforeFirst();
                    while (wrs.next()) {
                        siteId = wrs.getLong("SITE_ID");
                        break;
                    }
                }

                long count = ActionObjectDirect.Execute(aut.af.getDbManager(),
                        "NCS3.SPD_SITE",
                        new DatabaseDataTypes[]{DatabaseDataTypes.dtlong}, new Object[]{siteId});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records affected: " + count);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
