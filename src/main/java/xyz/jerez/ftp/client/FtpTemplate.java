package xyz.jerez.ftp.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * ftp操作类
 *
 * @author liqilin
 * @since 2020/6/26 19:20
 */
public interface FtpTemplate {

    /**
     * 下载ftp文件
     *
     * @param remoteDir      文件所在路径
     * @param remoteFileName 文件名
     * @param outputStream   输出流
     * @return 是否成功
     */
    boolean download(String remoteDir, String remoteFileName, OutputStream outputStream);

    /**
     * 上传ftp文件
     *
     * @param inputStream 输入流
     * @param remoteDir   文件所在路径
     * @param originName  文件名
     * @return 是否成功
     */
    boolean uploadFile(InputStream inputStream, String remoteDir, String originName);

    /**
     * 上传ftp文件
     *
     * @param inputStream    输入流
     * @param remoteFullPath 文件完整路径和名称
     * @return 是否成功
     */
    boolean uploadFile(InputStream inputStream, String remoteFullPath);

    /**
     * 罗列指定目录下的文件
     *
     * @param directory 指定目录
     * @return 文件名列表（包含所在路径）
     */
    List<String> listFile(String directory);

    /**
     * 罗列指定目录下的文件
     *
     * @param directory      指定目录
     * @param includeBaseDir 返回的文件名是否包含所在路径
     * @return 文件名列表
     */
    List<String> listFile(String directory, boolean includeBaseDir);

    /**
     * 罗列指定目录下的文件
     *
     * @param directory 指定目录
     * @param includeBaseDir 返回的文件名是否包含所在路径
     * @param recursion 是否递归
     * @return 文件名列表
     */
    List<String> listFile(String directory, boolean includeBaseDir, boolean recursion);

    /**
     * 判断指定目录（包含子目录）下是否存在某文件
     *
     * @param fileName  完整文件名
     * @return 是否存在
     */
    boolean isExists(String fileName);

    /**
     * 判断指定目录（不包括子目录）下是否存在某文件
     *
     * @param directory  指定目录
     * @param fileName  文件名
     * @return 是否存在
     */
    boolean isExists(String directory,String fileName);

    /**
     * 创建目录
     * @param directory 目录
     * @return 是否成功
     */
    boolean mkDir(String directory);

    /**
     * 切换当前工作目录
     * @param directory 目录
     * @return 是否成功
     */
    boolean changeWorkDir(String directory);

}
