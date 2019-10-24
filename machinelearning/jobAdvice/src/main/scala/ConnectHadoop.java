import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.PrivilegedExceptionAction;

/**
 * @author DW
 * @date 2019/10/11
 */
public class ConnectHadoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectHadoop.class);
    private static Configuration conf = new Configuration();
    private static FileSystem hdfs;
    private static String dir;

    ConnectHadoop(String hadoopDir) {
        if (!hadoopDir.endsWith("/")) {
            hadoopDir += "/";
        }

        UserGroupInformation ugi = UserGroupInformation.createRemoteUser("hadoop");
        try {
            ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", "hdfs://172.19.240.210:9000/");
                conf.set("dfs.replication", "2");
//                conf.set("dfs.block.size", "14680064");
                return null;
            });
            Path path = new Path("hdfs://172.19.240.210:9000/");
            hdfs = FileSystem.get(path.toUri(), conf);
            dir = hadoopDir;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除并重建 dir 指向的目录
     */
    public void initDir() {
        try {
            this.deleteFileDir(dir);
            this.createDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据构造参数创建hdfs目录
     */
    public void createDir() throws IOException {
        Path path = new Path(dir);
        if (hdfs.exists(path)) {
            LOGGER.error("创建文件夹:文件夹 \t" + conf.get("fs.defaultFS") + dir + "\t 已经存在");
        } else {
            hdfs.mkdirs(path);
            LOGGER.info("新建文件夹: \t" + conf.get("fs.defaultFS") + dir);
        }
    }

    /**
     * 从本地文件拷贝到hdfs
     *
     * @param localSrcFlie 本地文件，不是文件夹
     */
    public void copyFile(String localSrcFlie) throws IOException {
        Path src = new Path(localSrcFlie);
        Path dst = new Path(dir);
        if (!(new File(localSrcFlie)).exists()) {
            LOGGER.error("拷贝文件：本地文件：\t" + localSrcFlie
                    + "\t 不存在");
        } else if (!hdfs.exists(dst)) {
            LOGGER.error("拷贝文件：远程文件 \t" + dst.toUri()
                    + "\t 不存在");
        } else {
            String dstPath = dst.toUri() + "/" + src.getName();
            if (hdfs.exists(new Path(dstPath))) {
                LOGGER.warn("远程文件 \t" + dstPath
                        + "\t 已经存在，覆盖写入！");
            }
            hdfs.copyFromLocalFile(src, dst);
            LOGGER.info("复制文件到： \t" + conf.get("fs.defaultFS")
                    + dir);
        }
    }

    /**
     * 新建并写入到文件，如果存在就覆盖
     *
     * @param fileName    文件名
     * @param fileContent 文件内容
     */
    public void createFile(String fileName, String fileContent) throws IOException {
        Path dst = new Path(dir + fileName);
        FSDataOutputStream output = hdfs.create(dst);
        output.write(fileContent.getBytes());
        output.close();
        LOGGER.info("创建了新文件： \t" + conf.get("fs.defaultFS")
                + fileName);
    }

    /**
     * 追加到文件
     *
     * @param fileName    文件名
     * @param fileContent 文件内容
     */
    public void appendFile(String fileName, String fileContent) throws IOException {
        Path dst = new Path(dir + fileName);
        byte[] bytes = fileContent.getBytes();
        if (!hdfs.exists(dst)) {
            LOGGER.error("追加到文件:目标文件不存在");
            createFile(fileName, fileContent);
        } else {
            FSDataOutputStream output = hdfs.append(dst);
            output.write(bytes);
            output.close();
            LOGGER.info("追加内容到文件 \t" + conf.get("fs.defaultFS")
                    + fileName);
        }
    }

    /**
     * 列出hdfs指定目录文件
     *
     * @param dirName 文件目录
     */
    public void listFiles(String dirName) throws IOException {
        Path f = new Path(dirName);
        FileStatus[] status = hdfs.listStatus(f);
        System.out.println(dirName + " has all files:");
        for (FileStatus fileStatus : status) {
            System.out.println(fileStatus.getPath().toString());
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) throws IOException {
        Path f = new Path(dir + fileName);
        boolean isExists = hdfs.exists(f);
        if (isExists) {
            boolean isDel = hdfs.delete(f, true);
            LOGGER.info(fileName + "\t 删除" + (isDel ? "成功" : "失败"));
        } else {
            LOGGER.info("删除文件：\t " + fileName + " \t 不存在");
        }
    }

    /**
     * 删除文件夹
     *
     * @param fileDir 文件夹绝对路径
     */
    public void deleteFileDir(String fileDir) throws IOException {
        Path f = new Path(fileDir);
        boolean isExists = hdfs.exists(f);
        if (isExists) {
            boolean isDel = hdfs.delete(f, true);
            LOGGER.info(fileDir + "\t 文件夹删除" + (isDel ? "成功" : "失败"));
        } else {
            LOGGER.error("删除文件夹：\t " + fileDir + " \t 文件夹不存在");
        }
    }

    /**
     * 从hdfs中下载文件
     *
     * @param fileName   下载文件名
     * @param outputPath 本地输出路径
     */
    public void downloadFile(String fileName, String outputPath) throws IOException {
        Path f = new Path(dir + fileName);
        if (hdfs.exists(f)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(hdfs.open(f).getWrappedStream()));
            FileWriter fileWriter = new FileWriter(outputPath);
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                fileWriter.write(readLine);
                fileWriter.write("\n");
            }
            fileWriter.flush();
            fileWriter.close();
            reader.close();
            LOGGER.info("下载文件：\t " + fileName + " \t 到本地成功");
        } else {
            LOGGER.error("下载文件：\t " + fileName + " \t 文件不存在");
        }
    }

    public static void main(String[] args) throws IOException {
//        ConnectHadoop con = new ConnectHadoop("/graphX/");
//        con.downloadFile("Vertexes.csv", "D://Vertexes.csv");
    }
}