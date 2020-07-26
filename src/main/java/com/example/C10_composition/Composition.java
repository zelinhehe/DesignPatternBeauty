package com.example.C10_composition;

public class Composition {
}

/**
 * 继承主要有三个作用：
 *      表示 is-a 关系，
 *      支持多态特性，
 *      代码复用。
 * 而这三个作用都可以通过其他技术手段来达成。比如
 *      is-a 关系，可以通过组合和接口的 has-a 关系来替代；
 *      多态特性 可以利用接口来实现；
 *      代码复用 可以通过组合和委托来实现。
 * 所以，从理论上讲，通过组合、接口、委托三个技术手段，完全可以替换掉继承，
 * 在项目中不用或者少用继承关系，特别是一些复杂的继承关系
 */
interface Flyable {
    void fly();
}

interface Tweetable {
    void tweet();
}

interface EggLayable {
    void layEgg();
}

class FlyAbility implements Flyable {
    @Override
    public void fly() {
        System.out.println("fly");
    }
}

class TweetAbility implements Tweetable {
    @Override
    public void tweet() {
        System.out.println("tweet");
    }
}

class EggLayAbility implements EggLayable {
    @Override
    public void layEgg() {
        System.out.println("layEgg");
    }
}

class Ostrich implements Tweetable, EggLayable {//鸵鸟
    private TweetAbility tweetAbility = new TweetAbility(); //组合
    private EggLayAbility eggLayAbility = new EggLayAbility(); //组合

    @Override
    public void tweet() {
        tweetAbility.tweet(); // 委托
    }

    @Override
    public void layEgg() {
        eggLayAbility.layEgg(); // 委托
    }
}
