package com.orientechnologies.orient.server.distributed;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.server.OServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleQueryDistributedTests {

  private OServer          server0;
  private OServer          server1;
  private OServer          server2;
  private OrientDB         remote;
  private ODatabaseSession session;

  @Before
  public void before() throws Exception {
    server0 = OServer.startFromClasspathConfig("orientdb-simple-dserver-config-0.xml");
    server1 = OServer.startFromClasspathConfig("orientdb-simple-dserver-config-1.xml");
    server2 = OServer.startFromClasspathConfig("orientdb-simple-dserver-config-2.xml");
    remote = new OrientDB("remote:localhost", "root", "test", OrientDBConfig.defaultConfig());
    remote.create("test", ODatabaseType.PLOCAL);
    session = remote.open("test", "admin", "admin");
  }

  @Test
  public void test() {
    OVertex vertex = session.newVertex("V");
    vertex.setProperty("name", "one");
    session.save(vertex);
    OResultSet res = session.query("select from V");
    assertTrue(res.hasNext());
    assertEquals(res.next().getProperty("name"), "one");
  }

  @Test
  public void testRecords() {
    int records = (OGlobalConfiguration.QUERY_REMOTE_RESULTSET_PAGE_SIZE.getValueAsInteger() + 10);
    for (int i = 0; i < records; i++) {
      OVertex vertex = session.newVertex("V");
      vertex.setProperty("name", "one");
      vertex.setProperty("pos", i);
      session.save(vertex);
    }

    OResultSet res = session.query("select from V order by pos");
    for (int i = 0; i < records; i++) {
      assertTrue(res.hasNext());
      OResult ele = res.next();
      assertEquals((int) ele.getProperty("pos"), i);
      assertEquals(ele.getProperty("name"), "one");
    }
  }

  @After
  public void after() {
    remote.drop("test");
    remote.close();

    server0.shutdown();
    server1.shutdown();
    server2.shutdown();
  }

}