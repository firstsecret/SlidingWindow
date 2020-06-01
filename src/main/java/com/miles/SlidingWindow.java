package com.miles;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author `70miles
 */
public class SlidingWindow {
    private Map<Long, Long> counter = new ConcurrentHashMap<>();

    private static ScheduledExecutorService scheduledExecutorService;

    private Integer overflow = 15;

    private Integer windowLen = 5;

    private static ScheduledExecutorService cleanExecutorService;

    static {
        scheduledExecutorService= new ScheduledThreadPoolExecutor(5);
        cleanExecutorService = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * 滑窗
     * 每秒累加前5s内每秒的请求数量，判断是否超出阈值
     */
    public void mockSlidingWindow() {
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            try {
                long time = System.currentTimeMillis() / 1000;
                //每秒发送随机数量的请求
                int reqs = (int) (Math.random() * 5) + 1;
                System.out.println("============= one time start =============");
                System.out.println("mock req num:" + reqs);
                Long reqVal = counter.getOrDefault(time, 0L);
                reqVal += reqs;
                counter.put(time, reqVal);
                System.out.println("mock val insert:" + counter.get(time));
                long nums = 0;
                for (int i = 0; i < windowLen; i++) {
                    nums += counter.getOrDefault(time - i,0L);
                }
                System.out.println("当前流量：" + nums);
                if (nums > overflow) {
                    System.out.println("限流了,nums=" + nums);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("system err, " + e.getStackTrace());
            } finally {
                System.out.println("============= one time end =============");
            }
        },5000,1000,TimeUnit.MILLISECONDS);

        // clean
        cleanTask();
    }

    private void cleanTask(){
        cleanExecutorService.scheduleWithFixedDelay(()->{
            long time = System.currentTimeMillis() / 1000;
            time -= windowLen * 2;
            for (int i=10; i>0;i--){
                counter.remove(time - i);
            }
        },15,10,TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        // mock request
        SlidingWindow windowLimit = new SlidingWindow();
        windowLimit.mockSlidingWindow();
    }
}
