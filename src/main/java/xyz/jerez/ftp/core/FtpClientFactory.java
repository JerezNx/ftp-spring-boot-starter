package xyz.jerez.ftp.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import xyz.jerez.ftp.autoconfigure.FtpProperties;

import java.io.IOException;

/**
 * ftp客户端工厂
 *
 * @author liqilin
 * @since 2020/6/26 16:43
 */
@Slf4j
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

    private FtpProperties ftpProperties;

    public FtpClientFactory(FtpProperties ftpProperties) {
        this.ftpProperties = ftpProperties;
    }

    @Override
    public FTPClient create() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(ftpProperties.getConnectTimeOut());
        ftpClient.setControlEncoding(ftpProperties.getEncoding());
        try {
            log.info("Connecting to ftp server, {}:{}", ftpProperties.getHost(), ftpProperties.getPort());
            ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.error("Failed to connect to ftp server[{}:{}]", ftpProperties.getHost(), ftpProperties.getPort());
                return null;
            }
            boolean result = ftpClient.login(ftpProperties.getUsername(), ftpProperties.getPassword());
            if (!result) {
                log.error("Failed to login, username:{},password:{}", ftpProperties.getUsername(), ftpProperties.getPassword());
                return null;
            }
            ftpClient.setBufferSize(ftpProperties.getBufferSize());
            ftpClient.setFileType(ftpProperties.getFileType());
            if (ftpProperties.isPassiveMode()) {
                log.debug("Ftp enter local passive mode");
                ftpClient.enterLocalPassiveMode();
            }
        } catch (IOException e) {
            log.error("Failed to connect to ftp server[{}:{}], exception is ", ftpProperties.getHost(), ftpProperties.getPort(), e);
        }
        return ftpClient;
    }

    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        return new DefaultPooledObject<>(ftpClient);
    }

    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        FTPClient ftpClient;
        if (p == null || (ftpClient = p.getObject()) == null) {
            log.warn("Close a null ftp client");
            return;
        }
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException e) {
            log.error("Failed to logout ftp, exception is", e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("Failed to close ftp, exception is", e);
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        FTPClient ftpClient;
        if (p == null || (ftpClient = p.getObject()) == null) {
            log.error("The ftp client is null");
            return false;
        }
        try {
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            log.error("Failed to validate the ftp client, exception is", e);
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<FTPClient> p) throws Exception {
        FTPClient ftpClient;
        if (p == null || (ftpClient = p.getObject()) == null) {
            log.warn("The ftp client is null");
            return;
        }
        ftpClient.changeWorkingDirectory("/");
    }
}
