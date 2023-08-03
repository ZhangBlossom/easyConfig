//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.main;

import blossom.project.ec.db.datasource.AcceptableDataSource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws Exception {
        String dbName = "towelove";
        final AcceptableDataSource ds = new AcceptableDataSource();
        ds.setDbName(dbName);
        ds.init();
        List<Thread> tList = new ArrayList();

        Thread t;
        for(int i = 0; i < 3; ++i) {
            t = new Thread(new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            Connection conn = ds.getConnection();
                            String sql = "select * from face_pic where 1=1 limit 1";
                            Statement statement = conn.prepareStatement(sql);
                            statement.executeQuery(sql);
                            System.out.println("111");
                            Thread.sleep(3000L);
                            conn.close();
                        } catch (Throwable var5) {
                            var5.printStackTrace();

                            try {
                                Thread.sleep(3000L);
                            } catch (InterruptedException var4) {
                                var4.printStackTrace();
                            }
                        }
                    }
                }
            });
            tList.add(t);
        }

        Iterator var6 = tList.iterator();

        while(var6.hasNext()) {
            t = (Thread)var6.next();
            t.start();
        }

    }
}
