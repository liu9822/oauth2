
/**
 * @模块名:oauth2-server
 * @包名:cn.cc.study.config
 * @描述:AuthorizationServerConfiguration.java
 * @版本:1.0
 * @创建人:cc
 * @创建时间:2019年11月18日上午11:16:27
 */

package cn.cc.study.config.oauth2;

import java.security.KeyPair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import cn.cc.study.config.error.MssWebResponseExceptionTranslator;
import cn.cc.study.util.DigestUtil;

/**
 * @模块名:oauth2-server
 * @包名:cn.cc.study.config
 * @类名称: AuthorizationServerConfiguration
 * @类描述:【类描述】授权服务配置
 * @版本:1.0
 * @创建人:cc
 * @创建时间:2019年11月18日上午11:16:27
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {
    /**
     * 认证管理器
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 
     * @方法名:tokenStore
     * @方法描述:自定义储存策略
     * @return
     * @修改描述:
     * @版本:1.0
     * @创建人:cc
     * @创建时间:2019年11月19日 下午1:56:02
     * @修改人:cc
     * @修改时间:2019年11月19日 下午1:56:02
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    /**
     * 定义令牌端点上的安全性约 束
     * 
     * (non-Javadoc)
     * 
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer)
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients().tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 用于定义客户端详细信息服务的配置程序。可以初始化客户端详细信息;
     * 
     * (non-Javadoc)
     * 
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer)
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // clients.withClientDetails(clientDetails());
        clients.inMemory().withClient("android").scopes("read").secret(DigestUtil.encrypt("android"))
                .authorizedGrantTypes("password", "authorization_code", "refresh_token").and().withClient("webapp")
                .scopes("read").authorizedGrantTypes("implicit").and().withClient("browser")
                .authorizedGrantTypes("refresh_token", "password").scopes("read");
    }

    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return new MssWebResponseExceptionTranslator();
    }

    /**
     * 定义授权和令牌端点以及令牌服务
     * 
     * (non-Javadoc)
     * 
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer)
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore()).tokenEnhancer(jwtTokenEnhancer())
                .authenticationManager(authenticationManager).exceptionTranslator(webResponseExceptionTranslator());
    }

    /**
     * <p>
     * 注意，自定义TokenServices的时候，需要设置@Primary，否则报错，
     * </p>
     * 
     * @return
     */
    @Primary
    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenEnhancer(jwtTokenEnhancer());
        // tokenServices.setClientDetailsService(clientDetails());
        // token有效期自定义设置，默认12小时
        tokenServices.setAccessTokenValiditySeconds(60 * 60 * 24 * 7);
        // tokenServices.setAccessTokenValiditySeconds(60 * 60 * 12);
        // refresh_token默认30天
        tokenServices.setAccessTokenValiditySeconds(60 * 60 * 24 * 7);
        // tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24 * 7);
        return tokenServices;
    }

    /**
     * 定义jwt的生成方式
     *
     * @return JwtAccessTokenConverter
     */
    @Bean
    public JwtAccessTokenConverter jwtTokenEnhancer() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 非对称加密，但jwt长度过长
        KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("spring-jwt.jks"), "admin123456".toCharArray())
                .getKeyPair("spring-jwt");
        converter.setKeyPair(keyPair);
        // 对称加密
        // converter.setSigningKey("admin123");
        return converter;
    }
}
