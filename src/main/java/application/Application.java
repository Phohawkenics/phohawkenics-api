package application;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import controllers.HelloWorldController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.fasterxml.classmate.TypeResolver;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackageClasses = { HelloWorldController.class })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // http://springfox.github.io/springfox/docs/current/
    @Bean
    public Docket petApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                        .select()
                        .apis(RequestHandlerSelectors.basePackage( "controllers" ))
                        .paths(PathSelectors.any())
                        .build()
                        .pathMapping("/")
                        .directModelSubstitute(LocalDate.class, String.class)
                        .genericModelSubstitutes(ResponseEntity.class)
                        .alternateTypeRules(
                            newRule(
                                typeResolver.resolve(
                                    DeferredResult.class,
                                    typeResolver.resolve(ResponseEntity.class, WildcardType.class)
                                ),
                                typeResolver.resolve(WildcardType.class)
                            )
                        )
                        .useDefaultResponseMessages(false)
                        .globalResponseMessage(
                            RequestMethod.GET,
                            newArrayList(
                                new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build()
                            )
                        )
                        .securitySchemes(
                            newArrayList(apiKey())
                        )
                        .securityContexts(newArrayList(securityContext()));
    }

    @Autowired
    private TypeResolver typeResolver;

    private ApiKey apiKey() {
        return new ApiKey("mykey", "api_key", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/anyPath.*")).build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(new SecurityReference("mykey", authorizationScopes));
    }

    @Bean
    SecurityConfiguration security() {
        return new SecurityConfiguration("test-app-client-id", "test-app-realm", "test-app", "apiKey");
    }

    @Bean
    UiConfiguration uiConfig() {
        return new UiConfiguration("validatorUrl");
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

        };
    }

}