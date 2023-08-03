package blossom.project.ec.db.datasource;

import javax.sql.DataSource;



import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSourceStatLogger;
import com.alibaba.druid.pool.DruidDataSourceStatLoggerImpl;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author: 张锦标
 * @date: 22023/7/3 11:34
 * BasicDataSource类
 */
public class BasicDataSource implements DataSource {
    protected boolean checkSqlWhereCondition = true;
    protected String dbType;
    protected String environment;
    protected String projectId;
    protected String customizeCfg;
    protected String dbName;
    protected String url;
    protected String username;
    protected String password;
    protected String driverClassName;
    protected int initialSize = 1;
    protected int maxActive = 100;
    protected int minIdle = 1;
    protected long maxWait = 3000L;
    protected boolean poolPreparedStatements = false;
    protected int maxOpenPreparedStatements = -1;
    protected String validationQuery = "select 1";
    protected int validationQueryTimeout = 3;
    protected boolean testOnBorrow = false;
    protected boolean testOnReturn = false;
    protected boolean testWhileIdle = true;
    protected long timeBetweenEvictionRunsMillis = 60000L;
    protected long minEvictableIdleTimeMillis = 180000L;
    protected long maxEvictableIdleTimeMillis = 300000L;
    protected boolean removeAbandoned = false;
    protected int removeAbandonedTimeout = 60;
    protected boolean logAbandoned = true;
    protected List<String> connectionInitSqls;
    protected int phyTimeoutMillis = -1;
    protected String filters;
    protected List<Filter> proxyFilters;
    protected long timeBetweenLogStatsMillis;
    protected DruidDataSourceStatLogger statLogger = new DruidDataSourceStatLoggerImpl();
    protected Map<String, String> connectionProperties;
    protected Map<String, String> jdbcConnectionProperties;
    protected boolean jtaEnabled;

    public BasicDataSource() {
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public Connection getConnection() throws SQLException {
        return null;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public void setProjectId(String projectId) {
        if (projectId != null) {
            projectId = projectId.trim();
        }

        this.projectId = projectId;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        if (this.dbName != null) {
            this.dbName = this.dbName.trim();
        }

    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String env) {
        if (env != null) {
            env = env.trim();
        }

        this.environment = env;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getInitialSize() {
        return this.initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMaxActive() {
        return this.maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMinIdle() {
        return this.minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getMaxWait() {
        return this.maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public int getMaxOpenPreparedStatements() {
        return this.maxOpenPreparedStatements;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public String getValidationQuery() {
        return this.validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getValidationQueryTimeout() {
        return this.validationQueryTimeout;
    }

    public void setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    public boolean isTestOnBorrow() {
        return this.testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return this.testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return this.testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public List<String> getConnectionInitSqls() {
        return this.connectionInitSqls;
    }

    public void setConnectionInitSqls(List<String> connectionInitSqls) {
        this.connectionInitSqls = connectionInitSqls;
    }

    public String getFilters() {
        return this.filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public List<Filter> getProxyFilters() {
        return this.proxyFilters;
    }

    public void setProxyFilters(List<Filter> proxyFilters) {
        this.proxyFilters = proxyFilters;
    }

    public boolean isJtaEnabled() {
        return this.jtaEnabled;
    }

    public void setJtaEnabled(boolean jtaEnabled) {
        this.jtaEnabled = jtaEnabled;
    }

    public int getPhyTimeoutMillis() {
        return this.phyTimeoutMillis;
    }

    public void setPhyTimeoutMillis(int phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
    }

    public BasicDataSource clone() {
        BasicDataSource cloneDS = new BasicDataSource();
        cloneDS.environment = this.environment;
        cloneDS.projectId = this.projectId;
        cloneDS.dbName = this.dbName;
        cloneDS.username = this.username;
        cloneDS.password = this.password;
        cloneDS.url = this.url;
        cloneDS.driverClassName = this.driverClassName;
        cloneDS.minIdle = this.minIdle;
        cloneDS.maxActive = this.maxActive;
        cloneDS.initialSize = this.initialSize;
        cloneDS.maxWait = this.maxWait;
        cloneDS.testOnBorrow = this.testOnBorrow;
        cloneDS.testOnReturn = this.testOnReturn;
        cloneDS.testWhileIdle = this.testWhileIdle;
        cloneDS.timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRunsMillis;
        cloneDS.minEvictableIdleTimeMillis = this.minEvictableIdleTimeMillis;
        cloneDS.poolPreparedStatements = this.poolPreparedStatements;
        cloneDS.maxOpenPreparedStatements = this.maxOpenPreparedStatements;
        cloneDS.connectionInitSqls = this.connectionInitSqls;
        cloneDS.filters = this.filters;
        cloneDS.proxyFilters = this.proxyFilters;
        cloneDS.validationQuery = this.validationQuery;
        cloneDS.validationQueryTimeout = this.validationQueryTimeout;
        cloneDS.removeAbandoned = this.removeAbandoned;
        cloneDS.removeAbandonedTimeout = this.removeAbandonedTimeout;
        cloneDS.logAbandoned = this.logAbandoned;
        cloneDS.connectionProperties = this.connectionProperties;
        cloneDS.jdbcConnectionProperties = this.jdbcConnectionProperties;
        cloneDS.statLogger = this.statLogger;
        cloneDS.timeBetweenLogStatsMillis = this.timeBetweenLogStatsMillis;
        cloneDS.jtaEnabled = this.jtaEnabled;
        cloneDS.phyTimeoutMillis = this.phyTimeoutMillis;
        return cloneDS;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.dbName == null ? 0 : this.dbName.hashCode());
        result = 31 * result + (this.driverClassName == null ? 0 : this.driverClassName.hashCode());
        result = 31 * result + (this.password == null ? 0 : this.password.hashCode());
        result = 31 * result + (this.url == null ? 0 : this.url.hashCode());
        result = 31 * result + (this.username == null ? 0 : this.username.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            BasicDataSource other = (BasicDataSource)obj;
            if (this.dbName == null) {
                if (other.dbName != null) {
                    return false;
                }
            } else if (!this.dbName.equals(other.dbName)) {
                return false;
            }

            if (this.driverClassName == null) {
                if (other.driverClassName != null) {
                    return false;
                }
            } else if (!this.driverClassName.equals(other.driverClassName)) {
                return false;
            }

            if (this.password == null) {
                if (other.password != null) {
                    return false;
                }
            } else if (!this.password.equals(other.password)) {
                return false;
            }

            if (this.url == null) {
                if (other.url != null) {
                    return false;
                }
            } else if (!this.url.equals(other.url)) {
                return false;
            }

            if (this.username == null) {
                if (other.username != null) {
                    return false;
                }
            } else if (!this.username.equals(other.username)) {
                return false;
            }

            return true;
        }
    }

    public boolean isRemoveAbandoned() {
        return this.removeAbandoned;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public int getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public boolean isLogAbandoned() {
        return this.logAbandoned;
    }

    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public Map<String, String> getConnectionProperties() {
        return this.connectionProperties;
    }

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getCustomizeCfg() {
        return this.customizeCfg;
    }

    public void setCustomizeCfg(String customizeCfg) {
        this.customizeCfg = customizeCfg;
    }

    public long getTimeBetweenLogStatsMillis() {
        return this.timeBetweenLogStatsMillis;
    }

    public DruidDataSourceStatLogger getStatLogger() {
        return this.statLogger;
    }

    public void setTimeBetweenLogStatsMillis(long timeBetweenLogStatsMillis) {
        this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
    }

    public void setStatLogger(DruidDataSourceStatLogger statLogger) {
        this.statLogger = statLogger;
    }

    public boolean isCheckSqlWhereCondition() {
        return this.checkSqlWhereCondition;
    }

    public void setCheckSqlWhereCondition(boolean checkSqlWhereCondition) {
        this.checkSqlWhereCondition = checkSqlWhereCondition;
    }

    public String getDbType() {
        return this.dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Map<String, String> getJdbcConnectionProperties() {
        return this.jdbcConnectionProperties;
    }

    public void setJdbcConnectionProperties(Map<String, String> jdbcConnectionProperties) {
        this.jdbcConnectionProperties = jdbcConnectionProperties;
    }
}
