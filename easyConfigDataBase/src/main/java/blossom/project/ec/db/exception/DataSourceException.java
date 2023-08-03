package blossom.project.ec.db.exception;

/**
 * @author: 张锦标
 * @date: 22023/7/3 11:38
 * DataSourceException类
 * 数据源异常类
 */
public class DataSourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataSourceException() {
    }

    public DataSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

    public DataSourceException(String message) {
        super(message);
    }
}
