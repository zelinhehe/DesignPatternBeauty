package C16_OCP.OCP;

import C16_OCP.AlertRule;
import C16_OCP.Notification;
import C16_OCP.NotificationEmergencyLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * 上面的代码改动是基于“修改”的方式来实现新功能的。
 * 如果遵循开闭原则，也就是“对扩展开放、对修改关闭”。那如何通过“扩展”的方式，来实现同样的功能呢？
 * 先重构 Alert 代码，让它的扩展性更好。重构包含两部分：
 *      一：将 check() 函数的多个入参封装成 ApiStatInfo 类；
 *      二：引入 handler 的概念，将 if 判断逻辑分散在各个 handler 中。
 */
public class Alert {
    private List<AlertHandler> alertHandlers = new ArrayList<>();

    public void addAlertHandler(AlertHandler alertHandler) {
        this.alertHandlers.add(alertHandler);
    }

    public void check(ApiStatInfo apiStatInfo) {
        for (AlertHandler handler : alertHandlers) {
            handler.check(apiStatInfo);
        }
    }
}

class ApiStatInfo {
    private String api;
    private long requestCount;
    private long errorCount;
    private long durationOfSeconds;

    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }
    public long getRequestCount() { return requestCount; }
    public void setRequestCount(long requestCount) { this.requestCount = requestCount; }
    public long getErrorCount() { return errorCount; }
    public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
    public long getDurationOfSeconds() { return durationOfSeconds; }
    public void setDurationOfSeconds(long durationOfSeconds) { this.durationOfSeconds = durationOfSeconds; }
}

abstract class AlertHandler {
    protected AlertRule rule;
    protected Notification notification;

    public AlertHandler(AlertRule rule, Notification notification) {
        this.rule = rule;
        this.notification = notification;
    }

    public abstract void check(ApiStatInfo apiStatInfo);
}

class TpsAlertHandler extends AlertHandler {
    public TpsAlertHandler(AlertRule rule, Notification notification) {
        super(rule, notification);
    }
    @Override
    public void check(ApiStatInfo apiStatInfo) {
        long tps = apiStatInfo.getRequestCount() / apiStatInfo.getDurationOfSeconds();
        if (tps > rule.getMatchedRule(apiStatInfo.getApi()).getMaxTps()) {
            notification.notify(NotificationEmergencyLevel.URGENCY, "...");
        }
    }
}

class ErrorAlertHandler extends AlertHandler {
    public ErrorAlertHandler(AlertRule rule, Notification notification) {
        super(rule, notification);
    }
    @Override
    public void check(ApiStatInfo apiStatInfo) {
        if (apiStatInfo.getErrorCount() > rule.getMatchedRule(apiStatInfo.getApi()).getMaxErrorCount()) {
            notification.notify(NotificationEmergencyLevel.SEVERE, "...");
        }
    }
}

/**
 * 使用重构后的 Alert
 * ApplicationContext 是一个单例类，
 * 负责 Alert 的
 *      创建、
 *      组装（alertRule和notification的依赖注入）、
 *      初始化（添加 handlers）工作
 */
class ApplicationContext {
    private AlertRule alertRule;
    private Notification notification;
    private Alert alert;

    public void initializeBeans() {
        alertRule = new AlertRule(/*.省略参数.*/);
        notification = new Notification(/*.省略参数.*/);
        alert = new Alert();
        alert.addAlertHandler(new TpsAlertHandler(alertRule, notification));
        alert.addAlertHandler(new ErrorAlertHandler(alertRule, notification));
    }
    public Alert getAlert() { return alert; }

    // 饿汉式单例
    private static final ApplicationContext instance = new ApplicationContext();
    private ApplicationContext() {
        instance.initializeBeans();
    }
    public static ApplicationContext getInstance() {
        return instance;
    }
}

class Demo {
    public static void main(String[] args) {
        ApiStatInfo apiStatInfo = new ApiStatInfo();
        // ...省略设置apiStatInfo数据值的代码
        ApplicationContext.getInstance().getAlert().check(apiStatInfo);
    }
}

/**
 * 基于重构之后的代码，在添加新功能：每秒钟接口超时请求个数超过某个最大阈值就告警，我们又该如何改动代码呢？
 * 主要的改动有下面四处。
 *  一：在 ApiStatInfo 类中添加属性 timeoutCount
 *  二：添加 TimeoutAlertHandler 类
 *  三：在 ApplicationContext 类的 initializeBeans() 方法中，往 alert 对象中注册新的 timeoutAlertHandler。
 *  四：在使用 Alert 类的时候，需要给 check() 函数的入参 apiStatInfo 对象设置 timeoutCount 的值。
 */

//public class Alert {  // 代码未改动...
//}

//public class ApiStatInfo {  // 省略constructor/getter/setter方法
//    private String api;
//    private long requestCount;
//    private long errorCount;
//    private long durationOfSeconds;
/** 改动一：添加新字段 */
//    private long timeoutCount;
//}

//public abstract class AlertHandler {  // 代码未改动...
//}
//public class TpsAlertHandler extends AlertHandler {  // 代码未改动...
//}
//public class ErrorAlertHandler extends AlertHandler {  // 代码未改动...
//}
/** 改动二：添加新的handler */
//public class TimeoutAlertHandler extends AlertHandler {  // 省略代码...
//}

//public class ApplicationContext {
//    private AlertRule alertRule;
//    private Notification notification;
//    private Alert alert;
//
//    public void initializeBeans() {
//        alertRule = new AlertRule(/*.省略参数.*/); //省略一些初始化代码
//        notification = new Notification(/*.省略参数.*/); //省略一些初始化代码
//        alert = new Alert();
//        alert.addAlertHandler(new TpsAlertHandler(alertRule, notification));
//        alert.addAlertHandler(new ErrorAlertHandler(alertRule, notification));
          /** 改动三：注册handler */
//        alert.addAlertHandler(new TimeoutAlertHandler(alertRule, notification));
//    }
//    //...省略其他未改动代码...
//}

//public class Demo {
//    public static void main(String[] args) {
//        ApiStatInfo apiStatInfo = new ApiStatInfo();
//        // ...省略apiStatInfo的set字段代码
/** 改动四：设置timeoutCount值 */
//        apiStatInfo.setTimeoutCount(289);
//        ApplicationContext.getInstance().getAlert().check(apiStatInfo);
//    }
//}

/**
 * 重构之后的代码更加灵活和易扩展。
 *  一：如果想添加新的告警逻辑，只需要基于'扩展'的方式创建新的 handler 类即可，不需要改动原来的 check() 函数的逻辑。
 *  二：我们只需要为新的 handler 类添加单元测试，老的单元测试都不会失败，也不用修改。
 */
