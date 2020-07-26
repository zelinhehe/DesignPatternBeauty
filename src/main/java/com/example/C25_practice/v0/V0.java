package com.example.C25_practice.v0;


import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class V0 {
    public static void main(String[] args) {
        UserController controller = new UserController();
        for (int i = 0; i < 100; i++) {
            controller.login();
            if (i % 2 == 1) {
                controller.register();
            }
        }
    }
}

class Metrics {
    // Map的key是接口名称，value对应接口请求的响应时间或时间戳；
    private Map<String, List<Double>> responseTimes = new HashMap<>();
    private Map<String, List<Double>> timestamps = new HashMap<>();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void recordResponseTime(String apiName, double responseTime) {
        responseTimes.putIfAbsent(apiName, new ArrayList<>());
        responseTimes.get(apiName).add(responseTime);
    }

    public void recordTimestamp(String apiName, double timestamp) {
        timestamps.putIfAbsent(apiName, new ArrayList<>());
        timestamps.get(apiName).add(timestamp);
    }

    public void startRepeatedReport(long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                Map<String, Map<String, Double>> stats = new HashMap<>();
                for (Map.Entry<String, List<Double>> entry : responseTimes.entrySet()) {
                    String apiName = entry.getKey();
                    List<Double> apiRespTimes = entry.getValue();
                    stats.putIfAbsent(apiName, new HashMap<>());
                    stats.get(apiName).put("max", max(apiRespTimes));
                    stats.get(apiName).put("avg", avg(apiRespTimes));
                }

                for (Map.Entry<String, List<Double>> entry : timestamps.entrySet()) {
                    String apiName = entry.getKey();
                    List<Double> apiTimestamps = entry.getValue();
                    stats.putIfAbsent(apiName, new HashMap<>());
                    stats.get(apiName).put("count", (double) apiTimestamps.size());
                }
                System.out.println(gson.toJson(stats));
            }
        }, 0, period, unit);
    }

    private double max(List<Double> dataset) {
        double m = 0;
        for (double i: dataset) {
            if (i > m) {
                m = i;
            }
        }
        return m;
    }

    private double avg(List<Double> dataset) {//省略代码实现
        double a = 0;
        for (double i: dataset) {
                a += i;
        }
        return a / dataset.size();
    }
}


//应用场景：统计下面两个接口(注册和登录）的响应时间和访问次数
class UserController {
    private Metrics metrics = new Metrics();

    public UserController() {
        metrics.startRepeatedReport(5, TimeUnit.SECONDS);
    }

    public void register() {
        long startTimestamp = System.currentTimeMillis();
        metrics.recordTimestamp("register", startTimestamp);
        //...
        business("register");

        long respTime = System.currentTimeMillis() - startTimestamp;
        metrics.recordResponseTime("register", respTime);
    }

    public void login() {
        long startTimestamp = System.currentTimeMillis();
        metrics.recordTimestamp("login", startTimestamp);
        //...
        business("login");

        long respTime = System.currentTimeMillis() - startTimestamp;
        metrics.recordResponseTime("login", respTime);
    }

    private void business(String action) {
        Random random = new Random();
        int nextInt = random.nextInt(5);
        System.out.println(action + ": " + nextInt);
        try {
            Thread.sleep(1000 * nextInt);
        } catch (InterruptedException ignored) {
        }

    }
}
