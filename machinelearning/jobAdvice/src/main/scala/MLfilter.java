import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description TODO
 * @Author 233loser
 * @Date 2019/10/19 20:33
 * @Version 1.0
 **/
public class MLfilter {
    String fromPath ="E:\\娱乐\\社交\\聊天记录\\2433495058\\FileRecv\\allOut70f.csv";
    String toPath ="D:\\学习资料\\大三①\\云计算\\MLlib\\数据\\allOut70fRes.csv";
    int standard = 100;

    public List<String> readCsv(){
        List<String> res = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fromPath)));
            String input = "";
            while((input=reader.readLine())!=null){
                res.add(input);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    public void writeCSV(List<String> csvRes,String path){
        try {
            byte[] uft8bom={(byte)0xef,(byte)0xbb,(byte)0xbf};
            FileOutputStream out = new FileOutputStream(new File(path));
            out.write(uft8bom);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            for(int i = 0; i<csvRes.size();i++) {
//                bw.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF }));
                bw.write(csvRes.get(i));
                //新增一行数据
                if(i!=csvRes.size()-1) {
                    bw.newLine();
                }
            }
            bw.close();
        } catch (FileNotFoundException e) {
            //捕获File对象生成时的异常
            e.printStackTrace();
        } catch (IOException e) {
            //捕获BufferedWriter对象关闭时的异常
            e.printStackTrace();
        }
    }

//    public static void main(String[] agrs){
//        MLfilter mLfilter = new MLfilter();
//        List<String> read = mLfilter.readCsv();
//        List<String> filteredList = new ArrayList<>();
//        filteredList.add(read.get(0));
//        for(int i =1;i<read.size();i++) {
//            String[] num = read.get(i).split(",");
//            int sum = 0;
//            for (int j = 1; j < num.length; j++) {
//                sum += Integer.parseInt(num[j]);
//            }
//            if (sum > mLfilter.standard) {
//                filteredList.add(read.get(i));
//            }
//        }
//        mLfilter.writeCSV(filteredList,mLfilter.toPath);
//    }
    public static void main(String[] args){
        MLfilter mLfilter = new MLfilter();
        List<String> readLine = mLfilter.readCsv();
        List<String> writeLine = new ArrayList<>();
        for(String i : readLine){
            String[] points = i.split(",");
            Integer[] intPoints = new Integer[points.length];
            int sum = 0;
            for(int j =0;j<points.length;j++){
                try {
                    String temp = points[j];
                    String regEx="[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(temp);
                    Integer x = Integer.parseInt(m.replaceAll("").trim());
                    intPoints[j] = x;
                    sum += x;
                }catch (Exception e){
                    System.out.println(i);
                    System.out.println(j);
                }
            }
            String res=String.valueOf(((float)intPoints[0]/sum)*1000);//format 返回的是字符串
            StringBuffer buffer = new StringBuffer(res);
            for(int j =1;j<points.length;j++){
                buffer.append(",");
                String temp =String.valueOf(((float)intPoints[j]/sum)*1000);//format 返回的是字符串
                buffer.append(temp);
            }
            writeLine.add(buffer.toString());

        }
        mLfilter.writeCSV(writeLine,mLfilter.toPath);
    }
}
