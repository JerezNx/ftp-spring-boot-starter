package xyz.jerez.ftp.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.jerez.ftp.client.FtpTemplate;
import xyz.jerez.ftp.client.ProxyFtpTemplate;
import xyz.jerez.ftp.core.FtpClientFactory;

/**
 * ftp自动配置
 *
 * @author liqilin
 * @since 2020/6/26 17:35
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = FtpProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtpAutoConfiguration {

    /**
     * ftp配置
     *
     * @return ftp配置
     */
    @ConfigurationProperties(prefix = FtpProperties.PREFIX)
    @Bean
    public FtpProperties ftpProperties() {
        return new FtpProperties();
    }

    /**
     * 连接池配置
     *
     * @return 连接池配置
     */
    @ConfigurationProperties(prefix = "ftp.pool")
    @Bean
    public GenericObjectPoolConfig<FTPClient> poolConfig() {
        final GenericObjectPoolConfig<FTPClient> ftpClientPoolConfig = new GenericObjectPoolConfig<>();
        ftpClientPoolConfig.setJmxEnabled(false);
        return ftpClientPoolConfig;
    }

    @Bean
    FtpClientFactory ftpClientFactory() {
        log.debug("Init FtpClientFactory");
        return new FtpClientFactory(ftpProperties());
    }

    @Bean
    ObjectPool<FTPClient> ftpClientPool() {
        return new GenericObjectPool<>(ftpClientFactory(), poolConfig());
    }

    @Bean
    @ConditionalOnMissingBean
    FtpTemplate ftpTemplate() {
        return new ProxyFtpTemplate(ftpClientPool());
    }

}
