package net.linlan.stage.webconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * spring security配置
 * 
 * @author Linlan
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * 密码编码验证器
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭跨域保护
//        http
//                .csrf().disable();
        // 禁用http基础认证
        http
                .httpBasic().disable();
        // 权限配置
        http
                .authorizeRequests()
                // 放行OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                // 登录页放行
                .antMatchers("/api/ocr/**", "/api/stage/**").permitAll()
                .antMatchers("/**").authenticated()
                .and().headers().frameOptions().sameOrigin();

    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        // 放行静态资源
        web.
                ignoring().
                antMatchers("/images/**").
                antMatchers("/webjars/**");
    }
}
