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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ActionFactory af = null;

        try {
            af = new ActionFactory();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // class object test for one key
        if (af != null) {
            SystemIdentification si = null;

            try {
                si = (SystemIdentification) af.getClassByName("ac.factory.objects.SystemIdentification");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(1);

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        // class object test for multiple keys via cursor
        if (af != null) {
            Site si = null;

            try {
                si = (Site) af.getClassByName("ac.factory.objects.Site");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(new long[]{838, 830});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(new long[]{800, 801, 802, 803, 804});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        // class object test for multiple keys via cursor and custom filter
        if (af != null) {
            Site si = null;

            try {
                si = (Site) af.getClassByName("ac.factory.objects.Site");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(new long[]{838, 830});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(null, null);

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh("SITE_ID LIKE ?", new Object[]{"9%"});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh("SITE_ID = ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtint}, new Object[]{909});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        // class object test for update
        if (af != null) {
            Site si = null;

            try {
                si = (Site) af.getClassByName("ac.factory.objects.Site");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                if (si != null) {
                    si.Refresh(new long[]{838});

                    WebRowSet wrs = si.getRowSet();
                    System.out.println(si.toXML());

                    try {
                        wrs.setReadOnly(false);
                        wrs.beforeFirst();
                        wrs.next();

                        String siteName = wrs.getString("SITE") + "_RST";
                        wrs.updateString("SITE", siteName);

                        wrs.setReadOnly(true);
                        long count = si.Update(838);
                        System.out.println(".. records updated: " + count);

                        count = si.Update(838, new String[]{"SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{4L, 2L});
                        System.out.println(".. records updated: " + count);
                        //System.out.println(si.toXML());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    try {
                        si.Refresh(830);
                        long count = si.Delete(830);
                        System.out.println(".. records deleted: " + count);
                        //System.out.println(si.toXML());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    try {
                        si.Refresh(new long[]{830, 838});
                        long count = si.Delete(new long[]{830, 838});
                        System.out.println(".. records deleted: " + count);
                        //System.out.println(si.toXML());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    try {
                        long count = si.Delete("SITE LIKE ?", new Object[]{"DHALIWAL%"});
                        System.out.println(".. records deleted: " + count);
                        //System.out.println(si.toXML());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    try {
                        si.getRowSet().release();

                        //ANTENNA_HEIGHT,DT_CRTD,DT_UPDT,ICON_NAME,SITE,SITE_ANTENNA_ID,SITE_ANTENNA,SITE_CONFIG_ID,SITE_CONFIG,SITE_ID,SITE_TYPE_ID,SITE_TYPE,UNIT_ID,UNIT,CONTACT_ID,CONTACT,CITYSTATE_ID,CITY,STATE,ZIP
                        long recordNumber = si.Insert(new Object[]{505.0D, null, null, "SSD", "DHALIWAL", null, "ROHN", null, "V4", 0L, null, "PRIMARY", null, "ESD MORICHES", null, null, null, null, null, null});
                        System.out.println(si.toXML());
                        System.out.println(".. record# inserted: " + recordNumber);

                        si.Refresh(new long[]{830, 838});
                        recordNumber = si.Insert(new String[]{"SITE_ID", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, 4L, 2L});
                        System.out.println(si.toXML());
                        System.out.println(".. record# inserted: " + recordNumber);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    try {
                        si.Refresh(new long[]{838});
                        long recordNumber = si.Insert(new String[]{"SITE_ID", "SITE", "SITE_ANTENNA_ID", "SITE_CONFIG_ID"}, new Object[]{0L, "DHALIWAL2", 4L, 2L});
                        System.out.println(si.toXML());
                        System.out.println(".. record# inserted: " + recordNumber);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        // class object direct access test
        if (af != null) {
            try {
                WebRowSet wrs = ActionObjectDirect.View(af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE_STATUS",
                        null, null, null);
                System.out.println(ActionObjectDirect.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.View(af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"8%"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.View(af.getDbManager(),
                        "SELECT * FROM NCS3.vwSITE",
                        "SITE_ID LIKE ?", new DatabaseDataTypes[]{DatabaseDataTypes.dtstring}, new Object[]{"9%"});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                WebRowSet wrs = ActionObjectDirect.Cursor(af.getDbManager(),
                        "NCS3.SPS_SITE",
                        new DatabaseDataTypes[]{DatabaseDataTypes.dtarray}, new Object[]{new Long[]{830L, 838L}});
                //System.out.println(ActionObject.toXML(wrs));
                System.out.println(".. records selected: " + wrs.size());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            WebRowSet wrs = null;
            try {
                wrs = ActionObjectDirect.View(af.getDbManager(),
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

                long count = ActionObjectDirect.Execute(af.getDbManager(),
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
