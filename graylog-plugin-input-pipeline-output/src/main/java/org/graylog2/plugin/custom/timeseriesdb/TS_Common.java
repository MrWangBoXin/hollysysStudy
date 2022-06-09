package org.graylog2.plugin.custom.timeseriesdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TS_Common {

    /**
     * String(yyyy-MM-dd HH:mm:ss) 转 Date
     *
     * @param time
     * @return
     * @throws ParseException
     */
    // String date = "2010/05/04 12:34:23";
    public static Date StringToDate(String time) throws ParseException {

        Date date = new Date();
        // 注意format的格式要与日期String的格式相匹配
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * String(yyyy-MM-dd HH:mm:ss) 转 Format Date
     *
     * @param time
     * @return
     * @throws ParseException
     */
    // String date = "2010/05/04 12:34:23";
    public static String StringToDateStr(String time) throws ParseException {
        String startTime = "";
        // 注意format的格式要与日期String的格式相匹配
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            startTime = sdf.parse(time).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startTime;
    }

    /**
     * 计算时间差，以秒为单位。
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getDistanceTime(Date startTime, Date endTime) {
        int seconds = 0;
        long time1 = startTime.getTime();
        long time2 = endTime.getTime();

        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        seconds = (int) (diff / (1000));
        return seconds;
    }

    /**
     * 根据IP地址进行逗号拆分为带下划线字符串
     *
     * @param strIpAddress ip地址
     * @return 用前缀t_和ip地址用_拆分的组合
     */
    public static String FormatIpAddress(String strIpAddress) {
        String strReturnValue = "";
        if (strIpAddress.indexOf('.') != -1) {
            //找到 IP 地址
            String[] IpAddressTmp = strIpAddress.split("\\.");
            for (int i = 0; i < IpAddressTmp.length; i++) {
                strReturnValue += IpAddressTmp[i] + "_";
            }
        }
        if (strReturnValue.endsWith("_"))
            strReturnValue = strReturnValue.substring(0, strReturnValue.length() - 1);
        return strReturnValue;
    }

    /**
     * 字符串转换为long
     *
     * @param time
     * @return
     */
    public static long StringToTimestamp(String time) {
        long times = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(time);
            times = date.getTime();
        } catch (Exception e) {
            return 0;
        }
        if (times == 0) {
            System.out.println("String转10位时间戳失败");
        }
        return times;
    }
}
