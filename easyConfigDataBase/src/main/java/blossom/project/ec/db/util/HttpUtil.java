//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.util;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static AtomicInteger count = new AtomicInteger(0);

    public HttpUtil() {
    }

    public static String get(String url) {
        BufferedReader in = null;

        String rs;
        try {
            HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);
            connection.connect();
            rs = CommonUtil.read(connection.getInputStream());
            logger.debug("get dbcfg info:[{}]， response code:[{}]，content:[{}]", new Object[]{url, connection.getResponseCode(), CommonUtil.hidePassword(rs)});
            String var4 = rs;
            return var4;
        } catch (Exception var14) {
            if (count.incrementAndGet() == 5) {
                count.getAndSet(0);
                throw new RuntimeException(var14);
            }

            rs = null;
        } finally {
            try {
                if (in != null) {
                    ((BufferedReader)in).close();
                }
            } catch (Exception var13) {
                logger.warn("", var13);
            }

        }

        return rs;
    }
}
