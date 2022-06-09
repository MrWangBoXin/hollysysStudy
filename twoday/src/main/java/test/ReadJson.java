package test;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.xml.crypto.Data;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @BelongsProject: twoday
 * @BelongsPackage: test
 * @Author: wangboxin
 * @CreateTime: 2022-05-23  14:20
 * @Description: TODO
 * @Version: 1.0
 */
public class ReadJson {

    public static void main(String[] args) {
        //SimpleDateFormat sdf=new SimpleDateFormat("");
//        String time = new Date().toString();
//        System.out.println(time);
//        String[] s = time.split(" ");
//        System.out.println(s[1]);
//        System.out.println(s[2]);
//        System.out.println(s[3]);
        Date date=new Date();
        String time = date.toString();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String format = sdf.format(date);
        System.out.println(format);
        JSONObject jsonObject0 = JSON.parseObject("{\n" +
                "    \"data\": {\n" +
                "        \"findManyAsset\": [\n" +
                "            {\n" +
                "                \"id\": \"cl3icvlel70010sp98q9vx57y\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"extensions\": {\n" +
                "        \"tracing\": {\n" +
                "            \"version\": 1,\n" +
                "            \"startTime\": \"2022-05-23T07:50:04.906Z\",\n" +
                "            \"endTime\": \"2022-05-23T07:50:04.910Z\",\n" +
                "            \"duration\": 3315854,\n" +
                "            \"execution\": {\n" +
                "                \"resolvers\": [\n" +
                "                    {\n" +
                "                        \"path\": [\n" +
                "                            \"findManyAsset\"\n" +
                "                        ],\n" +
                "                        \"parentType\": \"Query\",\n" +
                "                        \"fieldName\": \"findManyAsset\",\n" +
                "                        \"returnType\": \"[Asset!]!\",\n" +
                "                        \"startOffset\": 219212,\n" +
                "                        \"duration\": 2879988\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"path\": [\n" +
                "                            \"findManyAsset\",\n" +
                "                            0,\n" +
                "                            \"id\"\n" +
                "                        ],\n" +
                "                        \"parentType\": \"Asset\",\n" +
                "                        \"fieldName\": \"id\",\n" +
                "                        \"returnType\": \"String!\",\n" +
                "                        \"startOffset\": 3178145,\n" +
                "                        \"duration\": 116635\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        JSONObject dataObject = jsonObject0.getJSONObject("data");
        JSONArray idObject = dataObject.getJSONArray("findManyAsset");
        JSONObject idAsset = JSON.parseObject(idObject.get(0).toString());
        System.out.println(idAsset.get("id").toString());

//        String ipList = FileUtil.readString("iplist", Charset.defaultCharset());
//        JSONArray ipArray = JSON.parseArray(ipList);
//        ipArray.forEach(t->{
//            JSONObject jsonObject = JSON.parseObject(t.toString());
//            if("1".equals(jsonObject.get("is_forward"))){
//                String rely_ip=jsonObject.get("ip").toString();
//                String rely_port=jsonObject.get("port").toString();
//
//
//            }
//        });


    }

}
