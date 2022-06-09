package org.graylog2.plugin.custom.rest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.custom.graphql.AssetSnmpBean;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.graphql.DevStatusBean;
import org.graylog2.plugin.custom.input.SnmpGetTask;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static org.graylog2.plugin.custom.graphql.CGraphql.GraphqlApi;

@Api(value = "Collector", description = "提供给子采集器接口")
@Path("/collector")
public class CollectorResource extends RestResource implements PluginRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorResource.class);

    private String platToken = "rest-token-hollysys123456";

    /**
     * @description:获取资产列表
     * @author: wangboxin
     * @date: 2022/5/9 14:01
     * @param: [collectorId]
     * @return:{
     *   "data": {
     *     "assets": [
     *       {
     *         "ip": "172.21.34.9",
     *         "assetType": "UpperComputer",
     *         "AssetStat": null
     *       },
     *       {
     *         "ip": "172.21.34.10",
     *         "assetType": "Switch",
     *         "AssetStat": null
     *       },
     *       {
     *         "ip": "172.21.34.12",
     *         "assetType": "UpperComputer",
     *         "AssetStat": {
     *           "id": "cl38hyb5e13850to28ueyns4e",
     *           "updatedAt": "2022-05-16T10:05:03.238Z",
     *           "online": true
     *         }
     *       }
     *     ]
     *   }
     * }
     **/
    @POST
    @Path("/assetList")
    @Timed
    @ApiOperation(value = "资产列表查询-白名单.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Asset List is NUll.")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String assetList(@ApiParam(name = "jsonObject", required = true)@Valid JSONObject jsonObject) {
        LOG.info("/collector/assetList param:jsonObject={}", jsonObject.toJSONString());

        String collectorId=jsonObject.getString("collectorId");
        String restToken=jsonObject.getString("restToken");

        if(StringUtils.isNotEmpty(collectorId) && !platToken.equals(restToken)){
            //restToken 校验失败，非法访问
            return "error";
        }
        String queryAssetsHql = String.format("{assets(where: {AND: [{ collector: { id: { equals: \"%s\" } } },{OR: [{ blockCollectLog: { not:{equals:true} } },{ blockCollectStat: { not: {equals:true} } }]}]}) {ip,assetType,AssetStat{id,updatedAt,online},assetNetwork{id,ipv4}}}", collectorId);
        //获取资产白名单
        String assetJsonString ;
        JSONArray assetList ;
        try {
            assetJsonString = GraphqlApi(queryAssetsHql);

            if (assetJsonString != null) {
                JSONObject jsonObject0 = JSON.parseObject(assetJsonString);
                JSONObject dataObject = jsonObject0.getJSONObject("data");
                assetList = dataObject.getJSONArray("assets");
                LOG.info("资产白名单：{}", assetList.toJSONString());

                return assetList.toJSONString();
            } else {
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("/collector/assetList 获取白名单错误");
        }
        return "error";
    }

    @POST
    @Path("/getCollectorConfig")
    @Timed
    @ApiOperation(value = "获取采集器配置信息.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollectorConfig(@ApiParam(name = "jsonObject", required = true)JSONObject jsonObject) {

        LOG.info("/collector/getCollectorConfig param:restToken={}",jsonObject.toJSONString());
        String restToken=jsonObject.getString("restToken");
        if(!platToken.equals(restToken)){
            //restToken 校验失败，非法访问
            return "error";
        }
        String query = "{findManyCollector{ id name ip port isMain heartInterval snmpInterval }}";

        String jsonString;
        JSONArray collectors;
        try {
            jsonString = GraphqlApi(query);
            if (jsonString != null) {
                JSONObject jsonObject0 = JSON.parseObject(jsonString);
                JSONObject dataObject = jsonObject0.getJSONObject("data");
                collectors = dataObject.getJSONArray("findManyCollector");
                LOG.info("CollectorConfig respJson is:{}", collectors.toJSONString());
                return collectors.toJSONString();
            }else {
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("获取采集器配置信息错误！");
        }


        return "error";
    }

    /**
     * @description:获取设备参数信息
     * @author: wangboxin
     * @date: 2022/5/9 14:22
     * @param: []
     * @return:
     *[
     *   {
     *     "snmpCommunity": "public",
     *     "snmpVersion": "SNMPv1",
     *     "ip": "172.21.33.33",
     *     "snmpPara": {
     *       "diskIdleOid": "",
     *       "diskSizeOid": "",
     *       "diskUsageRateOid": "",
     *       "cpuUnit": "%",
     *       "diskUnit": "MB",
     *       "ramUsageRateOid": "",
     *       "netUnit": "bps",
     *       "ramSizeOid": "1.3.6.1.4.1.36971.100.1.11.6.0",
     *       "netInputOid": "",
     *       "ramUnit": "MB",
     *       "cpuUsageRateOid": "1.3.6.1.4.1.36971.100.1.11.10.0",
     *       "ramIdleOid": "",
     *       "ramUsageOid": "1.3.6.1.4.1.36971.100.1.11.7.0",
     *       "netOutputOid": "",
     *       "diskUsageOid": ""
     *     }
     *   }
     * ]
     **/

    @POST
    @Path("/getDevInfo")
    @Timed
    @ApiOperation(value = "获取设备参数信息.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getDevInfo(@ApiParam(name = "jsonObject", required = true)JSONObject jsonObject) {
        LOG.info("/collector/getDevInfo param:jsonObject={}", jsonObject.toJSONString());

        String collectorId=jsonObject.getString("collectorId");
        String restToken=jsonObject.getString("restToken");



        if(StringUtils.isNotEmpty(collectorId) && !platToken.equals(restToken)){
            //restToken 校验失败，非法访问
            return "error";
        }
        //获取设备设备的参数信息
        List<AssetSnmpBean> snmpBeans = CGraphql.GraphqlGetSnmpParam(collectorId);
        String jsonString = JSON.toJSONString(snmpBeans);
        LOG.info("设备参数信息：{}", jsonString);
        return jsonString;
    }


    /**
     * @description: 更新设备状态, 计算放在平台插件
     * @author: wangboxin
     * @date: 2022/5/9 14:21
     * @param: [devStatus]
     * @return: true/false
     **/
    @POST
    @Path("/updateDevStatus")
    @Timed
    @ApiOperation(value = "更新设备状态.")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateDevStatus(@ApiParam(name = "jsonObject", required = true)JSONObject jsonObject) throws IOException {
        LOG.info("/collector/updateDevStatus param:jsonObject={}", jsonObject.toJSONString());

        String devStatusJson=jsonObject.getString("devStatus");
        String restToken=jsonObject.getString("restToken");
        if(StringUtils.isNotEmpty(devStatusJson) && !platToken.equals(restToken)){
            //restToken 校验失败，非法访问
            return "error";
        }

        DevStatusBean devStatusBean = JSON.parseObject(devStatusJson, DevStatusBean.class);

        try {
            SnmpGetTask.updateStat(devStatusBean);
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(false);
        }
        return String.valueOf(true);
    }
}

