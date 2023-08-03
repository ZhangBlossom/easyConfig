//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.datasource;

import blossom.project.ec.db.config.DataSourceType;
import blossom.project.ec.db.datasource.jta.DefaultJtaDataSourceFactory;
import blossom.project.ec.db.datasource.jta.JtaDataSourceConfig;
import blossom.project.ec.db.datasource.jta.JtaProvider;
import blossom.project.ec.db.util.CommonUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.xa.DruidXADataSource;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import javax.sql.DataSource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingDataSource implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingDataSource.class);
    public static final int ATOMIKOS_DEFAULT_REAP_TIMEOUT = 3600;
    public static final int ATOMIKOS_DEFAULT_MIN_POOL_SIZE = 3;
    private static final int ATOMIKOS_MAX_UNIQUE_RESOURCE_NAME_LENGTH = 45;
    private static final int MAX_PROJECT_ID_LENGTH = 15;
    public static final String SYS_PROP_JTA_ENABLED = "dal.jtaEnabled";
    private BasicDataSource BasicDataSource;
    private DataSource realDataSource;
    private DataSourceType dataSourceType;

    public DelegatingDataSource() {
    }

    public DataSource getRealDataSource() {
        return this.realDataSource;
    }

    public BasicDataSource getBasicDataSource() {
        return this.BasicDataSource;
    }

    public DruidDataSource getUnderlyingDruidDataSource() {
        if (this.BasicDataSource.jtaEnabled) {
            AtomikosDataSourceBean atomikosDataSourceBean = (AtomikosDataSourceBean)this.realDataSource;
            return (DruidDataSource)atomikosDataSourceBean.getXaDataSource();
        } else {
            return (DruidDataSource)this.realDataSource;
        }
    }

    public DataSourceType getDataSourceType() {
        return this.dataSourceType;
    }

    public void close() {
        if (this.BasicDataSource.jtaEnabled) {
            AtomikosDataSourceBean atomikosDataSourceBean = (AtomikosDataSourceBean)this.realDataSource;
            atomikosDataSourceBean.close();
            ((DruidXADataSource)atomikosDataSourceBean.getXaDataSource()).close();
        } else {
            ((DruidDataSource)this.realDataSource).close();
        }

    }

    public static DelegatingDataSource newInstance(BasicDataSource BasicDataSource, DataSourceType dataSourceType) throws SQLException {
        Object lowLevelDataSource;
        String dataSourceName;
        if (BasicDataSource.jtaEnabled) {
            DruidXADataSource realDataSource = new DruidXADataSource();
            configDruidDataSource("XADataSource_", realDataSource, BasicDataSource, dataSourceType);

            try {
                realDataSource.init();
            } catch (Throwable var8) {
                realDataSource.close();
                throw var8;
            }

            dataSourceName = realDataSource.getName();
            String uniqueResourceName = realDataSource.getName();
            if (uniqueResourceName.length() > 45) {
                uniqueResourceName = "XADataSource_" + CommonUtil.getShortName(BasicDataSource.getProjectId(), 15) + "_" + dataSourceType + "_" + System.identityHashCode(realDataSource);
                if (uniqueResourceName.length() > 45) {
                    uniqueResourceName = CommonUtil.generateUUIDString();
                }
            }

            JtaDataSourceConfig jtaDataSourceConfig = new JtaDataSourceConfig();
            jtaDataSourceConfig.setUniqueResourceName(uniqueResourceName);
            jtaDataSourceConfig.setXaDataSource(realDataSource);
            jtaDataSourceConfig.setMinPoolSize(Math.max(BasicDataSource.getInitialSize(), 3));
            jtaDataSourceConfig.setMaxPoolSize(Math.max(BasicDataSource.getInitialSize(), 3));
            lowLevelDataSource = DefaultJtaDataSourceFactory.getInstance().createJtaDataSource(JtaProvider.ATOMIKOS, jtaDataSourceConfig);
        } else {
            DruidDataSource realDataSource = new DruidDataSource();
            configDruidDataSource("DataSource_", realDataSource, BasicDataSource, dataSourceType);

            try {
                realDataSource.init();
            } catch (Throwable var7) {
                realDataSource.close();
                throw var7;
            }

            dataSourceName = realDataSource.getName();
            lowLevelDataSource = realDataSource;
        }

        LOGGER.info("{}  jtaEnabled:{}", dataSourceName, BasicDataSource.jtaEnabled);
        DelegatingDataSource dds = new DelegatingDataSource();
        dds.BasicDataSource = BasicDataSource;
        dds.realDataSource = (DataSource)lowLevelDataSource;
        dds.dataSourceType = dataSourceType;
        return dds;
    }

    private static void configDruidDataSource(String namePrefix, DruidDataSource realDataSource, BasicDataSource BasicDataSource, DataSourceType dataSourceType) throws SQLException {
        realDataSource.setName(namePrefix + BasicDataSource.getProjectId() + "_" + dataSourceType + "_" + BasicDataSource.getDbName() + "_" + System.identityHashCode(realDataSource));
        realDataSource.setUsername(BasicDataSource.username);
        realDataSource.setPassword(BasicDataSource.password);
        realDataSource.setUrl(BasicDataSource.url);
        realDataSource.setInitialSize(BasicDataSource.initialSize);
        realDataSource.setMaxActive(BasicDataSource.maxActive);
        realDataSource.setMinIdle(BasicDataSource.minIdle);
        realDataSource.setMaxWait(BasicDataSource.maxWait);
        realDataSource.setPoolPreparedStatements(BasicDataSource.poolPreparedStatements);
        realDataSource.setMaxOpenPreparedStatements(BasicDataSource.maxOpenPreparedStatements);
        realDataSource.setValidationQuery(BasicDataSource.validationQuery);
        realDataSource.setValidationQueryTimeout(BasicDataSource.validationQueryTimeout);
        realDataSource.setTestOnBorrow(BasicDataSource.testOnBorrow);
        realDataSource.setTestOnReturn(BasicDataSource.testOnReturn);
        realDataSource.setTestWhileIdle(BasicDataSource.testWhileIdle);
        realDataSource.setTimeBetweenEvictionRunsMillis(BasicDataSource.timeBetweenEvictionRunsMillis);
        realDataSource.setMinEvictableIdleTimeMillis(BasicDataSource.minEvictableIdleTimeMillis);
        realDataSource.setMaxEvictableIdleTimeMillis(BasicDataSource.maxEvictableIdleTimeMillis);
        realDataSource.setConnectionInitSqls(BasicDataSource.connectionInitSqls);
        realDataSource.setFilters(BasicDataSource.filters);
        realDataSource.setProxyFilters(BasicDataSource.proxyFilters);
        realDataSource.setDriverClassName(BasicDataSource.driverClassName);
        realDataSource.setRemoveAbandoned(BasicDataSource.removeAbandoned);
        realDataSource.setRemoveAbandonedTimeout(BasicDataSource.removeAbandonedTimeout);
        realDataSource.setLogAbandoned(BasicDataSource.logAbandoned);
        realDataSource.setTimeBetweenLogStatsMillis(BasicDataSource.timeBetweenLogStatsMillis);
        realDataSource.setStatLogger(BasicDataSource.statLogger);
        realDataSource.setBreakAfterAcquireFailure(false);
        realDataSource.setPhyTimeoutMillis((long)BasicDataSource.getPhyTimeoutMillis());
        realDataSource.addConnectionProperty("appName", BasicDataSource.projectId);
        realDataSource.addConnectionProperty("progName", BasicDataSource.projectId);
        if (BasicDataSource.connectionProperties != null && !BasicDataSource.connectionProperties.isEmpty()) {
            Iterator var4 = BasicDataSource.connectionProperties.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<String, String> connPropEntry = (Map.Entry)var4.next();
                realDataSource.addConnectionProperty((String)connPropEntry.getKey(), (String)connPropEntry.getValue());
            }
        }

    }

    public String toString() {
        return (new StringBuilder(300)).append("[").append(this.BasicDataSource.dbName).append(",").append(this.BasicDataSource.url).append(",").append(this.dataSourceType).append("]").toString();
    }
}
