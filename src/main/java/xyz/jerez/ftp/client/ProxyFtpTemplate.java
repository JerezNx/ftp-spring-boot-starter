package xyz.jerez.ftp.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * @author liqilin
 * @since 2020/6/26 19:33
 */
@Slf4j
public class ProxyFtpTemplate implements FtpTemplate {

    /**
     * ftp连接池
     */
    private ObjectPool<FTPClient> ftpPool;

    /**
     * ftp操作代理类，用于执行ftp操作前，从连接池取ftpClient，执行完成后，返回给连接池
     */
    private FtpTemplate ftpTemplateProxy;

    public ProxyFtpTemplate() {
        this.ftpTemplateProxy = (FtpTemplate) newProxyInstance(FtpTemplate.class.getClassLoader(),
                new Class[]{FtpTemplate.class}, new FtpClientInterceptor(new DefaultFtpTemplate()));
    }

    public ProxyFtpTemplate(ObjectPool<FTPClient> ftpPool) {
        this();
        this.ftpPool = ftpPool;
    }

    @Override
    public boolean download(String remoteDir, String remoteFileName, OutputStream outputStream) {
        return ftpTemplateProxy.download(remoteDir, remoteFileName, outputStream);
    }

    @Override
    public boolean uploadFile(InputStream inputStream, String remoteDir, String originName) {
        return ftpTemplateProxy.uploadFile(inputStream, remoteDir, originName);
    }

    @Override
    public boolean uploadFile(InputStream inputStream, String remoteFullPath) {
        return ftpTemplateProxy.uploadFile(inputStream, remoteFullPath);
    }

    @Override
    public List<String> listDir(String directory) {
        return ftpTemplateProxy.listDir(directory);
    }

    @Override
    public List<String> listFile(String directory) {
        return ftpTemplateProxy.listFile(directory);
    }

    @Override
    public List<String> listFile(String directory, boolean includeBaseDir) {
        return ftpTemplateProxy.listFile(directory, includeBaseDir);
    }

    @Override
    public List<String> listFile(String directory, boolean includeBaseDir, boolean recursion) {
        return ftpTemplateProxy.listFile(directory, includeBaseDir, recursion);
    }

    @Override
    public boolean isExists(String fileName) {
        return ftpTemplateProxy.isExists(fileName);
    }

    @Override
    public boolean isExists(String directory, String fileName) {
        return ftpTemplateProxy.isExists(directory, fileName);
    }

    @Override
    public boolean mkDir(String directory) {
        return ftpTemplateProxy.mkDir(directory);
    }

    @Override
    public boolean changeWorkDir(String directory) {
        return ftpTemplateProxy.changeWorkDir(directory);
    }

    @Override
    public boolean deleteFile(String fullFileName) {
        return ftpTemplateProxy.deleteFile(fullFileName);
    }

    @Override
    public boolean deleteDir(String directory) {
        return ftpTemplateProxy.deleteDir(directory);
    }

    /**
     * 获取ftpClient的动态代理拦截
     */
    class FtpClientInterceptor implements InvocationHandler {

        private Object target;

        FtpClientInterceptor(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            FTPClient ftpClient;
            try {
//                1. 从ftp连接池取ftpClient
                ftpClient = ftpPool.borrowObject();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Borrow ftp client failed, exception is ", e);
                throw e;
            }
            try {
                DefaultFtpTemplate.client.set(ftpClient);
//                2. 执行ftp操作
                return method.invoke(target, args);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Invoke ftp method failed, exception is ", e);
                throw e;
            } finally {
                DefaultFtpTemplate.client.remove();
                try {
//                    3. 返还ftpClient
                    ftpPool.returnObject(ftpClient);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Return ftp client failed, exception is ", e);
                }
            }
        }
    }

    public ObjectPool<FTPClient> getFtpPool() {
        return ftpPool;
    }

    public void setFtpPool(ObjectPool<FTPClient> ftpPool) {
        this.ftpPool = ftpPool;
    }
}
