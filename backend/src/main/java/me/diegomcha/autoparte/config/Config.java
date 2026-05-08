package me.diegomcha.autoparte.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
@EnableJpaAuditing
public class Config implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // If the user asks for a real file (e.g., /static/logo.png), serve it.
                        if (requestedResource.exists() && requestedResource.isReadable())
                            return requestedResource;

                        // Never send API requests to the frontend fallback.
                        if (resourcePath.equals("api") || resourcePath.startsWith("api/"))
                            return null;

                        // If the file doesn't exist, it's likely a React Router path
                        // Serve index.html so the React app can load and handle the routing.
                        return location.createRelative("index.html");
                    }
                });
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v{version}", handlerType ->
                handlerType.isAnnotationPresent(RestController.class)
        );
    }
}
