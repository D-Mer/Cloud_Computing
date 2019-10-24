package com.yunjisuan.presentation.controller;

import com.yunjisuan.presentation.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
public class DataController {
    @Autowired
    private MongoTemplate mongoTemplate;
    @RequestMapping(value = "/predict", method = RequestMethod.POST)
    public List<String> getPredict(@RequestBody List<Skill> skills) throws Exception{

        final int dimensions=493;
        final int centerNum=20;
        Resource resource1 = new ClassPathResource("centers.txt");
        BufferedReader centerBr = new BufferedReader(new InputStreamReader(resource1.getInputStream()));
        Resource resource2 = new ClassPathResource("points.txt");
        BufferedReader pointsBr = new BufferedReader(new InputStreamReader(resource2.getInputStream()));
        Resource resource3 = new ClassPathResource("allOut70.csv");
        BufferedReader namesBr = new BufferedReader(new InputStreamReader(resource3.getInputStream()));

        List<String> skillList=new ArrayList<>(Arrays.asList("html","vue","web","app","javascript","dom","jquery","bootstrap","pc","div","json","css","d3","bom","w3c","websocket","http","cocos","creator","typescript","ajax","sass","react","webpack","gulp","es","angular2","backbone","android","php","grunt","amd","shell","commonjs","angularjs","prototype","xml","yui","nodejs","git","xhtml","canvas","dhtml","java","dreamweaver","python","koa","redux","ruby","ps","ui","mac","jira","ecmascript","mui","wap","flash","oracle","zepto","bower","adobe","dns","bug","node","mvc","babel","easyui","webapp","layabox","scss","jq","requirejs","b2b","mongodb","sql","script","sketch","fpga","matlab","gui","swift","mvvm","xcode","google","lua","linux","webview","sencha","asp","mysql","c","design","ip","svg","cmd","servlet","amp","seo","b2c","router","svn","vuex","bi","redis","api","saas","erp","framework","yarn","safari","seajs","yii","symfony","cms","wordpress","golang","studio","sdk","cordova","hbuilder","unit","scrum","retrofit","okhttp","jni","ide","socket","tcp","andriod","eclipse","xmpp","os","tob","iot","weex","rn","gradle","ffmpeg","apple","stack","sqlite","gpu","gps","arm","wifi","kotlin","im","as3","rtp","crm","sip","opengl","jvm","mssql","sqlserver","ar","axure","idea","javaweb","ndk","arcgis","soap","cc","i2c","oop","c1","wms","cbd","adb","geek","rtmp","rtsp","db","ai","zigbee","io","j2ee","p2p","ftp","cvs","layout","spring","ood","jd","gdb","icon","painter","banner","acg","ae","dmax","c4d","unity","sai","ipad","coreldraw","illustartor","arpg","ue","gt","brt","excel","mmo","slg","vr","cdr","rpg","cs","pm","apache","centos","saltstack","springboot","cdn","squid","dhcp","vmware","tomcat","xen","puppet","kvm","zabbix","cacti","dubbo","haproxy","nginx","activemq","lvs","lnmp","perl","k8s","openstack","elk","lamp","docker","mq","mapreduce","hdfs","zk","hive","zookeeper","unix","kafka","hadoop","sap","spark","jboss","storm","rabbitmq","ci","bash","swarm","network","cdh","idc","cd","hbase","office","ppt","redmine","awk","weblogic","azure","liunx","twitter","mongdb","ad","base","impala","elasticsearch","ssh","memcache","rocketmq","iis","qa","mongo","vpn","postgresql","oa","mycat","bachelor","ccna","cisco","system","tcpip","h3c","ccnp","nosql","cassandra","bgp","ospf","ota","aws","cache","pv","dba","resin","flume","ssl","cpu","paas","django","flask","jetty","solr","ccie","pr","pl","db2","tornado","soa","rpc","case","wireshark","pmp","emc","visio","ms","bs","kettle","hr","mqtt","springcloud","cad","mangodb","xss","vba","powerpoint","dw","o2o","indesign","llustrator","dm","web2","actionscript","coredraw","mba","edm","xmind","mindmanager","mr","maya","sem","flow","psd","dojo","t1","cet","sns","b1","tb","mmorpg","viso","sense","prd","gis","powerdesigner","vs","webservice","coreanimation","cocoa","rest","ts","interface","block","core","hybird","lbs","oo","gcc","quartz","ioc","mfc","ooa","stl","net","phonegap","qt","opencv","boost","bitcoin","protobuf","grpc","dau","kpi","bbs","kol","ugc","mrd","springmvc","cloud","extjs","webapi","jms","mina","aop","thrift","security","orm","velocity","scala","acm","freemarker","selenium","tdd","j2se","ejb","etl","jmeter","appium","loadrunner","fiddler","robotium","postman","testng","qc","soapui","qtp","bugzilla","cmmi","pcb","dsp","robot","sqoop","winform","ethereum","analysis","mot","a2","a0","ka","mcu","eda","rtos","spi","cortex","cadence","protel","stm32","altium","iic","verilog","autocad","vhdl","proe","aso","roi","gmv","pgc","cpc","caffe","theano","mxnet","tensorflow","pytorch","slam","svm","sas","cuda","spss","gbdt","shader","layui","hyperledger","kylin","sybase","webgl","firebug","codeigniter","fis","thinkphp","smarty","cocos2dx","laravel","webform","wpf","wcf","xpath","scrapy","cpa","ado","zend","cps","ipo","ecshop","yaf","phpcms","swoole","phalcon","dsmax","edius","tableau","hrbp"));
        double[] inputPoint=new double[dimensions];
        int inputPointSum=0;
        for(Skill skill:skills){
            inputPoint[skillList.indexOf(skill.getName())]=skill.getValue();
            inputPointSum+=skill.getValue();
        }
        for(int i=0;i<dimensions;i++){
            inputPoint[i]=inputPoint[i]/inputPointSum*1000;
        }
        //debug
        System.out.print("输入坐标:");
        for(int i=0;i<dimensions;i++){
            System.out.print(inputPoint[i]+" ");
        }
        System.out.println();
        double minDistance=0;
        int centerIndex=0;
        for(int i=0;i<centerNum;i++){
            double distance=0;
            String line=centerBr.readLine();
            line=line.substring(1,line.length()-1);
            String[] strArr=line.split(",");
            double[] doubleArr=new double[dimensions];
            for(int j=0;j<dimensions;j++){
                doubleArr[j]=Double.parseDouble(strArr[j]);
                distance+=(inputPoint[j]-doubleArr[j])*(inputPoint[j]-doubleArr[j]);
            }
            if(i==0){
                minDistance=distance;
                centerIndex=i;
            }else {
                if(distance<minDistance){
                    minDistance=distance;
                    centerIndex=i;
                }
            }
        }

        Map<Double,Integer> distanceMap=new TreeMap<Double, Integer>(new Comparator<Double>() {
            public int compare(Double obj1, Double obj2) {
                // 降序排序
                return obj1.compareTo(obj2);
            }
        });
        String line=null;
        int lineIndex=0;
        while ((line=pointsBr.readLine())!=null){
            if(Integer.parseInt(line.split(":")[0])==centerIndex){
                line=line.split(":")[1];
                line=line.substring(2,line.length()-1);
                double distance=0;
                String[] strArr=line.split(",");
                double[] doubleArr=new double[dimensions];
                for(int j=0;j<dimensions;j++){
                    doubleArr[j]=Double.parseDouble(strArr[j]);
                    distance+=(inputPoint[j]-doubleArr[j])*(inputPoint[j]-doubleArr[j]);
                }
                distanceMap.put(distance,lineIndex);
            }
            lineIndex++;
        }
        List<String> res=new LinkedList<>();
       Map<Integer,String> nameMap=new HashMap<>();
        lineIndex=0;
        while ((line=namesBr.readLine())!=null){
            if(lineIndex==0){
                System.out.println(line);
            }

            nameMap.put(lineIndex,line.split(",")[0]);
            lineIndex++;
        }
        Set<Double> keySet=distanceMap.keySet();
        Iterator<Double> iter=keySet.iterator();
        for(int i=0;i<5;i++){
            Double k=iter.next();
            res.add(nameMap.get(distanceMap.get(k)));
            System.out.print("行数"+distanceMap.get(k)+"距离"+k+"名称"+nameMap.get(distanceMap.get(k)));
        }
        return res;
    }
    @RequestMapping(value = "/eduCount", method = RequestMethod.GET)
    public List<EducationCount> getEduCountData() {
        List<EducationCount> data=mongoTemplate.findAll(EducationCount.class);
        return data;
    }
    @RequestMapping(value = "/jobCount", method = RequestMethod.GET)
    public List<JobCount> getJobCountData() {
        List<JobCount> data=mongoTemplate.findAll(JobCount.class);
        return data;
    }
    @RequestMapping(value = "/jobClassCount", method = RequestMethod.GET)
    public List<JobClassCount> getJobClassCountData() {
        List<JobClassCount> data=mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC,"count")),JobClassCount.class);
        return data;
    }
    @RequestMapping(value = "/cityCount", method = RequestMethod.GET)
    public List<CityCount> getCityCountData() {
        List<CityCount> data=mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC,"count")).limit(10),CityCount.class);
        return data;
    }
    @RequestMapping(value = "/handle", method = RequestMethod.GET)
    public void handle() {
        List<CityCount> data=mongoTemplate.findAll(CityCount.class);
        mongoTemplate.remove(Query.query(Criteria.where("type").all()),CityCount.class);
        for (CityCount cityCount:data){
            String code=cityCount.getType();
            String procode=code.substring(0,2);
            String citycode=code.substring(0,4);
            System.out.println(code);
            CityCode cityCode=mongoTemplate.findOne(Query.query(Criteria.where("code").is(procode)),CityCode.class);

            String pro=cityCode.getPro();
            System.out.println(pro);
            String city="";
            for(Child child:cityCode.getChildren()){
                if(child.getCode().length()!=4){
                    city=pro;
                    break;
                }
                if(child.getCode().equals(citycode)){
                    city=child.getName();
                    break;
                }
            }
            cityCount.setPro(pro);
            cityCount.setCity(city);
            cityCount.setId(null);
        }
        mongoTemplate.insert(data,CityCount.class);
//        Query query=Query.query(Criteria

    }
}
