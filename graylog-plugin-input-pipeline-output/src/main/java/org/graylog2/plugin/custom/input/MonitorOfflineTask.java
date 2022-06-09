package org.graylog2.plugin.custom.input;

import org.graylog2.plugin.custom.graphql.CGraphql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class MonitorOfflineTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorOfflineTask.class);
    private static int offine_interval = 300000;

    public MonitorOfflineTask(int offineinterval) {
        this.offine_interval = offineinterval;
    }

    @Override
    public void run() {
        LOG.info("update device status offline task............");
        try {
            CGraphql.GraphqlUpdateDevOffline(offine_interval*1000);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}