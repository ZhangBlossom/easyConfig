package blossom.project.ec.db.datasource.jta;

import blossom.project.ec.db.util.ReflectUtil;

import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * @author: 张锦标
 * @date: 22023/7/3 13:32
 * JtaDataSourceFactory类
 */
public class DefaultJtaDataSourceFactory implements JtaDataSourceFactory {
    private static final JtaDataSourceFactory INSTANCE = new DefaultJtaDataSourceFactory();

    public DefaultJtaDataSourceFactory() {
    }

    public static JtaDataSourceFactory getInstance() {
        return INSTANCE;
    }

    public DataSource createJtaDataSource(JtaProvider jtaProvider, JtaDataSourceConfig dataSourceConfig) {
        switch (jtaProvider) {
            case ATOMIKOS:
                try {
                    DataSource dataSource = this.createAtomikos(dataSourceConfig);
                    return dataSource;
                } catch (Exception var5) {
                    throw new IllegalStateException("createAtomikos failed. 'com.atomikos:transactions-jdbc:4.0.6' and 'javax.transaction:jta:1.1' are required.", var5);
                }
            default:
                throw new IllegalStateException(jtaProvider + " not supported");
        }
    }

    /**
     * 使用反射
     * @param dataSourceConfig jta数据源配置
     * @return 配置好的数据源
     * @throws Exception
     */
    private DataSource createAtomikos(JtaDataSourceConfig dataSourceConfig) throws Exception {
        Class<?> clazz = Class.forName("com.atomikos.jdbc.AtomikosDataSourceBean");
        Object beanInstance = clazz.newInstance();
        ReflectUtil.invokeMethod(beanInstance, new Object[]{dataSourceConfig.getUniqueResourceName()}, clazz, "setUniqueResourceName", new Class[]{String.class});
        ReflectUtil.invokeMethod(beanInstance, new Object[]{dataSourceConfig.getXaDataSource()}, clazz, "setXaDataSource", new Class[]{XADataSource.class});
        ReflectUtil.invokeMethod(beanInstance, new Object[]{dataSourceConfig.getMinPoolSize()}, clazz, "setMinPoolSize", new Class[]{Integer.TYPE});
        ReflectUtil.invokeMethod(beanInstance, new Object[]{dataSourceConfig.getMaxPoolSize()}, clazz, "setMaxPoolSize", new Class[]{Integer.TYPE});
        ReflectUtil.invokeMethod(beanInstance, new Object[]{dataSourceConfig.getReapTimeout()}, clazz, "setReapTimeout", new Class[]{Integer.TYPE});
        ReflectUtil.invokeMethod(beanInstance, (Object[])null, clazz, "init", (Class[])null);
        return (DataSource)beanInstance;
    }
}
