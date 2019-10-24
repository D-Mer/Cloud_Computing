import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author 233loser
 * @version 1.0
 * @date 2019/10/11 16:27
 **/
public class FileCreation {
    private ConnectHadoop hdfs;

    public static void main(String[] args) {
        String srcDir = "E:\\tmp\\src\\";
        String distDir = "/streaming/";

        Scanner s = new Scanner(System.in);
        System.out.println("请输入模拟流文件夹目录，路径使用\\\\(默认值为 E:\\\\tmp\\\\src)");
        String inputDir = s.nextLine();
        if (inputDir != null && "".equals(inputDir.strip())) {
            if (new File(inputDir).exists() && new File(inputDir).isDirectory()) {
                srcDir = inputDir;
            }
        }
        System.out.println("请输入目标hdfs目录，路径使用/为分隔符的绝对路径(默认值为 /streaming/)");
        inputDir = s.nextLine();
        if (inputDir != null && "".equals(inputDir.strip()) && inputDir.startsWith("/")) {
            if (!inputDir.endsWith("/")) {
                distDir = inputDir + "/";
            } else {
                distDir = inputDir;
            }
        }
        s.close();
        FileCreation creation = new FileCreation();
        creation.hdfs = new ConnectHadoop(distDir);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        creation.generateCSV(new File(srcDir));
    }

    private void generateCSV(File sourceDir) {
        for (File f : Objects.requireNonNull(sourceDir.listFiles((File pathname) -> pathname.getName().endsWith(".csv")))) {
            try {
                //读取文件
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
                String line;
                int lineNum = 0;
                StringBuilder fileString = new StringBuilder(10000);

                while ((line = reader.readLine()) != null) {
                    if (lineNum < 10000) {
                        fileString.append(line).append("\r\n");
                        lineNum++;
                    } else {
                        writeCSV(fileString, Calendar.getInstance().getTimeInMillis() + ".csv");
                        fileString = new StringBuilder(10000);
                        lineNum = 0;
                    }
                }
                if (lineNum != 0) {
                    writeCSV(fileString, Calendar.getInstance().getTimeInMillis() + ".csv");
                }
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            break;
        }
    }

    private void writeCSV(StringBuilder csvString, String fileName) {
        try {
            hdfs.createFile(fileName, csvString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
