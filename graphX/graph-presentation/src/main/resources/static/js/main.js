
function formatData(orign){
    var data=[];

    for(var i=0;i<orign.length;i++){
        var temp={};
        temp.name=orign[i]['type'];
        temp.value=orign[i]['count'];

        data.push(temp);
    }
    return data;
}
function initWordCloud() {
    function getData() {
        var data=[];
        getRequestSync("/jobCount",function (res) {
            data=formatData(res);
        },function () {

        });
        return data;
    }
    var myWordCloud = echarts.init(document.getElementById("wordCloud"));

    var option = {
        backgroundColor: '#fff',
        tooltip: {
            show: false,

        },
        title: {
            text: "职位词云",
            left:"center",
        },
        series: [{
            type: 'wordCloud',
            gridSize: 1,
            sizeRange: [12, 55],
            rotationRange: [-45, 0, 45, 90],
            // maskImage: maskImage,
            textStyle: {
                normal: {
                    color: function () {
                        return 'rgb(' +
                            Math.round(Math.random() * 255) +
                            ', ' + Math.round(Math.random() * 255) +
                            ', ' + Math.round(Math.random() * 255) + ')'
                    }
                }
            },
            left: 'center',
            top: 'center',
            // width: '96%',
            // height: '100%',
            right: null,
            bottom: null,
            // width: 300,
            // height: 200,
            // top: 20,
            data: getData()
        }]

    }

    myWordCloud.setOption(
        option
    );
    setInterval(function () {
        option.series[0].data = getData();
        myWordCloud.setOption(option);
    }, 2500);
}
function initEdu(){
    function getData() {
        var data=[];
        getRequestSync("/eduCount",function (res) {
            data=formatData(res);
        },function () {

        });
        return data;
    }
    var myEdu=echarts.init(document.getElementById("edu"));
    var option={
        backgroundColor: '#fff',
        tooltip: {
            show: true,
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        title: {
            text: "招聘学历要求",
            left:"center"
        },
        series: [{
            type: 'pie',
            name:"招聘学历要求",

            label:{
                formatter:"{b}:{d}%"
            },
            radius:['50%','70%'],
            center: ['50%', '40%'],
            data:getData(),
            itemStyle: {
                emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            },
            // top:"30%"
        }]

    }
    myEdu.setOption(option);
    setInterval(function () {
        option.series[0].data = getData();
        myEdu.setOption(option);
    },1500)
}
function initJobClassWordCloud() {
    function getData() {
        var data=[];
        getRequestSync("/jobClassCount",function (res) {
            data=formatData(res);
        },function () {

        });
        return data;
    }
    var myWordCloud = echarts.init(document.getElementById("job-class-wordcloud"));

    var option = {
        backgroundColor: '#fff',
        tooltip: {
            show: false,

        },
        title: {
            text: "职业种类词云",
            left:"center",
        },
        series: [{
            type: 'wordCloud',
            gridSize: 1,
            sizeRange: [12, 55],
            rotationRange: [-45, 0, 45, 90],
            // maskImage: maskImage,
            textStyle: {
                normal: {
                    color: function () {
                        return 'rgb(' +
                            Math.round(Math.random() * 255) +
                            ', ' + Math.round(Math.random() * 255) +
                            ', ' + Math.round(Math.random() * 255) + ')'
                    }
                }
            },
            left: 'center',
            top: 'center',
            // width: '96%',
            // height: '100%',
            right: null,
            bottom: null,
            // width: 300,
            // height: 200,
            // top: 20,
            data: getData()
        }]

    }

    myWordCloud.setOption(
        option
    );
    setInterval(function () {
        option.series[0].data = getData();
        myWordCloud.setOption(option);
    }, 2500);
}
function initCityBar(){
    function formatCityBarData(input) {
        var data={
            name:[],
            value:[

                {
                    name:"0-3k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },{
                    name:"3k-5k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"5k-8k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"8k-10k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"10k-15k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"15k-20k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"20k-30k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"30k以上",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
            ]
        };

        for(var i=0;i<input.length;i++){
            data.name.push(input[i]['city']);
            for(var j=1;j<=8;j++){
                data.value[j-1].data.push(input[i]['salary'+j+'_count'])
            }
        }

        return data;
    }
    function getData() {
        var data=[];
        getRequestSync("/cityCount",function (res) {
            data=formatCityBarData(res);
        },function () {

        });
        return data;
    }
    var myWordCloud = echarts.init(document.getElementById("cityBar"));

    var option = {
        backgroundColor: '#fff',
        tooltip: {
            show: true,
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        legend: {
            top:'5%',
            data:['0-3k','3k-5k','5k-8k','8k-10k','10k-15k','15k-20k','20k-30k','30k以上']
        },
        title: {
            text: "城市职位数量与薪资分布\n",
            left:"center",
        },
        yAxis: {
            type: 'category',
            data: [],
            axisLabel:{
                interval: 0
            }
        },
        xAxis: {
            type: 'value'
        },
        series: []
        //     [{
        //     type: 'bar',
        //
        //     // maskImage: maskImage,
        //     textStyle: {
        //
        //     },
        //     left: 'center',
        //     top: 'center',
        //     // width: '96%',
        //     // height: '100%',
        //     right: null,
        //     bottom: null,
        //     // width: 300,
        //     // height: 200,
        //     // top: 20,
        //     data: []
        // }]

    };
    var data=getData();
    option.xAxis.data=data.name;
    option.series=data.value;
    myWordCloud.setOption(
        option
    );
    console.log(option);
    setInterval(function () {
        var data=getData();
        option.yAxis.data=data.name;
        option.series=data.value;
        myWordCloud.setOption(
            option
        );

    }, 1200);
}
function initJobClassBar(){
    function formatCityBarData(input) {
        var data={
            name:[],
            value:[

                {
                    name:"0-3k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },{
                    name:"3k-5k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"5k-8k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"8k-10k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"10k-15k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"15k-20k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"20k-30k",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
                {
                    name:"30k以上",
                    type:'bar',
                    stack:"薪资占比",
                    data:[]
                },
            ]
        };

        for(var i=0;i<20;i++){
            data.name.push(input[i]['type']);
            for(var j=1;j<=8;j++){
                data.value[j-1].data.push(input[i]['salary'+j+'_count'])
            }
        }

        return data;
    }
    function getData() {
        var data=[];
        getRequestSync("/jobClassCount",function (res) {
            data=formatCityBarData(res);
        },function () {

        });
        return data;
    }
    var myWordCloud = echarts.init(document.getElementById("jobClassBar"));

    var option = {
        backgroundColor: '#fff',
        tooltip: {
            show: true,
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        legend: {
            top:'5%',
            data:['0-3k','3k-5k','5k-8k','8k-10k','10k-15k','15k-20k','20k-30k','30k以上']
        },
        title: {
            text: "职位种类的数量与薪资分布\n",
            left:"center",
        },
        xAxis: {
            type: 'category',
            data: [],
            axisLabel:{
                interval: 0
            }
        },
        yAxis: {
            type: 'value'
        },
        series: []
        //     [{
        //     type: 'bar',
        //
        //     // maskImage: maskImage,
        //     textStyle: {
        //
        //     },
        //     left: 'center',
        //     top: 'center',
        //     // width: '96%',
        //     // height: '100%',
        //     right: null,
        //     bottom: null,
        //     // width: 300,
        //     // height: 200,
        //     // top: 20,
        //     data: []
        // }]

    };
    var data=getData();
    option.xAxis.data=data.name;
    option.series=data.value;
    myWordCloud.setOption(
        option
    );
    console.log(option);
    setInterval(function () {
        var data=getData();
        option.xAxis.data=data.name;
        option.series=data.value;
        myWordCloud.setOption(
            option
        );

    }, 1200);
}
initWordCloud();
initEdu();
initJobClassWordCloud();
initCityBar();
initJobClassBar();



