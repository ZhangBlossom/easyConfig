package blossom.project.ec.db.datasource.jta;

import javax.sql.XADataSource;

/**
 * @author: 张锦标
 * @date: 22023/7/3 11:32
 * JtaDataSourceConfig类
 * JTA数据源配置类 可以将需要配置的属性放在这里面
 * 我们的项目默认使用的是ATOMIKOS 所以只填写了ATOMIKOS的配置属性
 */
public class JtaDataSourceConfig {
    private String uniqueResourceName;
    private XADataSource xaDataSource;
    private int minPoolSize;
    private int maxPoolSize;
    private int reapTimeout;

    public JtaDataSourceConfig() {
    }

    public String getUniqueResourceName() {
        return this.uniqueResourceName;
    }

    public void setUniqueResourceName(String uniqueResourceName) {
        this.uniqueResourceName = uniqueResourceName;
    }

    public XADataSource getXaDataSource() {
        return this.xaDataSource;
    }

    public void setXaDataSource(XADataSource xaDataSource) {
        this.xaDataSource = xaDataSource;
    }

    public int getMinPoolSize() {
        return this.minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getReapTimeout() {
        return this.reapTimeout;
    }

    public void setReapTimeout(int reapTimeout) {
        this.reapTimeout = reapTimeout;
    }
}
