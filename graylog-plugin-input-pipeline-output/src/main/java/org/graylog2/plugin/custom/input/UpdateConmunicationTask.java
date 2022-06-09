package org.graylog2.plugin.custom.input;

import org.graylog2.plugin.custom.timeseriesdb.TS_PgOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class UpdateConmunicationTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateConmunicationTask.class);
    private static int interval = 300000*1000;

    public UpdateConmunicationTask(int interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        LOG.info("update device UpdateConmunicationTask task............");
        try {
            DeleteQuintuple();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int DeleteQuintuple() throws FileNotFoundException, SQLException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            String strDate = formatter.format(now.getTime() - 120000);
            String strSQL = String.format("DELETE FROM \"CommunicationRelationship\" WHERE \"updatedAt\" < \'%s\'",strDate);
            LOG.info("DeleteQuintuple strSQL is:{}",strSQL);
            int resultSet = TS_PgOpt.Delete(strSQL);
            if (resultSet == -1) {
                System.out.println("执行语句错误：" + strSQL);
                return -1;
            }
            System.out.println("PG Insert SysAlert OK\n");
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }
}
