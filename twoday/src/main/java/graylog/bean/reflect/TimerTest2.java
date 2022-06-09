package graylog.bean.reflect;

import java.time.LocalDateTime;
import java.util.Timer;

/**
 * @BelongsProject: twoday
 * @BelongsPackage: graylog.bean.reflect
 * @Author: wangboxin
 * @CreateTime: 2022-05-31  09:03
 * @Description: TODO
 * @Version: 1.0
 */


public class TimerTest2 {
    static int i = 0;
    static int k = 0;//显示第几次输出
    static int n = 0;

    public static void main(String[] args) {
        int total = 10;
        int pause = 5;
        int interval = 1;
        // timer
        Timer timer = new Timer(); // 定时任务
        SendMailTask task = new SendMailTask() {
            int count = 0; // 循环计数器：总循环打印total次
            @Override
            public synchronized void run() {
                ++k;
                ++n;
                System.out.println("hello，这是第"+k+"次输出！当前时间："+ LocalDateTime.now());
                i++;
                count++;
                if (count == total) {
                    System.err.println("完成时间: " + LocalDateTime.now());
                    timer.cancel(); // 取消定时器
                }
                // 每发完pause，暂停一段时间
                if (count % pause == 0 && count != total) {
                    try {
                        System.out.println("打印完" + pause + "条了," + interval + "分钟后继续: " + LocalDateTime.now());
                        n = 0;
                        Thread.sleep(interval * 60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(n > 0 && n % 3 == 0) {
                    try {
                        n = 0;
                        System.out.println("2秒后继续。。。。"+ LocalDateTime.now());
                        Thread.sleep(2000);
                        //wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 修改任务周期
                if(i>0 && i % 3 == 0) {
                    setPeriod(5000);
                    System.out.println("间隔修改为5秒。。。"+ LocalDateTime.now());
                }
            }
        };
        timer.schedule(task, 2000, 3000);
    }
}
