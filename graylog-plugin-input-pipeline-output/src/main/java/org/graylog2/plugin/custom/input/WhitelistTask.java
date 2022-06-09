package org.graylog2.plugin.custom.input;

import org.graylog2.plugin.custom.graphql.CGraphql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhitelistTask implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(WhitelistTask.class);

    @Override
    public void run() {
        LOG.info("update asset whitelist............");
        try{
            CGraphql.updateWhitelist();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
