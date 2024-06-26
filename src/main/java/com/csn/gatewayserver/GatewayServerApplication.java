package com.csn.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServerApplication.class, args);
	}

	@Bean
	public RouteLocator csnRouteConfig(RouteLocatorBuilder routeLocatorBuilder){
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/csn/post/**") // Specifies for any request whose	path starts with this will apply.
						.filters(f -> f.rewritePath("/csn/post/(?<segment>.*)","/${segment}") // rewrite path.
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("postCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://POST-SERVICE")) // forwarding to ms named as POST-SERVICE in eureka
				.route(p -> p
						.path("/csn/comment/**")
						.filters(f -> f.rewritePath("/csn/comment/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.retry(retryConfig -> retryConfig.setRetries(3)
										.setMethods(HttpMethod.GET)
										.setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true)))
						.uri("lb://COMMENT-SERVICE"))
				.route(p -> p
						.path("/csn/storage/**")
						.filters(f -> f.rewritePath("/csn/storage/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.retry(retryConfig -> retryConfig.setRetries(3)
										.setMethods(HttpMethod.GET)
										.setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true)))
						.uri("lb://STORAGE-SERVICE")).build();
	}

}
