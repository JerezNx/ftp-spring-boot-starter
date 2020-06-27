package xyz.jerez.ftp.core;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.NoSuchElementException;

/**
 * ftp客户端连接池
 * 拓展用，暂时无用
 *
 * @author liqilin
 * @since 2020/6/26 17:08
 */
@Deprecated
public class FtpClientPool<FTPClient> implements ObjectPool<FTPClient> {

    private GenericObjectPool<FTPClient> pool;

    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        pool.addObject();
    }

    @Override
    public FTPClient borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        return pool.borrowObject();
    }

    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        pool.clear();
    }

    @Override
    public void close() {
        pool.close();
    }

    @Override
    public int getNumActive() {
        return pool.getNumActive();
    }

    @Override
    public int getNumIdle() {
        return pool.getNumIdle();
    }

    @Override
    public void invalidateObject(FTPClient ftpClient) throws Exception {
        pool.invalidateObject(ftpClient);
    }

    @Override
    public void returnObject(FTPClient ftpClient) throws Exception {
        pool.returnObject(ftpClient);
    }
}
