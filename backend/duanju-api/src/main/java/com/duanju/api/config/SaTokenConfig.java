package com.duanju.api.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${duanju.upload.local-path:/data/uploads}")
    private String uploadLocalPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle ->
            SaRouter.match(
                    "/api/user/**",
                    "/api/wallet/**",
                    "/api/upload",
                    "/api/vip/buy",
                    "/api/usable/buy",
                    "/api/reseller/buy",
                    "/api/reseller/bind",
                    "/api/video/favorite",
                    "/api/video/favorite/remove"
            ).check(r -> StpUtil.checkLogin())
        ) {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // OPTIONS 预检请求直接放行，不做鉴权
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
                return super.preHandle(request, response, handler);
            }
        })
        .addPathPatterns("/**")
        .excludePathPatterns(
                "/api/auth/**",
                "/api/pay/callback/**",   // 支付回调：第三方服务器直接请求，不带用户 token
                "/doc.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/webjars/**"
        );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadLocalPath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
