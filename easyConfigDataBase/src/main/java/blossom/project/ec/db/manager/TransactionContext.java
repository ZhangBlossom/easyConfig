//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.manager;

import blossom.project.ec.db.datasource.DelegatingConnection;
import blossom.project.ec.db.util.CommonUtil;
import com.alibaba.druid.pool.DruidPooledConnection;
import java.sql.Connection;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionContext {
    private Logger logger = LoggerFactory.getLogger(TransactionContext.class);
    private String id = UUID.randomUUID().toString();
    private DelegatingConnection delegatingConnection;
    private Connection realConnection;
    private Map<String, String> dsInfo;
    private Queue<Action> actions;

    public TransactionContext() {
    }

    public String getId() {
        return this.id;
    }

    public DelegatingConnection getDelegatingConnection() {
        return this.delegatingConnection;
    }

    public void setDelegatingConnection(DelegatingConnection delegatingConnection) {
        this.delegatingConnection = delegatingConnection;
    }

    public Connection getRealConnection() {
        return this.realConnection;
    }

    public void setRealConnection(Connection realConnection) {
        this.realConnection = realConnection;
        if (this.realConnection instanceof DruidPooledConnection) {
            DruidPooledConnection dpc = (DruidPooledConnection)this.realConnection;
            this.dsInfo = CommonUtil.getDataSourceInfo(dpc);
        }

    }

    public void addAction(String methodName, Object... setParamVals) {
        this.addAction(new Action(methodName, setParamVals));
    }

    public void addAction(Action action) {
        if (this.actions == null) {
            this.actions = new ConcurrentLinkedQueue();
        }

        this.actions.offer(action);
    }

    public boolean isInitCompleted() {
        return this.delegatingConnection != null && this.realConnection != null;
    }

    public Queue<Action> getActions() {
        return this.actions;
    }

    public void destroy() {
        try {
            if (this.realConnection != null && !this.realConnection.isClosed()) {
                if (!this.realConnection.getAutoCommit()) {
                    this.realConnection.setAutoCommit(true);
                }

                this.realConnection.close();
            }
        } catch (Exception var2) {
            this.logger.error("", var2);
        }

        if (!CommonUtil.isNullOrEmpty(this.actions)) {
            this.actions.clear();
        }

        this.actions = null;
    }

    public Map<String, String> getDataSourceInfo() {
        return this.dsInfo;
    }
}
