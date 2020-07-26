package com.example.C19_IoC;


import java.util.ArrayList;
import java.util.List;

/**
 * 所有的流程都由程序员来控制
 */
public class UserServiceTest {
    public static boolean doTest() {
        // 被测试代码
        return true;
    }

    public static void main(String[] args) {  // 这部分逻辑可以放到框架中
        if (doTest()) {
            System.out.println("Test succeed.");
        } else {
            System.out.println("Test failed.");
        }
    }
}

/**
 * 抽象出一个下面这样一个框架，我们再来看，如何利用框架来实现同样的功能
 */
abstract class TestCase {
    public void run() {
        if (doTest()) {
            System.out.println("Test succeed.");
        } else {
            System.out.println("Test failed.");
        }
    }

    public abstract boolean doTest();
}

class JunitApplication {
    private static final List<TestCase> testCases = new ArrayList<>();

    public static void register(TestCase testCase) {
        testCases.add(testCase);
    }

    public static void start() {
        for (TestCase c : testCases){
            c.run();
        }
    }
}

/**
 * 把这个简化版本的测试框架引入到工程中之后，我们只需要在框架预留的扩展点，
 * 也就是 TestCase 类中的 doTest() 抽象函数中，填充具体的测试代码就可以实现之前的功能了，
 * 完全不需要写负责执行流程的 main() 函数了。
 */

class UserServiceTest2 extends TestCase {
    @Override
    public boolean doTest() {
        // 被测试代码
        return true;
    }

    public static void main(String[] args) {
        // 注册操作还可以通过配置的方式来实现，不需要程序员显示调用register()
        JunitApplication.register(new UserServiceTest2());
        JunitApplication.start();
    }
}

