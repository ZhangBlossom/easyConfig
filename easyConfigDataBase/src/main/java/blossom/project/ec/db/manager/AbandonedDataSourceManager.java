//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.manager;

import blossom.project.ec.db.datasource.DelegatingDataSource;
import blossom.project.ec.db.util.CommonUtil;
import com.alibaba.druid.pool.DruidDataSource;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbandonedDataSourceManager extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AbandonedDataSourceManager.class);
    private volatile boolean isStoped = false;
    private BlockingQueue<DelegatingDataSource> abandonedQueue = new LinkedBlockingQueue();
    private long lastUpdateTimestamp = System.currentTimeMillis();
    private long DS_EXPIRE_TIME_SEC = 180L;

    public AbandonedDataSourceManager() {
    }

    public void close() {
        this.isStoped = true;
        if (!CommonUtil.isNullOrEmpty(this.abandonedQueue)) {
            logger.error("尚有被弃用的且未关闭的DataSource：{}", this.abandonedQueue.size());
            this.abandonedQueue.clear();
        }

    }

    private void putAbandonedDataSource(DelegatingDataSource dds) {
        if (dds != null && !this.abandonedQueue.offer(dds)) {
            dds.close();
        }

    }

    public void putAbandonedDataSource(List<DelegatingDataSource> ddsList) {
        if (!CommonUtil.isNullOrEmpty(ddsList)) {
            try {
                this.abandonedQueue.addAll(ddsList);
                this.lastUpdateTimestamp = System.currentTimeMillis();
            } catch (Exception var5) {
                logger.error("老链接放入回收队列失败", var5);
                Iterator var3 = ddsList.iterator();

                while(var3.hasNext()) {
                    DelegatingDataSource dds = (DelegatingDataSource)var3.next();
                    dds.close();
                }
            }
        }

    }

    public void run() {
        while(true) {
            try {
                if (!this.isStoped) {
                    DelegatingDataSource dds = (DelegatingDataSource)this.abandonedQueue.poll(3L, TimeUnit.SECONDS);
                    if (dds == null) {
                        continue;
                    }

                    DruidDataSource druidDataSource = dds.getUnderlyingDruidDataSource();
                    if (druidDataSource.getActiveCount() == 0) {
                        logger.info("无连接在使用，关闭连接池【{}】: 明细【{}】", druidDataSource.getID(), dds.toString());
                        dds.close();
                        continue;
                    }

                    if (this.isDataSourceExired()) {
                        logger.info("超过3分钟，强制关闭连接池【{}】: 明细【{}】", druidDataSource.getID(), dds.toString());
                        dds.close();
                        continue;
                    }

                    this.putAbandonedDataSource(dds);
                    continue;
                }
            } catch (Exception var3) {
                logger.error("[AbandonedDataSourceManager]: ", var3);
            }

            return;
        }
    }

    private boolean isDataSourceExired() {
        long span = (System.currentTimeMillis() - this.lastUpdateTimestamp) / 1000L;
        return span >= this.DS_EXPIRE_TIME_SEC;
    }
}
