package xyz.jerez.ftp.autoconfigure;

import lombok.Data;

/**
 * ftp连接池属性配置
 *
 * @author liqilin
 * @since 2020/6/26 16:32
 */
@Data
public class FtpProperties {

    public static final String PREFIX = "ftp";

    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * ftp地址
     */
    private String host;
    /**
     * 端口号
     */
    private int port = 21;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 连接超时时间 毫秒
     */
    private int connectTimeOut = 5000;

    /**
     * 编码
     */
    private String encoding = "UTF-8";

    /**
     * 缓冲区大小
     */
    private int bufferSize = 1024;

    /**
     * 传输数据格式   2表binary
     * 设置文件传输模式为二进制，可以保证传输的内容不会被改变
     */
    private int fileType = 2;

    /**
     * 是否启用被动模式
     */
    private boolean passiveMode = true;

}
