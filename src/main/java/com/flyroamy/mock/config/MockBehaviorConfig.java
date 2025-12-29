package com.flyroamy.mock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mock")
public class MockBehaviorConfig {

    private Latency latency = new Latency();
    private Failure failure = new Failure();
    private AutoExpire autoExpire = new AutoExpire();

    public static class Latency {
        private boolean enabled = false;
        private int minMs = 50;
        private int maxMs = 200;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMinMs() { return minMs; }
        public void setMinMs(int minMs) { this.minMs = minMs; }
        public int getMaxMs() { return maxMs; }
        public void setMaxMs(int maxMs) { this.maxMs = maxMs; }
    }

    public static class Failure {
        private double rate = 0.0;
        private List<String> types = List.of("TIMEOUT", "SERVER_ERROR");

        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }

    public static class AutoExpire {
        private boolean enabled = false;
        private int seconds = 300;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getSeconds() { return seconds; }
        public void setSeconds(int seconds) { this.seconds = seconds; }
    }

    public Latency getLatency() { return latency; }
    public void setLatency(Latency latency) { this.latency = latency; }
    public Failure getFailure() { return failure; }
    public void setFailure(Failure failure) { this.failure = failure; }
    public AutoExpire getAutoExpire() { return autoExpire; }
    public void setAutoExpire(AutoExpire autoExpire) { this.autoExpire = autoExpire; }
}
