package C08_abstract_interface;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

// 抽象类
abstract class Logger {
    private String name;
    private boolean enabled;
    private Level minPermittedLevel;

    public Logger(String name, boolean enabled, Level minPermittedLevel) {
        this.name = name;
        this.enabled = enabled;
        this.minPermittedLevel = minPermittedLevel;
    }

    public void log(Level level, String message) {
        boolean loggable = enabled && (minPermittedLevel.intValue() <= level.intValue());
        if (!loggable) return;
        doLog(level, message);
    }

    public abstract void doLog(Level level, String message);
}

// 抽象类的子类：输出日志到文件
class FileLogger extends Logger {
    private Writer fileWriter;

    public FileLogger(String name, boolean enabled, Level minPermittedLevel,
                      String filePath) throws IOException {
        super(name, enabled, minPermittedLevel);
        this.fileWriter = new FileWriter(filePath);
    }

    @Override
    public void doLog(Level level, String message) {
        try {  // 格式化level和message,输出到日志文件
            fileWriter.write(level.toString() + " " + message);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

interface MessageQueueClient {
    void send(String msg);
}

class MessageQueueLogger extends Logger {
    private MessageQueueClient messageQueueClient;

    public MessageQueueLogger(String name, boolean enabled, Level minPermittedLevel,
                              MessageQueueClient messageQueueClient) {
        super(name, enabled, minPermittedLevel);
        this.messageQueueClient = messageQueueClient;
    }

    @Override
    public void doLog(Level level, String message) {
        // 格式化level和message,输出到消息中间件
        this.messageQueueClient.send(level.toString() + " " + message);
    }
}

class KafkaClient implements MessageQueueClient {
    @Override
    public void send(String msg) {
        System.out.println("Kafka: " + msg);
    }
}

public class LoggerDemo {

    public static void main(String[] args) throws IOException {
        Logger fileLogger = new FileLogger("fileLogger", true, Level.INFO, "fileLog.log");
        fileLogger.log(Level.WARNING, "this is a log");

        Logger messageQueueLogger = new MessageQueueLogger("messageQueueLogger", true, Level.INFO, new KafkaClient());
        messageQueueLogger.log(Level.INFO, "this is a log");
    }
}
