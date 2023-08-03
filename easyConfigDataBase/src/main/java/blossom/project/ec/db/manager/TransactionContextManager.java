//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package blossom.project.ec.db.manager;

import java.sql.Connection;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import blossom.project.ec.client.config.AppProperties;
import blossom.project.ec.db.cfgcenter.DBConfigCenterService;
import blossom.project.ec.db.config.DataSourceType;
import blossom.project.ec.db.datasource.AcceptableDataSource;
import blossom.project.ec.db.datasource.DelegatingConnection;
import blossom.project.ec.db.datasource.DelegatingDataSource;
import blossom.project.ec.db.datasource.RealConnectionStatsInfo;
import blossom.project.ec.db.exception.DataSourceException;
import blossom.project.ec.db.util.CommonUtil;
import blossom.project.ec.db.util.DateUtil;
import blossom.project.ec.db.util.SQLParserUtil;
import com.alibaba.druid.pool.DruidDataSource;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionContextManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionContextManager.class);
    private PathTrackManager pathTrackManager;
    private AcceptableDataSource dataSource;
    private Map<DataSourceType, List<DelegatingDataSource>> availableDataSourceTable;
    private Map<DataSourceType, AtomicLong> roundRobinTable;
    private String currentCfgInfoMd5;
    private ThreadLocal<IdentityHashMap<Object, TransactionContext>> threadTransactionContext;
    private ThreadLocal<DataSourceType> threadDataSourceTypeForce;
    private DBConfigCenterService client;
    private AbandonedDataSourceManager abandonedDataSourceManager;
    private Map<Long, RealConnectionStatsInfo> activeConnections;
    private Map<String, ConfigModel> dataSourceMap = new ConcurrentHashMap();

    public TransactionContextManager(AcceptableDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() {
        this.activeConnections = new ConcurrentHashMap();
        this.pathTrackManager = new PathTrackManager();
        this.availableDataSourceTable = new ConcurrentHashMap();
        this.threadTransactionContext = new ThreadLocal();
        this.threadDataSourceTypeForce = new ThreadLocal();
        this.roundRobinTable = new ConcurrentHashMap();
        this.roundRobinTable.put(DataSourceType.READ, new AtomicLong(0L));
        this.roundRobinTable.put(DataSourceType.WRITE, new AtomicLong(0L));
        this.abandonedDataSourceManager = new AbandonedDataSourceManager();

        try {
            this.client = new DBConfigCenterService(this, AppProperties.getProjectName());
            this.client.start();
        } catch (Exception var2) {
            logger.error("配置中心客户端启动失败", var2);
            throw var2;
        }

        this.abandonedDataSourceManager.start();
    }

    public AcceptableDataSource getDataSource() {
        return this.dataSource;
    }

    public void doRegisterTransaction(DelegatingConnection delegtConn) {
        TransactionContext tr = new TransactionContext();
        tr.setDelegatingConnection(delegtConn);
        IdentityHashMap<Object, TransactionContext> transactionMap = (IdentityHashMap)this.threadTransactionContext.get();
        if (transactionMap == null) {
            transactionMap = new IdentityHashMap();
            this.threadTransactionContext.set(transactionMap);
        }

        transactionMap.put(delegtConn, tr);
    }

    public void unDoRegisterTransaction(DelegatingConnection delegtConn) {
        IdentityHashMap<Object, TransactionContext> transactionMap = (IdentityHashMap)this.threadTransactionContext.get();
        if (transactionMap != null) {
            TransactionContext tc = (TransactionContext)transactionMap.remove(delegtConn);
            if (tc != null) {
                this.removeActiveConnection(tc.getRealConnection());
                tc.destroy();
            }

            if (transactionMap.isEmpty()) {
                this.threadTransactionContext.remove();
                this.threadDataSourceTypeForce.remove();
            }

        }
    }

    public void forceToUseDataSourceType(DataSourceType dsType) {
        this.threadDataSourceTypeForce.set(dsType);
    }

    public void holdRealConnection(DelegatingConnection delegtConn, String sql) {
        this.holdRealConnection(delegtConn, SQLParserUtil.parseSQLType(sql));
    }

    public void holdRealConnection(DelegatingConnection delegtConn, DataSourceType dsType) {
        TransactionContext tr = this.getTransactionContext(delegtConn);
        if (!tr.isInitCompleted()) {
            try {
                if (this.threadDataSourceTypeForce.get() != null) {
                    dsType = (DataSourceType)this.threadDataSourceTypeForce.get();
                } else if (!tr.getDelegatingConnection().getAutoCommit()) {
                    dsType = DataSourceType.WRITE;
                }

                Connection realConnection = this.retryGetRealConnection(dsType);
                tr.setRealConnection(realConnection);
                this.pathTrackManager.doPathChain(tr);
            } catch (Exception var5) {
                logger.error("hold Real Connection failed!", var5);
                if (var5 instanceof DataSourceException) {
                    throw (DataSourceException)var5;
                } else {
                    throw new DataSourceException(var5);
                }
            }
        }
    }

    private Connection retryGetRealConnection(DataSourceType dsType) {
        Connection realConnection = null;

        try {
            realConnection = this.doGetRealConnection(dsType);
            if (realConnection != null) {
                return realConnection;
            }
        } catch (Exception var5) {
            logger.error("can not get a real connection for {} datasource!", dsType, var5);
            if (DataSourceType.WRITE.equals(dsType)) {
                if (var5 instanceof DataSourceException) {
                    throw new DataSourceException("At least one write datasource needs to be configured for " + this.dataSource.getDbName());
                }

                throw new DataSourceException("can not get a real connection for " + dsType + " datasource!", var5);
            }
        }

        logger.error("now switch from READ to WRITE datasource");
        dsType = DataSourceType.WRITE;

        try {
            realConnection = this.doGetRealConnection(dsType);
            return realConnection;
        } catch (Exception var4) {
            logger.error("can not get a real connection for {} datasource!", dsType, var4);
            if (var4 instanceof DataSourceException) {
                throw var4;
            } else {
                throw new DataSourceException("can not get a real connection for " + dsType + " datasource!", var4);
            }
        }
    }

    private Connection doGetRealConnection(DataSourceType dsType) {
        List<DelegatingDataSource> availDSList = (List)this.availableDataSourceTable.get(dsType);
        if (CommonUtil.isNullOrEmpty(availDSList)) {
            logger.error(dsType + " datasource list " + this.dataSource.getDbName() + " is empty!");
            if (availDSList == null) {
                throw new DataSourceException("cannot get " + dsType + " datasource on " + this.dataSource.getDbName());
            } else {
                throw new DataSourceException(dsType + " datasource list on " + this.dataSource.getDbName() + " is empty!");
            }
        } else {
            int loop = 0;
            AtomicLong roundRobinCounter = (AtomicLong)this.roundRobinTable.get(dsType);
            Connection realCon = null;
            Exception exp = null;
            String expMsg = "";

            while(loop < availDSList.size()) {
                int index = (int)(roundRobinCounter.getAndIncrement() % (long)availDSList.size());
                DelegatingDataSource dds = (DelegatingDataSource)availDSList.get(index);

                try {
                    realCon = dds.getRealDataSource().getConnection();
                    this.traceActiveConnection(realCon);
                    exp = null;
                    expMsg = null;
                    break;
                } catch (Exception var11) {
                    logger.error("get the real connection failed for the datasource : {}, the error is : ", dds, var11);
                    logger.error(this.statsConnectionPoolInfo(dds.getUnderlyingDruidDataSource(), true));
                    logger.error("the {} datasource list : {}", dsType, availDSList);
                    realCon = null;
                    exp = var11;
                    expMsg = "[DSInfo]" + dds.getBasicDataSource().getUrl() + "[/DSInfo]";
                    ++loop;
                }
            }

            if (exp != null) {
                throw new DataSourceException(expMsg, exp);
            } else {
                return realCon;
            }
        }
    }

    public TransactionContext getTransactionContext(DelegatingConnection delegtConn) {
        IdentityHashMap<Object, TransactionContext> transactionMap = (IdentityHashMap)this.threadTransactionContext.get();
        return !CommonUtil.isNullOrEmpty(transactionMap) ? (TransactionContext)transactionMap.get(delegtConn) : null;
    }

    /**
     * 后续的更新以及第一次的初始化都是使用这个方法对数据库进行变更
     * @param xml 数据库配置文件
     * @param isInit 是否初始化过
     */
    public void updateDataSources(String xml, boolean isInit) {
        if (!CommonUtil.isNullOrEmpty(xml)) {
            String dbName = this.dataSource.getDbName();
            Element dbElement = CommonUtil.checkXml(xml, dbName);
            String newMd5 = CommonUtil.md5(dbElement);
            String oldMd5 = null;
            ConfigModel oldConfig = (ConfigModel)this.dataSourceMap.get(dbName);
            if (oldConfig != null) {
                oldMd5 = oldConfig.md5;
            }

            if (newMd5.equals(oldMd5)) {
                logger.info("There is no change:" + dbName);
            } else {
                logger.info("开始配置变更");
                Map<String, List<DelegatingDataSource>> updateMap = CommonUtil.createReadAndWriteDataSource(dbElement, this.dataSource);
                this.updateDataSources(updateMap);
                //根据数据源名称保存对应的事务管理器
                CommonUtil.savecontextManager(dbName, this);
                ConfigModel newConfig = new ConfigModel(dbName, xml, newMd5);
                this.dataSourceMap.put(dbName, newConfig);
                logger.info("配置变更完毕,\nbefore:\n {} \nafter: \n {}", oldConfig, newConfig);
                CommonUtil.logWarnMultiInit(isInit, dbName);
            }
        } else {
            throw new DataSourceException("database config is empty!");
        }
    }

    private void updateDataSources(Map<String, List<DelegatingDataSource>> updateMap) {
        List<DelegatingDataSource> newReadList = (List)updateMap.get(DataSourceType.READ.name());
        List<DelegatingDataSource> newWriteList = (List)updateMap.get(DataSourceType.WRITE.name());
        List<DelegatingDataSource> abandonedReadDS = (List)this.availableDataSourceTable.get(DataSourceType.READ);
        List<DelegatingDataSource> abandonedWriteDS = (List)this.availableDataSourceTable.get(DataSourceType.WRITE);
        logger.info("[{}] 更新数据库配置前状态信息：{}", this.dataSource.getProjectId(), this.availableDataSourceTable);
        this.availableDataSourceTable.put(DataSourceType.READ, newReadList);
        this.availableDataSourceTable.put(DataSourceType.WRITE, newWriteList);
        logger.info("[{}] 更新数据库配置后状态信息：{}", this.dataSource.getProjectId(), this.availableDataSourceTable);
        this.abandonedDataSourceManager.putAbandonedDataSource(abandonedReadDS);
        this.abandonedDataSourceManager.putAbandonedDataSource(abandonedWriteDS);
    }

    public PathTrackManager getPathTrackManager() {
        return this.pathTrackManager;
    }

    public boolean isInitDBCfg() {
        return this.availableDataSourceTable != null && (!CommonUtil.isNullOrEmpty((Collection)this.availableDataSourceTable.get(DataSourceType.READ)) || !CommonUtil.isNullOrEmpty((Collection)this.availableDataSourceTable.get(DataSourceType.WRITE)));
    }

    public void destroy() {
        if (!CommonUtil.isNullOrEmpty(this.availableDataSourceTable)) {
            Iterator var1 = this.availableDataSourceTable.values().iterator();

            while(var1.hasNext()) {
                List<DelegatingDataSource> ddsList = (List)var1.next();
                Iterator var3 = ddsList.iterator();

                while(var3.hasNext()) {
                    DelegatingDataSource dds = (DelegatingDataSource)var3.next();
                    dds.close();
                }
            }

            this.availableDataSourceTable.clear();
            this.availableDataSourceTable = null;
        }

        if (this.client != null) {
            this.client.destroy();
            this.client = null;
        }

        this.currentCfgInfoMd5 = null;
        this.dataSource = null;
        this.pathTrackManager = null;
        if (this.roundRobinTable != null) {
            this.roundRobinTable.clear();
            this.roundRobinTable = null;
        }

        if (this.abandonedDataSourceManager != null) {
            this.abandonedDataSourceManager.close();
            this.abandonedDataSourceManager = null;
        }

    }

    private String statsConnectionPoolInfo(DruidDataSource ds, boolean withDetail) {
        StringBuilder info = new StringBuilder();
        info.append("The Alibaba DruidDataSource Info : ").append("{").append("\n\t当前链接池名称[DataSourceName]: ").append(ds.getName()).append("\n\t当前链接池配置的最大链接为: ").append(ds.getMaxActive()).append("\n\t当前池中未用的连接数[poolingCount]: ").append(ds.getPoolingCount()).append("\n\t当前上层应用正在使用的连接数[activeCount]: ").append(ds.getActiveCount()).append("\n\t截止目前池中未用的连接数峰值[poolingPeak]: ").append(ds.getPoolingPeak()).append("\n\t池中未用的连接数峰值出现的时间点为：");
        if (ds.getPoolingPeakTime() != null) {
            info.append(DateUtil.date2String(ds.getPoolingPeakTime()));
        }

        info.append("\n\t截止目前上层应用使用的连接数峰值[activePeak]: ").append(ds.getActivePeak()).append("\n\t上层应用使用的连接数峰值出现的时间点为：");
        if (ds.getActivePeakTime() != null) {
            info.append(DateUtil.date2String(ds.getActivePeakTime()));
        }

        info.append("\n\t截止目前连接池创建连接时错误数：").append(ds.getCreateErrorCount()).append("\n\t截止目前连接池回收连接时错误数：").append(ds.getRecycleErrorCount());
        if (withDetail) {
            info.append("\n\n\t当前未释放的连接信息：\n");
            Iterator var4 = this.activeConnections.values().iterator();

            while(var4.hasNext()) {
                RealConnectionStatsInfo connStatsInfo = (RealConnectionStatsInfo)var4.next();
                info.append("\t\t").append(connStatsInfo).append("\n");
            }
        }

        info.append("}");
        return info.toString();
    }

    public void statsAllConnectionPoolInfo() {
        Iterator var1 = this.availableDataSourceTable.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<DataSourceType, List<DelegatingDataSource>> entry = (Map.Entry)var1.next();
            List<DelegatingDataSource> value = (List)entry.getValue();
            if (value != null && value.size() > 0) {
                logger.info(this.statsConnectionPoolInfo(((DelegatingDataSource)value.get(0)).getUnderlyingDruidDataSource(), false));
            }
        }

    }

    public void traceActiveConnection(Connection conn) {
        if (conn != null) {
            RealConnectionStatsInfo rcsi = new RealConnectionStatsInfo(conn);
            this.activeConnections.put(CommonUtil.getObjectHashCode(conn), rcsi);
        }
    }

    public void removeActiveConnection(Connection conn) {
        if (conn != null) {
            this.activeConnections.remove(CommonUtil.getObjectHashCode(conn));
        }
    }

    public Map<DataSourceType, List<DelegatingDataSource>> getAvailableDataSourceTable() {
        return this.availableDataSourceTable;
    }

    public String getPrjInfo() {
        StringBuilder str = new StringBuilder();
        str.append("项目信息：[projectId=").append(this.dataSource.getProjectId()).append(", dbName=").append(this.dataSource.getDbName()).append(", dalEnv=").append(this.dataSource.getEnvironment()).append(", appServerIp=").append(CommonUtil.getLocalAddress()).append("]");
        return str.toString();
    }

    public static class ConfigModel {
        private String xml;
        private String md5;
        private String dbName;

        public ConfigModel(String dbName, String xml, String md5) {
            this.dbName = dbName;
            this.xml = xml;
            this.md5 = md5;
        }

        public String getXml() {
            return this.xml;
        }

        public void setXml(String xml) {
            this.xml = xml;
        }

        public String getMd5() {
            return this.md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String getDbName() {
            return this.dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String toString() {
            return String.format("数据库名称=%s, MD5=%s, XML=%s", this.dbName, this.md5, this.xml);
        }
    }
}
