package com.keepguard.ms_auth;

import com.keepguard.lib_common.config.MetricsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.keepguard.ms_auth", "com.keepguard.lib_common"})
@EnableFeignClients(basePackages = "com.keepguard.ms_auth.adapters.out.feign")
@Import(MetricsConfig.class)
public class MsAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthApplication.class, args);
	}

}
