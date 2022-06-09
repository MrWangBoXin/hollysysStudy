package siddhiTest;


import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graylog.bean.AssetBean;

import javax.xml.crypto.Data;

public class Test {


    public static void main(String[] args) throws JsonProcessingException {

//        String id="Basic YWRtaW46YWRtaW4=";
//        JSONObject paramMap = new JSONObject();
//        paramMap.put("restToken",id);
//
//        System.out.println(paramMap.toJSONString());
//        String format = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//        System.out.println(format);
        TimeZone tz = TimeZone.getTimeZone("GMT+8");
        TimeZone.setDefault(tz);
//        SimpleDateFormat sdf=new SimpleDateFormat();
//        sdf.setTimeZone(tz);
//        String format = sdf.format(new Date());
//        System.out.println(format);
        //System.out.println(new TimeStamp().toString());
        //Timestamp timestamp = new Timestamp();
//        System.out.println();
//        TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
//        TimeZone.setDefault(tz);
        Date date = new Date();
        String time = date.toString();
        System.out.println(time);
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("Authorization:",id);
//    String logtime="2022-05-24T09:33:25.411Z";
//        System.out.println(logtime.replace('T', ' ').replace("Z", "").substring(0,logtime.lastIndexOf('.')));
////        String json=HttpUtil.get(,paramMap,basic);
//        String basic = HttpUtil.buildBasicAuth("admin", "admin", CharsetUtil.CHARSET_UTF_8);
//
//        String json=HttpRequest.get("http://172.21.88.95:9000/api/collector/GetDevInfo")
//                .header(Header.AUTHORIZATION, id)//头信息，多个头信息多次调用此方法即可
//                //.form(paramMap)//表单内容
//                .timeout(20000)//超时，毫秒
//                .execute().body();
//
//
//        System.out.println(json);
//
//                JSONObject jsonObject0 = JSON.parseObject(json);
//
//                JSONObject jsonObject1=jsonObject0.getJSONObject("data");
//
//                JSONArray collectors = jsonObject1.getJSONArray("collectors");
//
//                collectors.forEach(collector->{
//                    JSONObject jsonObject = JSON.parseObject(collector.toString());
//                    System.out.println(jsonObject.get("ip").equals("127.0.0.1"));
//                });

//        List<AssetBean> assetBeans = JSON.parseArray("[\n" +
//                "  {\n" +
//                "    \"AssetStat\": {\n" +
//                "      \"online\": false,\n" +
//                "      \"id\": \"cl32tny8u327580sp9ng44rbsd\",\n" +
//                "      \"updatedAt\": \"2022-05-13T08:10:12.851Z\"\n" +
//                "    },\n" +
//                "    \"ip\": \"172.21.88.7\",\n" +
//                "    \"assetType\": \"UpperComputer\"\n" +
//                "  },\n" +
//                "  {\n" +
//                "    \"ip\": \"172.21.21.21\",\n" +
//                "    \"assetType\": \"Switch\"\n" +
//                "  }\n" +
//                "]", AssetBean.class);
//        assetBeans.forEach(t->{
//                    System.out.println(t);
//                }
//        );


    }


}
