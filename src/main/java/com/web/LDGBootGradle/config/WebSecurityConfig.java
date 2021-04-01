package com.web.LDGBootGradle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()   //form을 submit할때 보내는 csrf토큰을 안보내도록 설정
            .authorizeRequests()
                .antMatchers("/urban","/account/register","/css/**","/api/**","/js/**",
                        "/fonts/**","/images/**","/account/**","/download/**").permitAll()  //인증없이 접근가능한 경로 설정(1)
                .antMatchers("/admin/**").access("ROLE_ADMIN") //특정 권한을 가진 이용자만 접근 가능하도록 설정
                .antMatchers("/urban", "/login", "/login-error").permitAll()    //인증없이 접근가능한 경로 설정(2)
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/account/login") // 기본 로그인 경로 및 페이지 설정
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    /*
    Authentication = 로그인
    Authorization = 권한
    반드시 select username, password, enabled " 순서 지킬 것
    쿼리 뒤에는 띄워쓰기 넣기
    ?는 알아서 들어감

    @OneToOne : 일대일 맵핑 // user - user_detail
    @OneToMany : 일대다 맵핑 // user - board
    @ManyToOne : 다대일 맵핑 // board - user
    @ManyToMany : 다대다 맵핑 // user - role
     */

    /*
    authoritiesByUsernameQuery :: 권한을 불러오는 쿼리를 작성하는데, 출력 결과의 컬럼은 2개여야 하며 [사용자이름], [권한명]

    UsersByUsernameQuery(sql):: 사용자 목록을 가져오는 쿼리를 지정하는데, 테이블명이나 컬럼명 등은 자유이나
    출력 결과의 컬럼은 3개여야 하며 [사용자이름], [비밀번호], [Enabled]를 순서대로 지정
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select username, password, enabled "
                        + "from user "
                        + "where username = ?")
                .authoritiesByUsernameQuery("select u.username, r.name "
                        + "from user_role ur inner join user u on ur.user_id = u.id "
                        + "inner join role r on ur.role_id = r.id "
                        + "where u.username = ?");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
