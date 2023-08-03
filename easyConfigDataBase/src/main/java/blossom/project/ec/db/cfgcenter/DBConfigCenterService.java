//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package blossom.project.ec.db.cfgcenter;

import blossom.project.ec.client.config.ChangeConfigData;
import blossom.project.ec.client.config.ConfigCenterClient;
import blossom.project.ec.client.config.ConfigChanged;
import blossom.project.ec.client.config.ConfigData;
import blossom.project.ec.db.exception.DataSourceException;
import blossom.project.ec.db.manager.TransactionContextManager;
import blossom.project.ec.db.util.CommonUtil;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DBConfigCenterService {
    private static final Logger logger = LoggerFactory.getLogger(DBConfigCenterService.class);
    private static String DB_CONFIG_KEY_FROM_PROPERTIES;
    private AtomicInteger scheduledUpdateFailTimes = new AtomicInteger(0);
    private TransactionContextManager tcmanager;
    private ScheduledExecutorService taskExecutor = new ScheduledThreadPoolExecutor(1,
            (new BasicThreadFactory.Builder()).namingPattern("taskExecutor-pool-%d").build());
    private ScheduledExecutorService statisInfoExecutor = new ScheduledThreadPoolExecutor(1,
            (new BasicThreadFactory.Builder()).namingPattern("statisInfoExecutor-pool-%d").build());
    private ConfigChanged configChangedListener = new ConfigChanged() {
        public void configChanged(ChangeConfigData changeInfo) {
            super.configChanged(changeInfo);
            Iterator var2 = changeInfo.getUpdateList().iterator();
            //遍历所有的更新事件
            //并且如果有更新事件是刚刚好去更新了数据库配置的 那么就再次调用onReceiveMessage方法
            //然后去更新数据库配置
            while (var2.hasNext()) {
                ConfigData configData = (ConfigData) var2.next();
                if (DB_CONFIG_KEY_FROM_PROPERTIES.equals(configData.getKey())) {
                    DBConfigCenterService.this.onReceiveMessage(configData.getValue(), true, false);
                }
            }

        }
    };

    public DBConfigCenterService(TransactionContextManager tcmanager) {
        this.tcmanager = tcmanager;
    }

    public DBConfigCenterService(TransactionContextManager tcmanager, String db_key) {
        this.tcmanager = tcmanager;
        DB_CONFIG_KEY_FROM_PROPERTIES = db_key;
    }

    public void start() {
        //从配置中心拉取配置并且初始化数据源
        this.pullConfig(true);
        //每隔60s统计一次数据源信息
        this.statisInfoExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                DBConfigCenterService.this.tcmanager.statsAllConnectionPoolInfo();
            }
        }, 60L, 60L, TimeUnit.SECONDS);
        //判断当前配置是否来自于配置中心而非本地
        //如果来自配置中心 那么每隔30s去配置中心拉去一次配置
        if (!CommonUtil.isLocalEnv(this.tcmanager.getDataSource().getEnvironment()) && !CommonUtil.isCustomizeEnv(this.tcmanager.getDataSource().getEnvironment())) {
            this.taskExecutor.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    try {
                        DBConfigCenterService.this.pullUnfmCfg(false);
                    } catch (Exception var2) {
                        DBConfigCenterService.logger.error("拉取配置中心信息失败!", var2);
                    }

                }
            }, 60L, 30L, TimeUnit.SECONDS);
            ConfigCenterClient.onConfigChanged(this.configChangedListener);
        }

    }

    public void destroy() {
        try {
            ConfigCenterClient.removeConfigChanged(this.configChangedListener);
            this.taskExecutor.shutdownNow();
            this.statisInfoExecutor.shutdownNow();
        } catch (Exception var2) {
            logger.error("close cfgcenter client error!", var2);
        }

    }

    private void pullConfig(boolean isInit) {
        this.pullUnfmCfg(isInit);
    }

    private void pullUnfmCfg(boolean isInit) {
        String cfgContent = null;
        String projectId = this.tcmanager.getDataSource().getProjectId();
        String env = this.tcmanager.getDataSource().getEnvironment();
        String dbName = this.tcmanager.getDataSource().getDbName();
        try {
            cfgContent = ConfigCenterClient.get(projectId, env, DB_CONFIG_KEY_FROM_PROPERTIES);
        } catch (Exception var6) {
            logger.error("Error getting " + projectId + ":" + env + ":"
                    + dbName+"from Config Center Server", var6);
        }

        if (CommonUtil.isNullOrEmpty(cfgContent) && isInit) {
            throw new DataSourceException(this.tcmanager.getPrjInfo() + " 没有数据库配置，请配置!");
        } else {
            if (CommonUtil.isNullOrEmpty(cfgContent) && !isInit) {
                logger.error("【{}:{}:{}】Scheduled update task consecutive failed:【{}】times ", new Object[]{projectId,
                        env, dbName, this.scheduledUpdateFailTimes.incrementAndGet()});
            } else {
                this.scheduledUpdateFailTimes.set(0);
                this.onReceiveMessage(cfgContent, false, isInit);
            }

        }
    }

    private void onReceiveMessage(String message, boolean isOnChaange, boolean isInit) {
        if (CommonUtil.isNullOrEmpty(message)) {
            throw new DataSourceException(this.tcmanager.getPrjInfo() + " 没有数据库配置，请先联系DBA申请配置!");
        } else {
            if (isOnChaange) {
                logger.info("[{}]收到onChange配置: {}", this.tcmanager.getDataSource().getProjectId(),
                        CommonUtil.hidePassword(message.replace("\n", "")));
            } else {
                logger.info("[{}]收到Task配置: {}", this.tcmanager.getDataSource().getProjectId(),
                        CommonUtil.hidePassword(message.replace("\n", "")));
            }

            try {
                this.tcmanager.updateDataSources(message, isInit);
            } catch (Exception var6) {
                String msg = "dal-new, 解析或更新配置出错!, error=" + var6.getMessage();
                logger.error("{}, \nxml={}", new Object[]{msg, message, var6});
                throw new DataSourceException(msg, var6);
            }

            if (!this.tcmanager.isInitDBCfg()) {
                throw new DataSourceException("DAL初始化失败，请检查appUk/ENV/dbName是否正确！");
            }
        }
    }
}
