package blossom.project.ec.db.datasource.jta;

/**
 * @author: 张锦标
 * @date: 22023/7/3 11:33
 * JtaProvider类
 * 用于选择JTA具体实现的数据源
 */
public enum JtaProvider {
    ATOMIKOS;
    private JtaProvider(){}
}
