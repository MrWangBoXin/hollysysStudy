package graylog.bean.reflect;

/**
 * @BelongsProject: twoday
 * @BelongsPackage: graylog.bean.reflect
 * @Author: wangboxin
 * @CreateTime: 2022-05-31  09:02
 * @Description: TODO
 * @Version: 1.0
 */
import java.lang.reflect.Field;
import java.util.Date;
import java.util.TimerTask;

/**
 * SendMailTask
 */
public abstract class SendMailTask extends TimerTask {

    long period = 0;

    public void setPeriod(long period) {
        Date now = new Date();
        // 设置下一次执行时间i
        long nextExecutionTime = now.getTime() + period;
        setDeclaredField(TimerTask.class, this, "nextExecutionTime", nextExecutionTime);
        // 修改执行周期
        setDeclaredField(TimerTask.class, this, "period", period);
    }

    //通过反射修改字段的值
    static boolean setDeclaredField(Class<?> clazz, Object obj, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

