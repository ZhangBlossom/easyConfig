//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.datasource;

import blossom.project.ec.db.util.CommonUtil;
import blossom.project.ec.db.util.DateUtil;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.Connection;

public class RealConnectionStatsInfo {
    private Connection realConn;
    private long startTimestamp;

    public RealConnectionStatsInfo(Connection realConn) {
        this.realConn = realConn;
        this.startTimestamp = System.currentTimeMillis();
    }

    public Connection getRealConn() {
        return this.realConn;
    }

    public void setRealConn(DruidPooledConnection realConn) {
        this.realConn = realConn;
    }

    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String toString() {
        if (this.realConn == null) {
            return null;
        } else {
            StringBuilder info = new StringBuilder();
            info.append("[");
            if (this.realConn instanceof DruidPooledConnection) {
                DruidPooledConnection druidPooledConnection = (DruidPooledConnection) this.realConn;
                info.append(druidPooledConnection.getConnectionHolder().getDataSource().getRawJdbcUrl()).append(", ").append("所属线程：").append(druidPooledConnection.getOwnerThread());
            }

            info.append("申请时间：").append(DateUtil.parseDataTime(this.startTimestamp)).append(", ").append("耗时：").append(System.currentTimeMillis() - this.startTimestamp).append(" ms, ").append("是否开启事务：").append(this.getAutoCommitInfo()).append("]");
            return info.toString();
        }
    }

    private String getAutoCommitInfo() {
        if (this.realConn == null) {
            return null;
        } else {
            Object autoCommit;
            try {
                autoCommit = this.realConn.getAutoCommit();
            } catch (Exception var3) {
                autoCommit = CommonUtil.getStackTrace(var3);
            }

            return autoCommit.toString();
        }
    }
}
