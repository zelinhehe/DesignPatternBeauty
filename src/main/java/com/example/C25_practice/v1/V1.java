package com.example.C25_practice.v1;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 四个模块：数据采集、存储、聚合统计、显示。
 * <p>
 * 数据采集：负责打点采集原始数据，包括记录每次接口请求的响应时间和请求时间。
 * <p>
 * 存储：负责将采集的原始数据保存下来，以便之后做聚合统计。
 * 数据的存储方式有很多种，我们暂时只支持 Redis 这一种存储方式，并且，采集与存储两个过程同步执行。
 * <p>
 * 聚合统计：负责将原始数据聚合为统计数据，包括响应时间的最大值、最小值、平均值、99.9 百分位值、99 百分位值，以及接口请求的次数和 tps。
 * <p>
 * 显示：负责将统计数据以某种格式显示到终端，暂时只支持主动推送给命令行和邮件。命令行间隔 n 秒统计显示上 m 秒的数据（比如，间隔 60s 统计上 60s 的数据）。邮件每日统计上日的数据。
 */

public class V1 {

    public static void main(String[] args) {
        MetricsStorage storage = new RedisMetricsStorage();

        ConsoleReporter consoleReporter = new ConsoleReporter(storage);
        consoleReporter.startRepeatedReport(5, 60);

        EmailReporter emailReporter = new EmailReporter(storage);
        emailReporter.addToAddress("wangzheng@xzg.com");
        emailReporter.startDailyReport();

        MetricsCollector collector = new MetricsCollector(storage);
        collector.recordRequest(new RequestInfo("register", 123, 10234));
        collector.recordRequest(new RequestInfo("register", 223, 11234));
        collector.recordRequest(new RequestInfo("register", 323, 12334));
        collector.recordRequest(new RequestInfo("login", 23, 12434));
        collector.recordRequest(new RequestInfo("login", 1223, 14234));

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 1. 数据采集 - 负责提供 API，来采集接口请求的原始数据。
 */
class MetricsCollector {
    private MetricsStorage metricsStorage;  //基于接口而非实现编程

    //依赖注入
    public MetricsCollector(MetricsStorage metricsStorage) {
        this.metricsStorage = metricsStorage;
    }

    //用一个函数代替了最小原型中的两个函数
    public void recordRequest(RequestInfo requestInfo) {
        if (requestInfo == null || StringUtils.isBlank(requestInfo.getApiName())) {
            return;
        }
        metricsStorage.saveRequestInfo(requestInfo);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class RequestInfo {
    private String apiName;
    private double responseTime;
    private long timestamp;
}

/**
 * 2. 存储 - 负责原始数据存储
 */
interface MetricsStorage {
    void saveRequestInfo(RequestInfo requestInfo);

    List<RequestInfo> getRequestInfos(String apiName, long startTimeInMillis, long endTimeInMillis);

    Map<String, List<RequestInfo>> getRequestInfos(long startTimeInMillis, long endTimeInMillis);
}

/**
 * 负责原始数据存储 - Redis
 */
class RedisMetricsStorage implements MetricsStorage {

    private Map<String, List<RequestInfo>> map = new HashMap<>();

    @Override
    public void saveRequestInfo(RequestInfo requestInfo) {
        map.putIfAbsent(requestInfo.getApiName(), new ArrayList<>());
        map.get(requestInfo.getApiName()).add(requestInfo);
    }

    @Override
    public List<RequestInfo> getRequestInfos(String apiName, long startTimestamp, long endTimestamp) {
        return map.get(apiName);
    }

    @Override
    public Map<String, List<RequestInfo>> getRequestInfos(long startTimestamp, long endTimestamp) {
        return map;
    }
}

/**
 * 3. 聚合统计 - 负责根据原始数据计算统计数据
 */
class Aggregator {
    public static RequestStat aggregate(List<RequestInfo> requestInfos, long durationInMillis) {
        double maxRespTime = Double.MIN_VALUE;
        double minRespTime = Double.MAX_VALUE;
        double avgRespTime = -1;
        double p999RespTime = -1;
        double p99RespTime = -1;
        double sumRespTime = 0;
        long count = 0;
        for (RequestInfo requestInfo : requestInfos) {
            ++count;
            double respTime = requestInfo.getResponseTime();
            if (maxRespTime < respTime) {
                maxRespTime = respTime;
            }
            if (minRespTime > respTime) {
                minRespTime = respTime;
            }
            sumRespTime += respTime;
        }
        if (count != 0) {
            avgRespTime = sumRespTime / count;
        }
        long tps = count / durationInMillis * 1000;
        requestInfos.sort(new Comparator<RequestInfo>() {
            @Override
            public int compare(RequestInfo o1, RequestInfo o2) {
                double diff = o1.getResponseTime() - o2.getResponseTime();
                return Double.compare(diff, 0.0);
            }
        });
        int idx999 = (int) (count * 0.999);
        int idx99 = (int) (count * 0.99);
        if (count != 0) {
            p999RespTime = requestInfos.get(idx999).getResponseTime();
            p99RespTime = requestInfos.get(idx99).getResponseTime();
        }
        RequestStat requestStat = new RequestStat();
        requestStat.setMaxResponseTime(maxRespTime);
        requestStat.setMinResponseTime(minRespTime);
        requestStat.setAvgResponseTime(avgRespTime);
        requestStat.setP999ResponseTime(p999RespTime);
        requestStat.setP99ResponseTime(p99RespTime);
        requestStat.setCount(count);
        requestStat.setTps(tps);
        return requestStat;
    }
}

@Data
class RequestStat {
    private double maxResponseTime;
    private double minResponseTime;
    private double avgResponseTime;
    private double p999ResponseTime;
    private double p99ResponseTime;
    private long count;
    private long tps;
}

/**
 * 4. 显示 - 终端
 */
class ConsoleReporter {
    private MetricsStorage metricsStorage;
    private ScheduledExecutorService executor;

    public ConsoleReporter(MetricsStorage metricsStorage) {
        this.metricsStorage = metricsStorage;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    // 第4个代码逻辑：定时触发第1、2、3代码逻辑的执行；
    public void startRepeatedReport(long periodInSeconds, long durationInSeconds) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 第1个代码逻辑：根据给定的时间区间，从数据库中拉取数据；
                long durationInMillis = durationInSeconds * 1000;
                long endTimeInMillis = System.currentTimeMillis();
                long startTimeInMillis = endTimeInMillis - durationInMillis;
                Map<String, List<RequestInfo>> requestInfos =
                        metricsStorage.getRequestInfos(startTimeInMillis, endTimeInMillis);
                Map<String, RequestStat> stats = new HashMap<>();
                for (Map.Entry<String, List<RequestInfo>> entry : requestInfos.entrySet()) {
                    String apiName = entry.getKey();
                    List<RequestInfo> requestInfosPerApi = entry.getValue();
                    // 第2个代码逻辑：根据原始数据，计算得到统计数据；
                    RequestStat requestStat = Aggregator.aggregate(requestInfosPerApi, durationInMillis);
                    stats.put(apiName, requestStat);
                }
                // 第3个代码逻辑：将统计数据显示到终端（命令行或邮件）；
                System.out.println("Time Span: [" + startTimeInMillis + ", " + endTimeInMillis + "]");
                Gson gson = new Gson();
                System.out.println(gson.toJson(stats));
            }
        }, 0, periodInSeconds, TimeUnit.SECONDS);
    }
}

/**
 * 4. 显示 - 邮件
 */
class EmailReporter {
    private static final Long DAY_HOURS_IN_SECONDS = 86400L;

    private MetricsStorage metricsStorage;
    private EmailSender emailSender;
    private List<String> toAddresses = new ArrayList<>();

    public EmailReporter(MetricsStorage metricsStorage) {
        this(metricsStorage, new EmailSender(/*省略参数*/));
    }

    public EmailReporter(MetricsStorage metricsStorage, EmailSender emailSender) {
        this.metricsStorage = metricsStorage;
        this.emailSender = emailSender;
    }

    public void addToAddress(String address) {
        toAddresses.add(address);
    }

    public void startDailyReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date firstTime = calendar.getTime();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                long durationInMillis = DAY_HOURS_IN_SECONDS * 1000;
                long endTimeInMillis = System.currentTimeMillis();
                long startTimeInMillis = endTimeInMillis - durationInMillis;
                Map<String, List<RequestInfo>> requestInfos =
                        metricsStorage.getRequestInfos(startTimeInMillis, endTimeInMillis);
                Map<String, RequestStat> stats = new HashMap<>();
                for (Map.Entry<String, List<RequestInfo>> entry : requestInfos.entrySet()) {
                    String apiName = entry.getKey();
                    List<RequestInfo> requestInfosPerApi = entry.getValue();
                    RequestStat requestStat = Aggregator.aggregate(requestInfosPerApi, durationInMillis);
                    stats.put(apiName, requestStat);
                }
                // TODO: 格式化为html格式，并且发送邮件
            }
        }, firstTime, DAY_HOURS_IN_SECONDS * 1000);
    }
}

class EmailSender {
}

/**
 * Review 设计与实现
 *
 * 我们前面讲到了 SOLID、KISS、DRY、YAGNI、LOD 等设计原则，
 * 基于接口而非实现编程、多用组合少用继承、高内聚低耦合等设计思想。
 * 我们现在就来看下，上面的代码实现是否符合这些设计原则和思想。
 *
 * MetricsCollector
 * 负责采集和存储数据，职责相对来说还算比较单一。
 * 它基于接口而非实现编程，通过依赖注入的方式来传递 MetricsStorage 对象，
 * 可以在不需要修改代码的情况下，灵活地替换不同的存储方式，满足开闭原则。
 *
 * MetricsStorage、RedisMetricsStorage
 * 设计比较简单。当我们需要实现新的存储方式的时候，只需要实现 MetricsStorage 接口即可。
 * 因为所有用到 MetricsStorage 和 RedisMetricsStorage 的地方，都是基于相同的接口函数来编程的，
 * 所以，除了在组装类的地方有所改动（从 RedisMetricsStorage 改为新的存储实现类），
 * 其他接口函数调用的地方都不需要改动，满足开闭原则。
 *
 * Aggregator
 * 是一个工具类，里面只有一个静态函数，有 50 行左右的代码量，负责各种统计数据的计算。
 * 当需要扩展新的统计功能的时候，需要修改 aggregate() 函数代码，
 * 并且一旦越来越多的统计功能添加进来之后，这个函数的代码量会持续增加，可读性、可维护性就变差了。
 * 所以，从刚刚的分析来看，这个类的设计可能存在职责不够单一、不易扩展等问题，需要在之后的版本中，对其结构做优化。
 *
 * ConsoleReporter、EmailReporter
 * 存在代码重复问题。在这两个类中，从数据库中取数据、做统计的逻辑都是相同的，可以抽取出来复用，否则就违反了 DRY 原则。
 * 而且整个类负责的事情比较多，职责不是太单一。
 * 特别是显示部分的代码，可能会比较复杂（比如 Email 的展示方式），最好是将显示部分的代码逻辑拆分成独立的类。
 * 除此之外，因为代码中涉及线程操作，并且调用了 Aggregator 的静态函数，所以代码的可测试性不好。
 */
