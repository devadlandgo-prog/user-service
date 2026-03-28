package com.landgo.userservice.factory;

import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.strategy.OAuth2AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuth2StrategyFactory {

    private final Map<AuthProvider, OAuth2AuthenticationStrategy> strategies;

    public OAuth2StrategyFactory(List<OAuth2AuthenticationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OAuth2AuthenticationStrategy::getProvider, Function.identity()));
    }

    public OAuth2AuthenticationStrategy getStrategy(AuthProvider provider) {
        OAuth2AuthenticationStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        }
        return strategy;
    }
}
