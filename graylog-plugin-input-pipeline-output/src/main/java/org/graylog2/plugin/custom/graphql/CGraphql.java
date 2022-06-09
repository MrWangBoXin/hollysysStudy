package org.graylog2.plugin.custom.graphql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.graylog2.plugin.custom.MyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CGraphql {
    private static final Logger LOG = LoggerFactory.getLogger(CGraphql.class);
    private static String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjazJlanlraDYwMDA0OWIwMm5uc3NxYjh4IiwiaWF0IjoxNjIwMjk1Mjg4LCJleHAiOjQ3NzYwNTUyODh9.RPGietH-ca9912wkZkkIp6fEDBTbg0ApHtI-EqxNxAI";
    private static String url = "http://beryllium:8080/";
    private static String siddhi_url = "http://siddhi:8006";
    private static Map<String,String[]> ipTypeRole = new HashMap<String, String[]>();

    public static void init(String surl, String stoken, String siddhi){
        if(surl != null && surl != ""){
            url = surl;
        }
        if(stoken != null && stoken != "") {
            token = stoken;
        }
        if(siddhi != null && siddhi != ""){
            siddhi_url = siddhi;
        }
        LOG.info("url is{}:,token is:{},siddhi_url is{}:",url,token,siddhi_url);
    }

    public static void updateWhitelist() {
        clearIpRole();
        List<AssetBean> beanList = CGraphql.GetAllAssets();
        if(!beanList.isEmpty()){
            for(AssetBean asset : beanList){
                String[] typeRole = new String[2];
                typeRole[0] = asset.getAssetType();
                typeRole[1] = "Unknown";
                if(asset.getAssetExtend() != null) {
                    Integer role = asset.getAssetExtend().getAssetRole();
                    if(role != null){
                        switch (role){
                            case 1:
                                typeRole[1] = "Ops";
                                break;
                            case 2:
                                typeRole[1] = "Svr";
                                break;
                            case 3:
                                typeRole[1] = "Eng";
                                break;
                            case 4:
                                typeRole[1] = "Com";
                                break;
                            case 5:
                                typeRole[1] = "Fcs";
                                break;
                            default:
                                typeRole[1] = "Unknown";
                                break;
                        }
                    }
                }
                /***b/c/las新增**/
                if(MyTools.isVersionBCLas()) {
                    List<AssetNetwork> assetNetwork = asset.getAssetNetwork();
                    if (assetNetwork.size() > 0) {
                        assetNetwork.forEach(t -> {
                            updateIpRole(t.getIpv4(), typeRole);
                        });

                    }
                }
                ipTypeRole.put(asset.getIp(),typeRole);
            }
            LOG.info("asset whitelist count is:{},{},{}",beanList.size(),ipTypeRole.size(),JSON.toJSONString(ipTypeRole));
        }
    }
    /***b/c/las新增**/
    public static synchronized void clearIpRole(){
        ipTypeRole.clear();
    }
    /***b/c/las新增**/
    public static synchronized void updateIpRole(String ip,String[] role){
        ipTypeRole.put(ip,role);
    }

    public static synchronized Map<String,String[]> getIpList() {
        return ipTypeRole;
    }

    public static synchronized Map<String,String> getIpType(String ip) {
        LOG.info("ipTypeRole is "+JSON.toJSONString(ipTypeRole)+",remoteIp is "+ip);
        Map<String,String> map = new HashMap<>();
        map.put("exist","no");
        map.put("type","unkown");
        if(ipTypeRole.containsKey(ip)){
            map.put("exist","yes");
            String[] assetType = ipTypeRole.get(ip);
            if(assetType != null) {
                map.put("type",assetType[0]);
            }
        }
        return map;
    }

    public static List<AssetBean> GetAllAssets(){
        List<AssetBean> assets = new ArrayList<>();
        try {
            String query ="";
            if(MyTools.isVersionBCLas()) {
                query = "{assets {ip,assetType,AssetStat{id,updatedAt,online},assetExtend{id},assetNetwork{id,ipv4}}}";
            }else {
                query = "{assets {ip,assetType,AssetStat{id,updatedAt,online},assetExtend{id}}}";
            }
            String respJson = GraphqlApi( query);
            LOG.debug("respJson allAssets is:{}",respJson);
            if(respJson != null) {
                ObjectMapper mapper=new ObjectMapper();
                AssetListBean whitelist = mapper.readValue(respJson, AssetListBean.class);
                assets = whitelist.getData().getAssets();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return assets;
    }

    public static void GraphqlUpdateDevOffline(long interval)  {
        List<AssetBean> assets = GetAllAssets();
        if(assets.isEmpty()){
            LOG.info("assets is:{}", assets);
            return;
        }

        int k = 0;
        String mutation = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        for(AssetBean asset : assets){
            AssetStatBean assetstat = asset.getAssetStat();
            if(assetstat != null){
                try {
                    long updateTime = format.parse(asset.getAssetStat().getUpdatedAt()).getTime();
                    long currentStamp = System.currentTimeMillis();
                    long difference = currentStamp - updateTime;
                    if(difference > interval){
                        LOG.info("offline asset.ip is:{},online is:{},difference is:{},interval is:{}", asset.getIp(),assetstat.getOnline(),difference,interval);
                        if(assetstat.getOnline()){
                            String prefix = String.format("row%d", k++);
                            String createStat = String.format("cpuUnit:\"\",cpuUsageRate:null,ramUnit:\"\",ramUsageRate:null,diskUnit:\"\",diskIdle:null,netUnit:\"\",netInput:null,netOutput:null,netPorts:\"\",online:false ");
                            String updateStat = String.format("cpuUnit:{set:\"\"},cpuUsageRate:{set:null},ramUnit:{set:\"\"},ramUsageRate:{set:null},diskUnit:{set:\"\"},diskIdle:{set:null},netUnit:{set:\"\"},netInput:{set:null},netOutput:{set:null},netPorts:{set:\"\"},online:{set:false} ");
                            String strDevicelog = String.format("%s:upsertOneAssetStat(where:{id:\"%s\"},create:{asset:{connect:{ip:\"%s\"}},%s}update:{%s}){id}",prefix, asset.getAssetStat().getId(), asset.getIp(), createStat, updateStat);
                            mutation += strDevicelog;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        if(mutation.length() > 0){
            mutation = String.format("mutation{%s}",mutation);
            GraphqlApi( mutation);
        }
    }

    //获取采集器对应的资产的SNMP参数
    public static List<AssetSnmpBean> GraphqlGetSnmpParam(String collectorId) {
        List<AssetSnmpBean> assets = new ArrayList<>();
        try {
            String query ="";
            if(MyTools.isVersionBCLas()) {
                query = String.format("{assets(where: {AND: [{logProtocol:{equals: SNMP}},{ collector: { id: { equals: \"%s\" } } },{OR: [{ blockCollectLog: { not: { equals: true } } },{ blockCollectStat: { not: { equals: true } } }]}]}){ip,snmpVersion,snmpCommunity,snmpPara{cpuUsageRateOid,cpuUnit,ramUsageRateOid,ramUnit,ramSizeOid,ramUsageOid,ramIdleOid,diskUnit,diskUsageRateOid,diskSizeOid,diskUsageOid,diskIdleOid,netUnit,statusOid,netInputOid,netOutputOid}}}", collectorId);
            }else {
                query = String.format("{assets(where: {AND: [{logProtocol:{equals: SNMP}},{ collector: { id: { equals: \"%s\" } } },{OR: [{ blockCollectLog: { not: { equals: true } } },{ blockCollectStat: { not: { equals: true } } }]}]}){ip,snmpVersion,snmpCommunity,snmpPara{cpuUsageRateOid,cpuUnit,ramUsageRateOid,ramUnit,ramSizeOid,ramUsageOid,ramIdleOid,diskUnit,diskUsageRateOid,diskSizeOid,diskUsageOid,diskIdleOid,netUnit,netInputOid,netOutputOid}}}", collectorId);
            }
            //LOG.info("Graphql GetSnmpParam by ip:{}", query);
            String respJson = GraphqlApi( query);
            LOG.info("respJson is:{}", respJson);
            if (respJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                AssetSnmpListBean whitelist = mapper.readValue(respJson, AssetSnmpListBean.class);
                assets = whitelist.getData().getAssets();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return assets;
    }

    private static AssetBean GraphqlGetAssetByIp(String ip){
        List<AssetBean> assets = GetAllAssets();
        if(assets.isEmpty()){
            LOG.warn("assets is:{}", assets);
            return null;
        }
        for(AssetBean asset : assets){
            if(asset.getIp().equals(ip)){
                return asset;
            }
        }
        return null;
    }

    public static void GraphqlUpdateDevStatus(DevStatusBean devStatus){
        AssetBean asset = GraphqlGetAssetByIp(devStatus.getIp());
        if(asset == null) {
            return;
        }
        String statId = "";
        if(asset.getAssetStat() != null){
            statId = asset.getAssetStat().getId();
        }
        if(MyTools.isVersionBCLas()) {
            String strNetports = JSON.toJSONString(devStatus.getInterfaceList());
            strNetports = strNetports.replaceAll("\"", "\\\\\"");
            String createStat = String.format("cpuUnit:\"%s\",cpuUsageRate:%d,ramUnit:\"%s\",ramUsageRate:%d,ramSize:%d,ramUsage:%d,ramIdle:%d,diskUnit:\"%s\",diskIdle:%d,diskSize:%d,diskUsage:%d,diskUsageRate:%d,netUnit:\"%s\",netInput:%d,netOutput:%d,netPorts:\"%s\",online:%s,onlineTime:1 ",
                    devStatus.getCpuUnit(), devStatus.getCpuUsageRate(), devStatus.getRamUnit(), devStatus.getRamUsageRate(), devStatus.getRamSize(), devStatus.getRamUsage(), devStatus.getRamIdle(), devStatus.getDiskUnit(), devStatus.getDiskIdle(), devStatus.getDiskSize(), devStatus.getDiskUsage(), devStatus.getDiskUsageRate(), devStatus.getNetUnit(), devStatus.getNetInput(), devStatus.getNetOutput(), strNetports, devStatus.getOnline());

            String updateStat = String.format("cpuUnit:{set:\"%s\"},cpuUsageRate:{set:%d},ramUnit:{set:\"%s\"},ramUsageRate:{set:%d},ramSize:{set:%d},ramUsage:{set:%d},ramIdle:{set:%d},diskUnit:{set:\"%s\"},diskIdle:{set:%d},diskSize:{set:%d},diskUsage:{set:%d},diskUsageRate:{set:%d},netUnit:{set:\"%s\"},netInput:{set:%d},netOutput:{set:%d},netPorts:{set:\"%s\"},online:{set:%s},onlineTime: { increment: 1 } ",
                    devStatus.getCpuUnit(), devStatus.getCpuUsageRate(), devStatus.getRamUnit(), devStatus.getRamUsageRate(), devStatus.getRamSize(), devStatus.getRamUsage(), devStatus.getRamIdle(), devStatus.getDiskUnit(), devStatus.getDiskIdle(), devStatus.getDiskSize(), devStatus.getDiskUsage(), devStatus.getDiskUsageRate(), devStatus.getNetUnit(), devStatus.getNetInput(), devStatus.getNetOutput(), strNetports, devStatus.getOnline());
            String mutation = String.format("mutation{one:upsertOneAssetStat(where:{id:\"%s\"},create:{asset:{connect:{ip:\"%s\"}},%s}update:{%s}){id}}", statId, devStatus.getIp(), createStat, updateStat);
            GraphqlApi(mutation);
        }else {
            String createStat = String.format("cpuUnit:\"%s\",cpuUsageRate:%d,ramUnit:\"%s\",ramUsageRate:%d,ramSize:%d,ramUsage:%d,ramIdle:%d,diskUnit:\"%s\",diskIdle:%d,diskSize:%d,diskUsage:%d,diskUsageRate:%d,netUnit:\"%s\",netInput:%d,netOutput:%d,netPorts:\"%s\",online:%s,onlineTime:1 ",
                    devStatus.getCpuUnit(), devStatus.getCpuUsageRate(), devStatus.getRamUnit(), devStatus.getRamUsageRate(), devStatus.getRamSize(), devStatus.getRamUsage(), devStatus.getRamIdle(), devStatus.getDiskUnit(), devStatus.getDiskIdle(), devStatus.getDiskSize(), devStatus.getDiskUsage(), devStatus.getDiskUsageRate(), devStatus.getNetUnit(), devStatus.getNetInput(), devStatus.getNetOutput(), devStatus.getNetPorts(), devStatus.getOnline());

            String updateStat = String.format("cpuUnit:{set:\"%s\"},cpuUsageRate:{set:%d},ramUnit:{set:\"%s\"},ramUsageRate:{set:%d},ramSize:{set:%d},ramUsage:{set:%d},ramIdle:{set:%d},diskUnit:{set:\"%s\"},diskIdle:{set:%d},diskSize:{set:%d},diskUsage:{set:%d},diskUsageRate:{set:%d},netUnit:{set:\"%s\"},netInput:{set:%d},netOutput:{set:%d},netPorts:{set:\"%s\"},online:{set:%s},onlineTime: { increment: 1 } ",
                    devStatus.getCpuUnit(), devStatus.getCpuUsageRate(), devStatus.getRamUnit(), devStatus.getRamUsageRate(), devStatus.getRamSize(), devStatus.getRamUsage(), devStatus.getRamIdle(), devStatus.getDiskUnit(), devStatus.getDiskIdle(), devStatus.getDiskSize(), devStatus.getDiskUsage(), devStatus.getDiskUsageRate(), devStatus.getNetUnit(), devStatus.getNetInput(), devStatus.getNetOutput(), devStatus.getNetPorts(), devStatus.getOnline());
            String mutation = String.format("mutation{one:upsertOneAssetStat(where:{id:\"%s\"},create:{asset:{connect:{ip:\"%s\"}},%s}update:{%s}){id}}", statId, devStatus.getIp(), createStat, updateStat);
            GraphqlApi(mutation);
        }
    }

    public static void UpdateOsinfo(String ip, String onelog){
        try {
            int begin = onelog.indexOf("{");
            int end = onelog.indexOf("}");
            if (begin <=0 || end <= 0){
                return;
            }
            String strOsinfo = onelog.substring(begin,end+1);
            LOG.info("strOsinfo is:{}",strOsinfo);
            String id = GetAssetExtendId(ip);
            if(id==null){
                LOG.info("strOsinfo can not find extendid");
            }
            ObjectMapper mapper=new ObjectMapper();
            AssetOsInfoBean osinfo = mapper.readValue(strOsinfo, AssetOsInfoBean.class);
            //更新操作系统版本
            String creatStr = String.format("hostName:\"%s\",OperatingSystem:\"%s\",releaseId:\"%s\",arch:\"%s\",majorVersion:\"%s\",minorVersion:\"%s\",assetRole:[%s],asset:{connect:{ip:\"%s\"}}",
                    osinfo.getHostName(), osinfo.getOs(), osinfo.getReleaseId(), osinfo.getArch(), osinfo.getMajor(), osinfo.getMinor(), osinfo.getRole(), ip);
            String updatestr = String.format("hostName:{set:\"%s\"},OperatingSystem:{set:\"%s\"},releaseId:{set:\"%s\"},arch:{set:\"%s\"},majorVersion:{set:\"%s\"},minorVersion:{set:\"%s\"},assetRole:[%s]",
                    osinfo.getHostName(), osinfo.getOs(), osinfo.getReleaseId(), osinfo.getArch(), osinfo.getMajor(), osinfo.getMinor(), osinfo.getRole());
            String mutation = String.format("mutation{upsertOneAssetExtend( where:{id:\"%s\"} create:{%s} update:{%s}){id}}", id, creatStr, updatestr);
            LOG.info("UpdateOsinfo mutation:{}", mutation);
            GraphqlApi(mutation);
            //更新补丁
            String patches = "";
            for (String hotfix : osinfo.getHotFixd() ){
                patches += String.format("{patchNumber:\"%s\"}", hotfix);
            }
            String mutationPatch = String.format("mutation{updateAssetOfAssetPatches( assetIp:\"%s\" patches:[%s]){id}}", ip, patches);
            GraphqlApi(mutationPatch);
            //ip-mac更新
            String networks = "";
            for (String ipMac : osinfo.getIpMac() ){
                String[] ipAndMac = ipMac.split("\\|");
                networks += String.format("{ipv4:\"%s\",mac:\"%s\"}", ipAndMac[0], ipAndMac[1]);
            }
            String mutationIpMac = String.format("mutation{updateAssetOfNetworks( assetIp:\"%s\" networks:[%s]){id}}", ip, networks);
            GraphqlApi(mutationIpMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ;
    }

    public static void GraphqlUpdateDevPorts(String ip, String onelog){
        try {
            int begin = onelog.indexOf("{");
            int end = onelog.indexOf("}");
            if (begin <=0 || end <= 0){
                return;
            }
            String tcpudpPorts = onelog.substring(begin,end+1);
            LOG.info("tcpudpPorts is:{}",tcpudpPorts);
            ObjectMapper mapper=new ObjectMapper();
            AssetRiskPortBean riskPort = mapper.readValue(tcpudpPorts, AssetRiskPortBean.class);
            String alltcp = "";
            for(String appPort : riskPort.getTcpPorts()) {
                String[] tcpkv = appPort.split(" ");
                if(tcpkv.length ==2) {
                    alltcp += String.format("{portNumber:%s,isTcp:true,appName:\"%s\"} ", tcpkv[0], tcpkv[1]);
                }
            }
            String alludp = "";
            for(String appudp : riskPort.getUdpPorts()){
                String[] udpkv = appudp.split(" ");
                if(udpkv.length == 2) {
                    alludp += String.format("{portNumber:%s,isUdp:true,appName:\"%s\"} ", udpkv[0], udpkv[1]);
                }
            }

            String mutation = String.format("mutation{updateAssetOfOpenPorts( assetIp:\"%s\" openPorts: [ %s %s ] ) {id}}", ip, alltcp, alludp);
            GraphqlApi( mutation);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String GetAssetExtendId(String ip){
        try {
            String query = "{assets(where:{ip:{equals:\""+ip+"\"}}){ip,AssetStat{id,updatedAt,online},assetExtend{id}}}";
            String respJson = GraphqlApi(query);
            LOG.info("respJson is:{}",respJson);
            if(respJson != null) {
                ObjectMapper mapper=new ObjectMapper();
                AssetListBean whitelist = mapper.readValue(respJson, AssetListBean.class);
                for(AssetBean asset : whitelist.getData().getAssets()){
                    if(asset != null){
                        AssetExtendBean extendBean = asset.getAssetExtend();
                        if(extendBean != null) {
                            LOG.info("respJson extend.id is:{}", extendBean.getId());
                            return extendBean.getId();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String BaselineCheck(String ip,List<BaselineCheckBean> basechecklist, Boolean delete){
        try {
            String deleteCheck = "deleteManyAssetCheck(where:{ip:{equals:\""+ip+"\"}}){count}";
            String allcheck = "";
            for(int i = 0; i < basechecklist.size(); i++) {
                BaselineCheckBean bean = basechecklist.get(i);
                String scanResult = bean.getScanResult().replaceAll("\\\\","\\\\\\\\");
                String onecheck="";

                if(MyTools.isVersionBCLas()) {
                    onecheck = String.format("row%d: createOneAssetCheck(data:{ip:\"%s\",checkType:%d,configCheck:{connect:{checkId:\"%s\"}},scanResult:\"%s\",checkResult:%s,weight:%d}){id} ", i, ip, bean.getCheckType(), bean.getConfigCheckId(), scanResult, bean.getCheckResult(), bean.getWeight());
                    allcheck += "\n"+onecheck;
                }else {
                    onecheck = String.format("row%d: createOneAssetCheck(data:{ip:\"%s\",checkType:%d,name:\"%s\",scanResult:\"%s\",checkResult:%s,weight:%d}){id} ",i,ip,bean.getCheckType(),bean.getName(),bean.getScanResult(),bean.getCheckResult(),bean.getWeight());
                    allcheck += onecheck;
                }

            }
            if(allcheck.length() == 0){
                return null;
            }
            if(MyTools.isVersionBCLas()) {
                System.out.println("---------------allcheck is:" + allcheck);
            }
            String mutation = "";
            if(delete) {
                mutation = String.format("mutation{%s %s}", deleteCheck, allcheck);
            }else{
                mutation = String.format("mutation{%s}", allcheck);
            }
            String respJson = GraphqlApi(mutation);
            LOG.info("respJson is:{}",respJson);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getPlatVersion(){
        String query ="{version{productionName}}";
        String result= GraphqlApi(query);
        JSONObject jsonObject=JSONObject.parseObject(result);
        if(jsonObject!=null){
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONObject dataObject1 = dataObject.getJSONObject("version");
            return dataObject1.get("productionName").toString();
        }
        return null;
    }

    public static String GraphqlApi(String queryStr){
        try {
            Map<String, String> query = new HashMap<String, String>();
            query.put("query",queryStr);
            ObjectMapper mapper=new ObjectMapper();
            String strjson = mapper.writeValueAsString(query);
            LOG.info("graphapi strjson is:{}",strjson);
            LOG.info("graphapi url is:{} token is {}",url,token);
            String respJson = SendHttpRequest(url,token,strjson);
            if(respJson != null) {
                if (respJson.contains("errors")) {
                    LOG.info("graphapi respJson error is:{}", respJson);
                    return null;
                }
                return respJson;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String SendSiddhiParams(String siddhiStream, String jsonData){
        try {
            ObjectMapper mapper=new ObjectMapper();
            Map<String,String> map = mapper.readValue(jsonData,new TypeReference<HashMap<String,String>>(){});
            String mutation = "";
            String operate = "";
            String protocol = "";
            if(map.containsKey("info")){
                operate = map.get("info");
                protocol = map.get("detail");
            }else if(map.containsKey("operate")){
                operate = map.get("operate");
                protocol = map.get("protocol");
            }

            mutation = String.format("mutation {\n createOneSiddhiAlert(data: {occurrenceAt:\"%s\", srcIp: \"%s\", dstIp: \"%s\", operate:\"%s\", protocol:\"%s\",tag: \"%s\", level: %s, eventId:\"%s\", id: \"%s\"}) {\n    id\n  }\n}",
                    map.get("timestamp"), map.get("src_ip"), map.get("dst_ip"), operate, protocol, map.get("tag"), map.get("level"), map.get("rulenum"), map.get("uuid"));
            String respJson = GraphqlApi(mutation);
            if(MyTools.isVersionBCLas()) {
                LOG.info("siddhiAlert respJson is:{}", respJson);
                String strjson = String.format("{\"event\":%s}", jsonData);
                respJson = SendHttpRequest(siddhi_url + siddhiStream, "", strjson);
            }
            LOG.info("siddhi respJson is:{}",respJson);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String SendHttpRequest(String url, String token, String jsonData){
        String returnValue = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try{
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            StringEntity requestEntity = new StringEntity(jsonData,"utf-8");
            httpPost.setHeader("Content-type", "application/json;charset=UTF-8");
            if(token != null) {
                httpPost.setHeader("Authorization", token);
            }
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost);
            if(response != null) {
                HttpEntity responseEntity = response.getEntity();
                returnValue = EntityUtils.toString(responseEntity);
                int state = response.getStatusLine().getStatusCode();
                if (state != HttpStatus.SC_OK) {
                    LOG.warn("请求返回:" + state + "(" + url + ")");
                }
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return returnValue;
    }
}
