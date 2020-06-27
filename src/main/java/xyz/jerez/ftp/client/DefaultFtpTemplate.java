package xyz.jerez.ftp.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认实现
 *
 * @author liqilin
 * @since 2020/6/26 19:21
 */
@Slf4j
public class DefaultFtpTemplate implements FtpTemplate {

//    private ObjectPool<FTPClient> ftpPool;

    /**
     * 当前线程所使用的ftpClient
     * @see ProxyFtpTemplate.FtpClientInterceptor 动态代理设置，移除
     */
    static ThreadLocal<FTPClient> client = new ThreadLocal<>();

    @Override
    public boolean download(String remoteDir, String remoteFileName, OutputStream outputStream) {
        FTPClient ftpClient = client.get();
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(remoteDir);
            ftpClient.retrieveFile(remoteFileName, outputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP下载文件失败,文件路径：{}/{},异常{}", remoteDir, remoteFileName, e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Borrow ftp client failed, exception is ", e);
        }
        return false;
    }

    @Override
    public boolean uploadFile(InputStream inputStream, String remoteDir, String originName) {
        FTPClient ftpClient = client.get();
        if (ftpClient == null) {
            log.error("获取FtpClient失败");
            return false;
        }

        try {
            mkDir(remoteDir);
            changeWorkDir(remoteDir);
            ftpClient.storeFile(originName, inputStream);//保存文件
//            ftpClient.logout();
            return true;
        } catch (IOException e) {
            log.error("FTP上传文件失败,文件路径：{}/{},异常{}", remoteDir, originName, e.toString());
            e.printStackTrace();
            return false;
        }
//        finally {
//            if (ftpClient.isConnected()) {
//                try {
//                    ftpClient.disconnect();
//                } catch (IOException e) {
//                    log.error("断开连接失败：{}", e);
//                }
//            }
//        }
    }

    @Override
    public boolean uploadFile(InputStream inputStream, String remoteFullPath) {
        String path = remoteFullPath.substring(0, remoteFullPath.lastIndexOf("/"));
        String fileName = remoteFullPath.substring(remoteFullPath.lastIndexOf("/") + 1);
        return uploadFile(inputStream, path, fileName);
    }

    @Override
    public List<String> listFile(String directory) {
        return listFile(directory, true);
    }

    @Override
    public List<String> listFile(String directory, boolean includeBaseDir) {
        return listFile(directory, true, true);
    }

    @Override
    public List<String> listFile(String directory, boolean includeBaseDir, boolean recursion) {
        FTPClient ftpClient = client.get();
        List<String> list = new ArrayList<>();
        try {
            FTPFile[] files = ftpClient.listFiles(directory);
            for (FTPFile file : files) {
                String t = includeBaseDir ? (directory + "/" + file.getName()).replaceAll("//", "/") : file.getName();
                if (file.isFile()) {
                    list.add(t);
                } else if (file.isDirectory() && recursion) {
                    list.addAll(listFile((t + "/").replaceAll("//", "/"), includeBaseDir, recursion));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP查看文件失败，路径：{},异常：{}", directory, e.toString());
        }
        return list;
    }

    @Override
    public boolean isExists(String fileName) {
        List<String> list = listFile("/");
        return list.contains(fileName);
    }

    @Override
    public boolean isExists(String directory, String fileName) {
        List<String> list = listFile(directory, false, false);
        return list.contains(fileName);
    }

    @Override
    public boolean mkDir(String directory) {
        FTPClient ftpClient = client.get();
        directory = directory.replaceAll("//", "/");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        try {
            String[] str = directory.split("/");
            StringBuilder t = new StringBuilder();
            StringBuilder parnet = new StringBuilder();
            ftpClient.changeWorkingDirectory("/");
            for (String s : str) {
                t.append("/").append(s);
                if (!isExists(t.substring(1))) {
                    ftpClient.makeDirectory(s);
                }
                ftpClient.changeWorkingDirectory(s);
                parnet.append("../");
            }
            if (str.length >= 1) {
                ftpClient.changeWorkingDirectory(parnet.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP创建文件夹失败，路径:{}，异常：{}", directory, e.toString());
        }
        return false;
    }

    @Override
    public boolean changeWorkDir(String directory) {
        FTPClient ftpClient = client.get();
        try {
            ftpClient.cwd(directory);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP切换路径失败，路径:{}，异常：{}", directory, e.toString());
        }
        return false;
    }

    private String getParentPath(String file) {
        if (file.contains("/")) {
            String temp;
            Pattern p = Pattern.compile("[/]+");
            Matcher m = p.matcher(file);
            int i = 0;
            while (m.find()) {
                temp = m.group(0);
                i += temp.length();
            }
            StringBuilder parent = new StringBuilder();
            for (int j = 0; j < i; j++) {
                parent.append("../");
            }
            return parent.toString();
        } else {
            return "./";
        }
    }

}
