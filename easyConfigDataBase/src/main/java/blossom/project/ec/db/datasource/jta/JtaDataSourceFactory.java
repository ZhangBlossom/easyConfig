package blossom.project.ec.db.datasource.jta;

import javax.sql.DataSource;

/**
 * @author: 张锦标
 * @date: 22023/7/3 13:33
 * JtaDataSourceFactory接口
 * JTA数据源工厂
 * 用于创建JTA数据源
 */
public interface JtaDataSourceFactory {
    /**
     * 创建Java事务架构数据源
     * @param jtaProvider 事务架构选择器 用于选择创建的事务架构
     * @param jtaDataSourceConfig 事务架构配置
     * @return
     */
    public DataSource createJtaDataSource(JtaProvider jtaProvider,JtaDataSourceConfig jtaDataSourceConfig);
}
