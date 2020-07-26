package com.example.C16_OCP;

// 告警规则，自由设置
public class AlertRule {

    public static class Rule {

        public long getMaxTps() {
            return 1;
        }

        public long getMaxErrorCount() {
            return 1;
        }

        public long getMaxTimeoutTps() {
            return 1;
        }
    }


    public Rule getMatchedRule(String api) {
        return new Rule();
    }
}