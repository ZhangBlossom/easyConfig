//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.util;

import blossom.project.ec.db.config.DataSourceType;
import blossom.project.ec.db.datasource.BasicDataSource;
import blossom.project.ec.db.datasource.DelegatingDataSource;
import blossom.project.ec.db.exception.DataSourceException;
import blossom.project.ec.db.manager.TransactionContextManager;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
    private static final String ENV_LOCAL = "local";
    private static final String ENV_CUSTOMIZE = "customize";
    private static final String MYSQL_UTF8 = "utf8mb4";
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 60;
    private static final int DEFAULT_SOCKET_TIMEOUT_SECONDS = 120;
    private static final String DEFAULT_MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private static final String DEFAULT_SQLSERVER_DRIVER_CLASS_NAME = "net.sourceforge.jtds.jdbc.Driver";
    private static final String CONNECTION_PROP_DRIVER_CLASS_NAME = "driverClassName";
    private static final String CONNECTION_PROP_CONNECT_TIMEOUT_MYSQL = "connectTimeout";
    private static final String CONNECTION_PROP_SOCKET_TIMEOUT = "socketTimeout";
    private static final String DEFAULT_CONNECT_TIMEOUT_MYSQL = "60000";
    private static final String DEFAULT_SOCKET_TIMEOUT_MYSQL = "120000";
    private static final String CONNECTION_PROP_CONNECT_TIMEOUT_MSSQL = "loginTimeout";
    private static final String DEFAULT_CONNECT_TIMEOUT_MSSQL = "60";
    private static final String DEFAULT_SOCKET_TIMEOUT_MSSQL = "120";
    private static Map<String, AtomicInteger> allDataSourceMap = new ConcurrentHashMap();
    private static Map<String, TransactionContextManager> contextManagerMap = new ConcurrentHashMap();

    public CommonUtil() {
    }

    public static boolean isLocalEnv(String env) {
        return "local".equals(env);
    }

    public static boolean isCustomizeEnv(String env) {
        return "customize".equals(env);
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkSqlWhereCondition(String sql, String dbType) {
        sql = preCheckDeleteSqlWhereConditionForSqlServer(sql, dbType);
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
        Iterator var3 = statementList.iterator();

        while(var3.hasNext()) {
            SQLStatement sqlStatement = (SQLStatement)var3.next();
            if (sqlStatement instanceof SQLUpdateStatement) {
                SQLUpdateStatement statement = (SQLUpdateStatement)sqlStatement;
                if (statement.getWhere() == null) {
                    throw new DataSourceException("can not find where condition in sql: [" + sql + "]");
                }
            }

            if (sqlStatement instanceof SQLDeleteStatement) {
                SQLDeleteStatement statement = (SQLDeleteStatement)sqlStatement;
                if (statement.getWhere() == null) {
                    throw new DataSourceException("can not find where condition in sql: [" + sql + "]");
                }
            }
        }

    }

    public static String preCheckDeleteSqlWhereConditionForSqlServer(String sql, String dbType) {
        String tempSql = sql.trim().toLowerCase();
        if (dbType.equals("sqlserver") && tempSql.startsWith("delete")) {
            int fromIndex = tempSql.indexOf("from");
            if (fromIndex != -1) {
                String deleteStr = tempSql.substring(6, fromIndex);
                tempSql = tempSql.replace(deleteStr, " ");
                return tempSql;
            }
        }

        return sql;
    }

    public static String getLocalAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList();
            ArrayList<String> ipv6Result = new ArrayList();

            while(enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface)enumeration.nextElement();
                Enumeration<InetAddress> en = networkInterface.getInetAddresses();

                while(en.hasMoreElements()) {
                    InetAddress address = (InetAddress)en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }

            Collections.sort(ipv4Result);
            Collections.sort(ipv6Result);
            if (!ipv4Result.isEmpty()) {
                Iterator var9 = ipv4Result.iterator();

                String ip;
                do {
                    if (!var9.hasNext()) {
                        return (String)ipv4Result.get(ipv4Result.size() - 1);
                    }

                    ip = (String)var9.next();
                } while(ip.endsWith(".0.1"));

                return ip;
            }

            if (!ipv6Result.isEmpty()) {
                return (String)ipv6Result.get(0);
            }

            InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (SocketException var6) {
            var6.printStackTrace();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return null;
    }

    public static String normalizeHostAddress(InetAddress localHost) {
        return localHost instanceof Inet6Address ? "[" + localHost.getHostAddress() + "]" : localHost.getHostAddress();
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(String obj) {
        return obj == null || obj.trim().isEmpty();
    }

    public static String getStackTrace(Throwable throwable) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    public static String read(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = null;

            while((line = br.readLine()) != null) {
                result.append(line);
            }

            String var4 = result.toString();
            return var4;
        } catch (IOException var8) {
            throw var8;
        } finally {
            if (br != null) {
                br.close();
                br = null;
            }

        }
    }

    public static Class getBasicType(Object o) {
        if (o instanceof Integer) {
            return Integer.TYPE;
        } else if (o instanceof Boolean) {
            return Boolean.TYPE;
        } else if (o instanceof Float) {
            return Float.TYPE;
        } else if (o instanceof Double) {
            return Double.TYPE;
        } else if (o instanceof Long) {
            return Long.TYPE;
        } else if (o instanceof Short) {
            return Short.TYPE;
        } else if (o instanceof Byte) {
            return Byte.TYPE;
        } else {
            return o instanceof Character ? Character.TYPE : o.getClass();
        }
    }

    public static Element checkXml(String xml, String dbName) {
        Element usedDbElement = null;
        int count = 0;

        try {
            Document document = DocumentHelper.parseText(xml);
            Iterator<Element> dbElements = document.getRootElement().elementIterator("database");

            while(dbElements.hasNext()) {
                Element dbElement = (Element)dbElements.next();
                String dataSourceName = dbElement.attributeValue("name");
                if (dataSourceName.equals(dbName)) {
                    ++count;
                    usedDbElement = dbElement;
                }
            }
        } catch (Exception var8) {
            String msg = String.format("dal-new, parse config error=%s，xml=%s", var8.getMessage(), xml);
            throw new DataSourceException(msg, var8);
        }

        if (count == 0) {
            throw new DataSourceException("dal-new, no database found in config, dbName=" + dbName);
        } else if (count > 1) {
            throw new DataSourceException("dal-new, Multiple databases are configured, dbName=" + dbName);
        } else {
            return usedDbElement;
        }
    }

    public static void logWarnMultiInit(boolean isInit, String dbName) {
        if (isInit) {
            AtomicInteger initCount = (AtomicInteger)allDataSourceMap.get(dbName);
            if (initCount == null) {
                initCount = new AtomicInteger(1);
                allDataSourceMap.put(dbName, initCount);
            } else {
                initCount.incrementAndGet();
            }
        }

        Iterator var4 = allDataSourceMap.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, AtomicInteger> entry = (Map.Entry)var4.next();
            if (((AtomicInteger)entry.getValue()).get() > 1) {
                logger.warn("multi【{}】database are init!", entry.getKey());
            }
        }

    }

    public static String md5(Element dbElement) {
        Element readEle = dbElement.element("read");
        Element writeEle = dbElement.element("write");
        List<Element> readServerList = new ArrayList();
        if (readEle != null) {
            readServerList = readEle.elements();
        }

        List<Element> writeServerList = new ArrayList();
        if (writeEle != null) {
            writeServerList = writeEle.elements();
        }

        StringBuilder dataSourceText = new StringBuilder();
        Iterator var6 = ((List)readServerList).iterator();

        Element write;
        List writeAttributeList;
        Iterator var9;
        Attribute attr;
        while(var6.hasNext()) {
            write = (Element)var6.next();
            writeAttributeList = write.attributes();
            var9 = writeAttributeList.iterator();

            while(var9.hasNext()) {
                attr = (Attribute)var9.next();
                dataSourceText.append(write.attributeValue(attr.getName()));
            }
        }

        var6 = ((List)writeServerList).iterator();

        while(var6.hasNext()) {
            write = (Element)var6.next();
            writeAttributeList = write.attributes();
            var9 = writeAttributeList.iterator();

            while(var9.hasNext()) {
                attr = (Attribute)var9.next();
                dataSourceText.append(write.attributeValue(attr.getName()));
            }
        }

        String text = dataSourceText.toString();
        return StringEncrypt.md5String(text);
    }

    public static void savecontextManager(String dataSourceName, TransactionContextManager contextManager) {
        contextManagerMap.put(dataSourceName, contextManager);
    }

    public static TransactionContextManager getcontextManager(String dataSourceName) {
        return (TransactionContextManager)contextManagerMap.get(dataSourceName);
    }

    public static Map<String, List<DelegatingDataSource>> createReadAndWriteDataSource(Element dbElement, BasicDataSource userCfgDS) {
        Map<String, List<DelegatingDataSource>> dataSourceMap = new ConcurrentHashMap();
        List<DelegatingDataSource> readDataSource = new ArrayList();
        List<DelegatingDataSource> writeDataSource = new ArrayList();

        Iterator it;
        Element writeElements;
        Element server;
        BasicDataSource newDS;
        try {
            writeElements = dbElement.element("read");
            if (writeElements != null) {
                it = writeElements.elementIterator("server");

                while(it.hasNext()) {
                    server = (Element)it.next();
                    if (Boolean.parseBoolean(server.attributeValue("enabled"))) {
                        newDS = userCfgDS.clone();
                        parseSingleServerCfg(server, newDS, userCfgDS);
                        if (!isNullOrEmpty(newDS.getUrl())) {
                            readDataSource.add(DelegatingDataSource.newInstance(newDS, DataSourceType.READ));
                        }
                    }
                }
            }

            if (isNullOrEmpty((Collection)readDataSource)) {
                logger.warn("no read db from {} config", userCfgDS.getDbName());
            }

            dataSourceMap.put(DataSourceType.READ.name(), readDataSource);
        } catch (Exception var10) {
            logger.error("解析Read Server节点出错:{}", var10);
            if (var10 instanceof DataSourceException) {
                throw (DataSourceException)var10;
            }

            throw new DataSourceException(var10);
        }

        try {
            writeElements = dbElement.element("write");
            if (writeElements != null) {
                it = writeElements.elementIterator("server");

                while(it.hasNext()) {
                    server = (Element)it.next();
                    if (Boolean.parseBoolean(server.attributeValue("enabled"))) {
                        newDS = userCfgDS.clone();
                        parseSingleServerCfg(server, newDS, userCfgDS);
                        if (!isNullOrEmpty(newDS.getUrl())) {
                            writeDataSource.add(DelegatingDataSource.newInstance(newDS, DataSourceType.WRITE));
                        }
                    }
                }
            }

            if (isNullOrEmpty((Collection)writeDataSource)) {
                logger.warn("no write db from {} config", userCfgDS.getDbName());
            }

            dataSourceMap.put(DataSourceType.WRITE.name(), writeDataSource);
            return dataSourceMap;
        } catch (Exception var9) {
            logger.error("解析Write Server节点出错:{}", var9);
            if (var9 instanceof DataSourceException) {
                throw (DataSourceException)var9;
            } else {
                throw new DataSourceException(var9);
            }
        }
    }

    private static void parseSingleServerCfg(Element server, BasicDataSource newDS, BasicDataSource userCfgDS) {
        try {
            String encrypt = server.attributeValue("encrypt");
            String connectionString = server.attributeValue("connectionString");
            String provider = server.attributeValue("provider");
            Map<String, String> connInfoMap = parseConnectionString(connectionString);
            newDS.setUsername((String)connInfoMap.get("User ID"));
            if (Boolean.valueOf(encrypt)) {
                newDS.setPassword(StringEncrypt.decrypt((String)connInfoMap.get("Password"), newDS.getProjectId()));
            } else {
                newDS.setPassword((String)connInfoMap.get("Password"));
            }

            newDS.setDbName((String)connInfoMap.get("Initial Catalog"));
            String minPoolSize = (String)connInfoMap.get("minPoolSize");
            if (!isNullOrEmpty(minPoolSize)) {
                newDS.setMinIdle(Integer.valueOf(minPoolSize));
            }

            String maxPoolSize = (String)connInfoMap.get("maxPoolSize");
            if (!isNullOrEmpty(maxPoolSize)) {
                newDS.setMaxActive(Integer.valueOf(maxPoolSize));
            }

            String charset = (String)connInfoMap.get("charset");
            if (isNullOrEmpty(charset) || "utf8mb4".equals(charset)) {
                charset = "utf8";
            }

            String useUnicode = (String)connInfoMap.get("useUnicode");
            if (isNullOrEmpty(useUnicode)) {
                useUnicode = "true";
            }

            if (!isNullOrEmpty(provider)) {
                StringBuilder url = new StringBuilder();
                String[] ip_port = ((String)connInfoMap.get("Data Source")).split(",");
                String ip = ip_port[0];
                String port = "";
                if (ip_port.length > 1) {
                    port = ip_port[1];
                }

                Map<String, String> jdbcConnectionProperties = userCfgDS.getJdbcConnectionProperties();
                if (jdbcConnectionProperties == null) {
                    jdbcConnectionProperties = new HashMap();
                }

                Iterator var16;
                Map.Entry entry;
                if (provider.equals("MySql.Data.MySqlClient")) {
                    userCfgDS.setDbType("mysql");
                    if (((Map)jdbcConnectionProperties).containsKey("driverClassName")) {
                        newDS.setDriverClassName((String)((Map)jdbcConnectionProperties).get("driverClassName"));
                    } else {
                        newDS.setDriverClassName("com.mysql.jdbc.Driver");
                    }

                    if (isNullOrEmpty(port)) {
                        port = "3306";
                    }

                    url.append("jdbc:mysql://").append(ip).append(":").append(port).append("/").append(newDS.getDbName()).append("?").append("useUnicode=").append(useUnicode).append("&").append("characterEncoding=").append(charset).append("&").append("autoReconnect=true");
                    if (!((Map)jdbcConnectionProperties).containsKey("connectTimeout")) {
                        ((Map)jdbcConnectionProperties).put("connectTimeout", "60000");
                    }

                    if (!((Map)jdbcConnectionProperties).containsKey("socketTimeout")) {
                        ((Map)jdbcConnectionProperties).put("socketTimeout", "120000");
                    }

                    if (jdbcConnectionProperties != null && ((Map)jdbcConnectionProperties).size() > 0) {
                        var16 = ((Map)jdbcConnectionProperties).entrySet().iterator();

                        while(var16.hasNext()) {
                            entry = (Map.Entry)var16.next();
                            url.append("&").append((String)entry.getKey()).append("=").append((String)entry.getValue());
                        }
                    }
                } else if (provider.equals("System.Data.SqlClient")) {
                    userCfgDS.setDbType("sqlserver");
                    if (((Map)jdbcConnectionProperties).containsKey("driverClassName")) {
                        newDS.setDriverClassName((String)((Map)jdbcConnectionProperties).get("driverClassName"));
                    } else {
                        newDS.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
                    }

                    if (isNullOrEmpty(port)) {
                        port = "1433";
                    }

                    url.append("jdbc:jtds:sqlserver://").append(ip).append(":").append(port).append(";DatabaseName=").append(newDS.getDbName()).append(";").append("useUnicode=").append(useUnicode).append(";").append("characterEncoding=").append(charset).append("&").append("autoReconnect=true");
                    if (!((Map)jdbcConnectionProperties).containsKey("loginTimeout")) {
                        ((Map)jdbcConnectionProperties).put("loginTimeout", "60");
                    }

                    if (!((Map)jdbcConnectionProperties).containsKey("socketTimeout")) {
                        ((Map)jdbcConnectionProperties).put("socketTimeout", "120");
                    }

                    if (jdbcConnectionProperties != null && ((Map)jdbcConnectionProperties).size() > 0) {
                        var16 = ((Map)jdbcConnectionProperties).entrySet().iterator();

                        while(var16.hasNext()) {
                            entry = (Map.Entry)var16.next();
                            url.append("&").append((String)entry.getKey()).append("=").append((String)entry.getValue());
                        }
                    }
                }

                newDS.setUrl(url.toString());
                String jtaEnabledStr = (String)connInfoMap.get("jtaEnabled");
                if (jtaEnabledStr != null) {
                    newDS.setJtaEnabled(Boolean.valueOf(jtaEnabledStr));
                }
            }

        } catch (Exception var18) {
            logger.error("解析Server节点出错!", var18);
            if (var18 instanceof DataSourceException) {
                throw (DataSourceException)var18;
            } else {
                throw new DataSourceException(var18);
            }
        }
    }

    private static Map<String, String> parseConnectionString(String connStr) {
        Map<String, String> map = new HashMap();
        Map<String, String> oldMap = new HashMap();
        String[] kvs = connStr.split(";");
        String[] var4 = kvs;
        int var5 = kvs.length;

        String userName;
        for(int var6 = 0; var6 < var5; ++var6) {
            userName = var4[var6];
            int index = userName.indexOf("=");
            map.put(userName.substring(0, index).trim().toLowerCase(), userName.substring(index + 1));
            oldMap.put(userName.substring(0, index).trim(), userName.substring(index + 1));
        }

        if (map.containsKey("server")) {
            String ip = (String)map.get("server");
            String port = (String)map.get("port");
            String db = (String)map.get("database");
            userName = (String)map.get("user id");
            String passwd = (String)map.get("password");
            String charset = (String)map.get("charset");
            String useunicode = (String)map.get("useunicode");
            String minPoolSize = (String)map.get("min pool size");
            String maxPoolSize = (String)map.get("max pool size");
            String jtaEnabled = (String)map.get("jta enabled");
            map.clear();
            map.put("Data Source", ip + "," + port);
            map.put("Initial Catalog", db);
            map.put("User ID", userName);
            map.put("Password", passwd);
            map.put("charset", charset);
            map.put("useunicode", useunicode);
            map.put("minPoolSize", minPoolSize);
            map.put("maxPoolSize", maxPoolSize);
            map.put("jtaEnabled", jtaEnabled);
            return map;
        } else {
            oldMap.put("minPoolSize", oldMap.get("Min Pool Size"));
            oldMap.put("maxPoolSize", oldMap.get("Max Pool Size"));
            return oldMap;
        }
    }

    public static long getObjectHashCode(Object obj) {
        return (long)System.identityHashCode(obj);
    }

    public static Map<String, String> getDataSourceInfo(DruidPooledConnection dpc) {
        String jdbcUrl = dpc.getConnectionHolder().getDataSource().getRawJdbcUrl();
        return getDataSourceInfo(jdbcUrl);
    }

    public static Map<String, String> getDataSourceInfo(String jdbcUrl) {
        if (jdbcUrl.contains("jdbc:mysql://")) {
            return getMysqlDataSourceInfo(jdbcUrl);
        } else {
            return jdbcUrl.contains("jdbc:sqlserver://") ? getSqlServerDataSourceInfo(jdbcUrl) : null;
        }
    }

    public static Map<String, String> getMysqlDataSourceInfo(String jdbcUrl) {
        jdbcUrl = jdbcUrl.substring(jdbcUrl.indexOf("jdbc:mysql://") + "jdbc:mysql://".length(), jdbcUrl.indexOf("?"));
        String ipAndPort = jdbcUrl.substring(0, jdbcUrl.indexOf("/"));
        String dbName = jdbcUrl.substring(jdbcUrl.indexOf("/") + 1);
        Map<String, String> map = new HashMap(2);
        map.put("ipAndPort", ipAndPort);
        map.put("dbName", dbName);
        return map;
    }

    public static Map<String, String> getSqlServerDataSourceInfo(String jdbcUrl) {
        jdbcUrl = jdbcUrl.substring(jdbcUrl.indexOf("jdbc:sqlserver://") + "jdbc:sqlserver://".length(), jdbcUrl.indexOf("useUnicode"));
        String ipAndPort = jdbcUrl.substring(0, jdbcUrl.indexOf(";"));
        int dbEnd = jdbcUrl.length() - 1;
        int dbStart = jdbcUrl.indexOf("=") + 1;
        String dbName = jdbcUrl.substring(dbStart, dbEnd);
        Map<String, String> map = new HashMap(2);
        map.put("ipAndPort", ipAndPort);
        map.put("dbName", dbName);
        return map;
    }

    public static String hidePassword(String xmlString) {
        if (xmlString == null) {
            return null;
        } else {
            try {
                return xmlString.replaceAll("Password=\\w+;", "Password=******;");
            } catch (Exception var2) {
                return xmlString;
            }
        }
    }

    public static String getShortName(String name, int maxLength) {
        if (name != null && name.length() > maxLength) {
            StringBuilder shortName = new StringBuilder();
            String[] segments = name.split("\\.");

            for(int i = 0; i < segments.length - 1; ++i) {
                shortName.append(segments[i].charAt(0)).append('.');
            }

            shortName.append(segments[segments.length - 1]);
            return shortName.length() < maxLength ? shortName.toString() : shortName.substring(0, maxLength);
        } else {
            return name;
        }
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
