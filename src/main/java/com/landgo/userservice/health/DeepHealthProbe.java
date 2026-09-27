package com.landgo.userservice.health;

/**
 * A dependency probe contributed to the deep health endpoint.
 *
 * <p>Implementations are picked up by Spring as beans, so adding a dependency
 * check means adding a bean and nothing else.
 *
 * <p>Deliberately NOT a Spring {@code HealthIndicator}: these probes must not
 * reach {@code /actuator/health}, which the load balancer polls.
 */
public interface DeepHealthProbe {

    String name();

    ProbeResult check();
}
