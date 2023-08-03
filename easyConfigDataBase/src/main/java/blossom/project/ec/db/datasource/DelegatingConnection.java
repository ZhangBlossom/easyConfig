//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package blossom.project.ec.db.datasource;
import blossom.project.ec.db.config.DataSourceType;
import blossom.project.ec.db.exception.DataSourceException;
import blossom.project.ec.db.manager.Action;
import blossom.project.ec.db.manager.TransactionContext;
import blossom.project.ec.db.manager.TransactionContextManager;
import blossom.project.ec.db.util.CommonUtil;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(DelegatingConnection.class);
    private TransactionContextManager tcmanager;
    private Statement delegatingStatement;
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    public DelegatingConnection(TransactionContextManager tcmanager) {
        this.tcmanager = tcmanager;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        return false;
    }

    public Statement createStatement() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            if (this.delegatingStatement != null && this.delegatingStatement.isClosed()) {
                this.delegatingStatement = null;
            }

            if (this.delegatingStatement == null) {
                TransactionContext tc = this.tcmanager.getTransactionContext(this);
                if (tc != null && tc.isInitCompleted()) {
                    this.delegatingStatement = tc.getRealConnection().createStatement();
                } else {
                    DelegatingStatement delegateStat = new DelegatingStatement(this);
                    delegateStat.addAction("createStatement");
                    this.delegatingStatement = delegateStat;
                }
            } else if (this.delegatingStatement instanceof DelegatingStatement) {
                ((DelegatingStatement)this.delegatingStatement).addAction("createStatement");
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "createStatement", var3);
        }

        return this.delegatingStatement;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql);
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, sql, var3);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareCall(sql);
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, sql, var3);
            return null;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.tcmanager.holdRealConnection(this, sql);
        return this.tcmanager.getTransactionContext(this).getRealConnection().nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().setAutoCommit(autoCommit);
            } else {
                tc.addAction("setAutoCommit", new Object[]{autoCommit});
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "setAutoCommit: " + autoCommit, var3);
        }

    }

    public boolean getAutoCommit() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getAutoCommit() : (Boolean)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setAutoCommit", Boolean.class, 0, true);
    }

    public void commit() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().commit();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "commit", var2);
        }

    }

    public void rollback() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().rollback();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "rollback", var2);
        }

    }

    public void close() throws SQLException {
        try {
            if (this.isClosed()) {
                return;
            }

            if (this.delegatingStatement != null && !this.delegatingStatement.isClosed()) {
                this.delegatingStatement.close();
            }

            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted() && !tc.getRealConnection().isClosed()) {
                tc.getRealConnection().close();
            }

            this.isClosed.set(true);
            this.tcmanager.unDoRegisterTransaction(this);
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "close", var2);
        }

    }

    public boolean isClosed() throws SQLException {
        return this.isClosed.get();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && !tc.isInitCompleted()) {
                this.tcmanager.holdRealConnection(this, DataSourceType.READ);
            }

            return tc.getRealConnection().getMetaData();
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "getMetaData", var2);
            return null;
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().setReadOnly(readOnly);
            } else {
                tc.addAction("setReadOnly", new Object[]{readOnly});
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "setReadOnly: " + readOnly, var3);
        }

    }

    public boolean isReadOnly() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().isReadOnly() : (Boolean)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setReadOnly", Boolean.class, 0, false);
    }

    public void setCatalog(String catalog) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().setCatalog(catalog);
            } else {
                tc.addAction("setCatalog", new Object[]{catalog});
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "setCatalog: " + catalog, var3);
        }

    }

    public String getCatalog() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getCatalog() : (String)this.tcmanager.getPathTrackManager().getMethodValue(tc, "getCatalog", String.class, 0, null);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().setTransactionIsolation(level);
            } else {
                tc.addAction("setTransactionIsolation", new Object[]{level});
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "setTransactionIsolation: " + level, var3);
        }

    }

    public int getTransactionIsolation() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getTransactionIsolation() : (Integer)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setTransactionIsolation", Integer.class, 0, 0);
    }

    public SQLWarning getWarnings() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getWarnings() : null;
    }

    public void clearWarnings() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().clearWarnings();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "clearWarnings", var2);
        }

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            if (this.delegatingStatement != null && this.delegatingStatement.isClosed()) {
                this.delegatingStatement = null;
            }

            if (this.delegatingStatement == null) {
                TransactionContext tc = this.tcmanager.getTransactionContext(this);
                if (tc != null && tc.isInitCompleted()) {
                    this.delegatingStatement = tc.getRealConnection().createStatement(resultSetType, resultSetConcurrency);
                } else {
                    DelegatingStatement delegateStat = new DelegatingStatement(this);
                    delegateStat.addAction("createStatement", resultSetType, resultSetConcurrency);
                    this.delegatingStatement = delegateStat;
                }
            } else if (this.delegatingStatement instanceof DelegatingStatement) {
                ((DelegatingStatement)this.delegatingStatement).addAction("createStatement", resultSetType, resultSetConcurrency);
            }
        } catch (Exception var5) {
            this.tcmanager.getDataSource().handleException(this, "createStatement", var5);
        }

        return this.delegatingStatement;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (Exception var5) {
            this.tcmanager.getDataSource().handleException(this, sql, var5);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (Exception var5) {
            this.tcmanager.getDataSource().handleException(this, sql, var5);
            return null;
        }
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getTypeMap() :
                (Map)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setTypeMap", Map.class, 0, null);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().clearWarnings();
        } else {
            tc.addAction("setTypeMap", new Object[]{map});
        }

    }

    public void setHoldability(int holdability) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().setHoldability(holdability);
        } else {
            tc.addAction("setHoldability", new Object[]{holdability});
        }

    }

    public int getHoldability() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getHoldability() : (Integer)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setHoldability", Integer.class, 0, 0);
    }

    public Savepoint setSavepoint() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                return tc.getRealConnection().setSavepoint();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "setSavepoint", var2);
        }

        return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                return tc.getRealConnection().setSavepoint(name);
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "setSavepoint: " + name, var3);
        }

        return null;
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().rollback(savepoint);
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "rollback", var3);
        }

    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                tc.getRealConnection().releaseSavepoint(savepoint);
            }
        } catch (Exception var3) {
            this.tcmanager.getDataSource().handleException(this, "releaseSavepoint", var3);
        }

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            if (this.delegatingStatement != null && this.delegatingStatement.isClosed()) {
                this.delegatingStatement = null;
            }

            if (this.delegatingStatement == null) {
                TransactionContext tc = this.tcmanager.getTransactionContext(this);
                if (tc != null && tc.isInitCompleted()) {
                    this.delegatingStatement = tc.getRealConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
                } else {
                    DelegatingStatement delegateStat = new DelegatingStatement(this);
                    delegateStat.addAction("createStatement", resultSetType, resultSetConcurrency, resultSetHoldability);
                    this.delegatingStatement = delegateStat;
                }
            } else if (this.delegatingStatement instanceof DelegatingStatement) {
                ((DelegatingStatement)this.delegatingStatement).addAction("createStatement", resultSetType, resultSetConcurrency);
            }
        } catch (Exception var6) {
            this.tcmanager.getDataSource().handleException(this, "createStatement", var6);
        }

        return this.delegatingStatement;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (Exception var6) {
            this.tcmanager.getDataSource().handleException(this, sql, var6);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (Exception var6) {
            this.tcmanager.getDataSource().handleException(this, sql, var6);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql, autoGeneratedKeys);
        } catch (Exception var4) {
            this.tcmanager.getDataSource().handleException(this, sql, var4);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql, columnIndexes);
        } catch (Exception var4) {
            this.tcmanager.getDataSource().handleException(this, sql, var4);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        this.checkSqlWhereCondition(sql);

        try {
            this.tcmanager.holdRealConnection(this, sql);
            return this.tcmanager.getTransactionContext(this).getRealConnection().prepareStatement(sql, columnNames);
        } catch (Exception var4) {
            this.tcmanager.getDataSource().handleException(this, sql, var4);
            return null;
        }
    }

    private void checkSqlWhereCondition(String sql) {
        if (this.tcmanager.getDataSource().isCheckSqlWhereCondition() && !this.tcmanager.getDataSource().getDbType().equals("sqlserver")) {
            CommonUtil.checkSqlWhereCondition(sql, this.tcmanager.getDataSource().getDbType());
        }

    }

    public Clob createClob() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                return tc.getRealConnection().createClob();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "createClob", var2);
        }

        return null;
    }

    public Blob createBlob() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");

        try {
            TransactionContext tc = this.tcmanager.getTransactionContext(this);
            if (tc != null && tc.isInitCompleted()) {
                return tc.getRealConnection().createBlob();
            }
        } catch (Exception var2) {
            this.tcmanager.getDataSource().handleException(this, "createBlob", var2);
        }

        return null;
    }

    public NClob createNClob() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().createNClob() : null;
    }

    public SQLXML createSQLXML() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().createSQLXML() : null;
    }

    public boolean isValid(int timeout) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().isValid(timeout) : false;
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        CommonUtil.checkState(this.isClosed.get(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().setClientInfo(name, value);
        } else {
            tc.addAction("setClientInfo", new Object[]{name, value});
        }

    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        CommonUtil.checkState(this.isClosed.get(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().setClientInfo(properties);
        } else {
            tc.addAction("setClientInfo", new Object[]{properties});
        }

    }

    public String getClientInfo(String name) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getClientInfo(name) : (String)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setClientInfo", String.class, 1, null);
    }

    public Properties getClientInfo() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getClientInfo() : (Properties)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setClientInfo", Properties.class, 0, null);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().createArrayOf(typeName, elements) : null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().createStruct(typeName, attributes) : null;
    }

    public void setSchema(String schema) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().setSchema(schema);
        } else {
            tc.addAction("setSchema", new Object[]{schema});
        }

    }

    public String getSchema() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getSchema() : (String)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setSchema", String.class, 0, null);
    }

    public void abort(Executor executor) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().abort(executor);
        }

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        if (tc != null && tc.isInitCompleted()) {
            tc.getRealConnection().abort(executor);
        }

    }

    public int getNetworkTimeout() throws SQLException {
        CommonUtil.checkState(this.isClosed(), "Connection is already closed!");
        TransactionContext tc = this.tcmanager.getTransactionContext(this);
        return tc != null && tc.isInitCompleted() ? tc.getRealConnection().getNetworkTimeout() : (Integer)this.tcmanager.getPathTrackManager().getMethodValue(tc, "setNetworkTimeout", Integer.class, 1, 0);
    }

    private class DelegatingStatement implements Statement {
        private DelegatingConnection delegtConn;
        private Statement realStatement;
        private Queue<Action> actionQueue = new ConcurrentLinkedQueue();
        private AtomicBoolean isClosed = new AtomicBoolean(false);

        public DelegatingStatement(DelegatingConnection delegtConn) {
            this.delegtConn = delegtConn;
        }

        public void addAction(String methodName, Object... setParamVals) {
            this.actionQueue.offer(new Action(methodName, setParamVals));
        }

        private void beforeDoExecute(String sql) {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            DelegatingConnection.this.checkSqlWhereCondition(sql);
            Action action = (Action)this.actionQueue.poll();

            try {
                DelegatingConnection.this.tcmanager.holdRealConnection(this.delegtConn, sql);

                for(; action != null; action = (Action)this.actionQueue.poll()) {
                    if ("createStatement".equals(action.getMethodName())) {
                        this.realStatement = (Statement)DelegatingConnection.this.tcmanager.getPathTrackManager().doPathChain(DelegatingConnection.this.tcmanager.getTransactionContext(this.delegtConn).getRealConnection(), action);
                    } else if (this.realStatement != null) {
                        DelegatingConnection.this.tcmanager.getPathTrackManager().doPathChain(this.realStatement, action);
                    }
                }

            } catch (Exception var4) {
                DelegatingConnection.logger.error("[DelegatingStatement] beforeDoExecute: ", var4);
                if (action != null) {
                    this.actionQueue.add(action);
                }

                if (var4 instanceof DataSourceException) {
                    throw (DataSourceException)var4;
                } else {
                    throw new DataSourceException(var4);
                }
            }
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return false;
        }

        public ResultSet executeQuery(String sql) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.executeQuery(sql);
            } catch (Exception var3) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var3);
                return null;
            }
        }

        public int executeUpdate(String sql) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.executeUpdate(sql);
            } catch (Exception var3) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var3);
                return 0;
            }
        }

        public void close() throws SQLException {
            if (!this.isClosed()) {
                try {
                    if (this.realStatement != null && !this.realStatement.isClosed()) {
                        this.realStatement.close();
                    }

                    this.actionQueue.clear();
                    this.isClosed.set(true);
                } catch (Exception var2) {
                    this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, "DelegatingStatement close", var2);
                }

            }
        }

        public int getMaxFieldSize() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getMaxFieldSize() : (Integer)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setMaxFieldSize", Integer.class, 0, 0);
        }

        public void setMaxFieldSize(int max) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setMaxFieldSize(max);
            } else {
                this.addAction("setMaxFieldSize", max);
            }

        }

        public int getMaxRows() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getMaxRows() : (Integer)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setMaxRows", Integer.class, 0, 0);
        }

        public void setMaxRows(int max) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setMaxRows(max);
            } else {
                this.addAction("setMaxRows", max);
            }

        }

        public void setEscapeProcessing(boolean enable) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setEscapeProcessing(enable);
            } else {
                this.addAction("setEscapeProcessing", enable);
            }

        }

        public int getQueryTimeout() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getQueryTimeout() : (Integer)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setQueryTimeout", Integer.class, 0, 0);
        }

        public void setQueryTimeout(int seconds) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setQueryTimeout(seconds);
            } else {
                this.addAction("setQueryTimeout", seconds);
            }

        }

        public void cancel() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.cancel();
            }

        }

        public SQLWarning getWarnings() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getWarnings() : null;
        }

        public void clearWarnings() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.clearWarnings();
            }

        }

        public void setCursorName(String name) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setCursorName(name);
            } else {
                this.addAction("setCursorName", name);
            }

        }

        public boolean execute(String sql) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.execute(sql);
            } catch (Exception var3) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var3);
                return false;
            }
        }

        public ResultSet getResultSet() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");

            try {
                if (this.realStatement != null) {
                    return this.realStatement.getResultSet();
                }
            } catch (Exception var2) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, "getResultSet", var2);
            }

            return null;
        }

        public int getUpdateCount() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getUpdateCount() : 0;
        }

        public boolean getMoreResults() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getMoreResults() : false;
        }

        public void setFetchDirection(int direction) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setFetchDirection(direction);
            } else {
                this.addAction("setFetchDirection", direction);
            }

        }

        public int getFetchDirection() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getFetchDirection() : (Integer)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setFetchDirection", Integer.class, 0, 0);
        }

        public void setFetchSize(int rows) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setFetchSize(rows);
            } else {
                this.addAction("setFetchSize", rows);
            }

        }

        public int getFetchSize() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getFetchSize() : (Integer)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setFetchSize", Integer.class, 0, 0);
        }

        public int getResultSetConcurrency() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getResultSetConcurrency() : 0;
        }

        public int getResultSetType() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getResultSetType() : 0;
        }

        public void addBatch(String sql) throws SQLException {
            this.beforeDoExecute(sql);
            this.realStatement.addBatch(sql);
        }

        public void clearBatch() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.clearBatch();
            }

        }

        public int[] executeBatch() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");

            try {
                return this.realStatement.executeBatch();
            } catch (Exception var2) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, "executeBatch", var2);
                return null;
            }
        }

        public Connection getConnection() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            TransactionContext tc = DelegatingConnection.this.tcmanager.getTransactionContext(this.delegtConn);
            return (Connection)(tc != null && tc.isInitCompleted() ? tc.getRealConnection() : this.delegtConn);
        }

        public boolean getMoreResults(int current) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getMoreResults(current) : false;
        }

        public ResultSet getGeneratedKeys() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getGeneratedKeys() : null;
        }

        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.executeUpdate(sql, autoGeneratedKeys);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return 0;
            }
        }

        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.executeUpdate(sql, columnIndexes);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return 0;
            }
        }

        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.executeUpdate(sql, columnNames);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return 0;
            }
        }

        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.execute(sql, autoGeneratedKeys);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return false;
            }
        }

        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.execute(sql, columnIndexes);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return false;
            }
        }

        public boolean execute(String sql, String[] columnNames) throws SQLException {
            try {
                this.beforeDoExecute(sql);
                return this.realStatement.execute(sql, columnNames);
            } catch (Exception var4) {
                this.delegtConn.tcmanager.getDataSource().handleException(this.delegtConn, sql, var4);
                return false;
            }
        }

        public int getResultSetHoldability() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.getResultSetHoldability() : 0;
        }

        public boolean isClosed() throws SQLException {
            return this.isClosed.get();
        }

        public void setPoolable(boolean poolable) throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.setPoolable(poolable);
            } else {
                this.addAction("setPoolable", poolable);
            }

        }

        public boolean isPoolable() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            return this.realStatement != null ? this.realStatement.isPoolable() : (Boolean)DelegatingConnection.this.tcmanager.getPathTrackManager().getMethodValue(this.actionQueue, "setPoolable", Boolean.class, 0, false);
        }

        public void closeOnCompletion() throws SQLException {
            CommonUtil.checkState(this.isClosed.get(), "Statement is already closed!");
            if (this.realStatement != null) {
                this.realStatement.closeOnCompletion();
            }

        }

        public boolean isCloseOnCompletion() throws SQLException {
            return this.realStatement != null ? this.realStatement.isCloseOnCompletion() : false;
        }
    }
}
