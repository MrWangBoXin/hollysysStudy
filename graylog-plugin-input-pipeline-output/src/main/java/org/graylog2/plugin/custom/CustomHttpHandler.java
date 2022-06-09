package org.graylog2.plugin.custom;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.input.UdpSnmpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * 处理/myserver路径请求的处理器类
 */
public class CustomHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UdpSnmpTransport.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            String responsetext = "0";
            String reqParam = getRequestParam(httpExchange);
            LOG.info("asset has changed,请求参数：",reqParam);
            if(reqParam == null){
                responsetext = "unknow";
            }else{
                if(reqParam.equals("asset")) {
                    CGraphql.updateWhitelist();
                }if(reqParam.equals("snmp")) {
                    CGraphql.updateWhitelist();
                }else{
                    responsetext = "unknow";
                }
            }

            handleResponse(httpExchange, responsetext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取请求参数
     * @param httpExchange
     * @return
     * @throws Exception
     */
    private String getRequestParam(HttpExchange httpExchange) throws Exception {
        String paramStr = "";

        if (httpExchange.getRequestMethod().equals("GET")) {
            //GET请求读queryString
            paramStr = httpExchange.getRequestURI().getQuery();
        } else {
            //非GET请求读请求体
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
            StringBuilder requestBodyContent = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                requestBodyContent.append(line);
            }
            paramStr = requestBodyContent.toString();
        }

        return paramStr;
    }

    /**
     * 处理响应
     * @param httpExchange
     * @param responsetext
     * @throws Exception
     */
    private void handleResponse(HttpExchange httpExchange, String responsetext) throws Exception {
        //生成html
        StringBuilder responseContent = new StringBuilder();
        responseContent.append(responsetext);
        String responseContentStr = responseContent.toString();
        byte[] responseContentByte = responseContentStr.getBytes("utf-8");

        //设置响应头，必须在sendResponseHeaders方法之前设置！
        httpExchange.getResponseHeaders().add("Content-Type:", "text/plain;charset=utf-8");

        //设置响应码和响应体长度，必须在getResponseBody方法之前调用！
        httpExchange.sendResponseHeaders(200, responseContentByte.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(responseContentByte);
        out.flush();
        out.close();
    }
}
