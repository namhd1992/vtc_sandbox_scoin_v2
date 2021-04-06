package com.vtc.gateway.scoinv2api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.vtc"})
@Configuration
@EnableJpaRepositories(basePackages = {"com.vtc"})
@EntityScan("com.vtc.*")
@ComponentScan("com.vtc.*")
@PropertySource("classpath:common.properties")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//
//        };
//    }

//    @Bean
//    public Docket swaggerSettings() {
//      ParameterBuilder aParameterBuilder = new ParameterBuilder();
//      aParameterBuilder.name("access_token").modelRef(new ModelRef("string")).parameterType("header").required(false)
//          .build();
//      List<Parameter> aParameters = new ArrayList<Parameter>();
//      aParameters.add(aParameterBuilder.build());
//      return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
//          .paths(PathSelectors.any()).build().pathMapping("/").globalOperationParameters(aParameters);
//    }

}
