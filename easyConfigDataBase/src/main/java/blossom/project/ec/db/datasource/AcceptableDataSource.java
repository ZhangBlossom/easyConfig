package blossom.project.ec.db.datasource;

import blossom.project.ec.client.config.AppProperties;
import blossom.project.ec.db.config.DataSourceType;
import blossom.project.ec.db.exception.DataSourceException;
import blossom.project.ec.db.manager.TransactionContextManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: 张锦标
 * @date: 22023/7/3 11:09
 * AcceptableDataSource类
 * 可接收的数据源
 * 数据源配置来自于配置中心
 */
public class AcceptableDataSource extends BasicDataSource implements Closeable {
    private Logger logger = LoggerFactory.getLogger(AcceptableDataSource.class);
    //事务管理器
    private TransactionContextManager tcmanager;
    //可接受的数据源的状态
    //0--尚未初始化 1--初始化完成 2--关闭
    private AtomicInteger adsStatus = new AtomicInteger(0);

    public AcceptableDataSource() {
    }

    public void init() {
        String projectIdFromCode = this.getProjectId();
        //从各类配置环境中试图获取配置
        String projectIdFromConfig = AppProperties.getProjectName();
        this.logger.warn("数据库初始化配置名称：代码配置【{}】，配置文件配置:【{}】", projectIdFromCode, projectIdFromConfig);
        if (StringUtils.isNoneBlank(new CharSequence[]{projectIdFromCode})) {
            this.setProjectId(projectIdFromCode);
        } else if (StringUtils.isNoneBlank(new CharSequence[]{projectIdFromConfig})) {
            this.setProjectId(projectIdFromConfig);
        }

        String projectEnvFromCode = this.getEnvironment();
        String projectEnvFromConfig = AppProperties.getEnvironment();
        this.logger.warn("数据库初始化环境： 项目环境【{}】，配置文件环境:【{}】", projectEnvFromCode, projectEnvFromConfig);
        if (StringUtils.isNoneBlank(new CharSequence[]{projectEnvFromCode})) {
            this.setEnvironment(projectEnvFromCode);
        } else if (StringUtils.isNoneBlank(new CharSequence[]{projectEnvFromConfig})) {
            this.setEnvironment(projectEnvFromConfig);
        }
        //初始化事务管理器
        if (this.adsStatus.get() == 0) {
            this.tcmanager = new TransactionContextManager(this);
            this.tcmanager.init();
            this.adsStatus.set(1);
            this.logger.info("数据库初始化完成，当前项目名称:"
                    + AppProperties.getProjectName() + ",数据库名称:" + this.dbName + "}");
        }
    }

    public Connection getConnection() throws SQLException {
        this.checkDataSourceStateIsNormal();
        if (this.tcmanager == null) {
            this.logger.error("not yet initialized AcceptableDataSource!");
            throw new DataSourceException("not yet initialized AcceptableDataSource!");
        } else {
            DelegatingConnection delegtConn = new DelegatingConnection(this.tcmanager);
            this.tcmanager.doRegisterTransaction(delegtConn);
            return delegtConn;
        }
    }

    public void close() {
        this.logger.info("close AcceptableDataSource...");
        this.checkDataSourceStateIsNormal();
        if (this.tcmanager != null) {
            this.tcmanager.destroy();
            this.tcmanager = null;
        }

        this.adsStatus.set(2);
    }

    public void switchToWriteDB() throws SQLException {
        this.tcmanager.forceToUseDataSourceType(DataSourceType.WRITE);
    }

    public void switchToReadDB() throws SQLException {
        this.tcmanager.forceToUseDataSourceType(DataSourceType.READ);
    }

    private void checkDataSourceStateIsNormal() {
        switch (this.adsStatus.get()) {
            case 0:
                this.logger.error("not yet initialized AcceptableDataSource!");
                throw new DataSourceException("not yet initialized AcceptableDataSource!");
            case 2:
                this.logger.error("AcceptableDataSource already has been closed!");
                throw new DataSourceException("AcceptableDataSource already has been closed!");
            default:
        }
    }

    public SQLException handleException(DelegatingConnection conn, String sql, Throwable t) throws SQLException {
        if (t instanceof DataSourceException) {
            throw (DataSourceException)t;
        } else {
            throw new SQLException("Execute SQL [" + sql + "] Error!", t);
        }
    }

    public Map<DataSourceType, List<DelegatingDataSource>> getAvailableDataSourceTable() {
        return this.tcmanager.getAvailableDataSourceTable();
    }
}
