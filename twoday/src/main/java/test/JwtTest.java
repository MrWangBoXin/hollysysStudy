package test;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import utils.PemUtils;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JwtTest {
    private static final String PRIVATE_KEY_FILE = "src/main/resources/private.pem";
    private static final String PUBLIC_KEY_FILE = "src/main/resources/public.pem";

    private static final String CLUSTER_ID= "0160E22D-6066-4841-B078-02A34CC5CB01";

    public static void main(String[] args) throws IOException {
        System.out.println(genetatetJwtToken());
    }
    private static String genetatetJwtToken() throws IOException {



        //Header
        Map<String,Object> mapHeader=new HashMap<>(1);
        mapHeader.put("alg","RS512");


        Map<String,Object> mapPayload=new HashMap<>();
        mapPayload.put("version","2");
        mapPayload.put("jti","7d3fbfbb-c9a5-4f25-ad89-dc96013890c1");
        mapPayload.put("iss","Graylog, Inc.");
        mapPayload.put("sub","/license/enterprise");
        mapPayload.put("aud","/recipient");
        mapPayload.put("iat",2620629278L);
        mapPayload.put("exp",2652165278L);
        mapPayload.put("nbf",1620629278L);
        mapPayload.put("trial",false);

        // /recipient
        Map<String,Object> mapRecipient=new HashMap<>(3);
        mapRecipient.put("company","holi");
        mapRecipient.put("name","gang guo");
        mapRecipient.put("email","guogang181422@hollysys.com");

        //"enterprise"
        Map<String,Object> mapEnterprise=new HashMap<>();
        mapEnterprise.put("cluster_ids",new String[]{CLUSTER_ID});
        mapEnterprise.put("require_remote_check",false);
        mapEnterprise.put("allowed_remote_check_failures",7200000);
        mapEnterprise.put("traffic_limit",536870912000L);
        mapEnterprise.put("traffic_check_range",2592000);
        mapEnterprise.put("allowed_traffic_violations",5);
        mapEnterprise.put("expiration_warning_range",2592000);


        String jwtToken1 = JWT.create()
                // Header
                .withHeader(mapHeader)
                // Payload
                .withPayload(mapPayload)
                .withClaim("/recipient",mapRecipient)
                .withClaim("enterprise",mapEnterprise)
                .sign(Algorithm.RSA512((RSAPublicKey)PemUtils.readPublicKeyFromFile(PUBLIC_KEY_FILE,"RSA"),
                                       (RSAPrivateKey)PemUtils.readPrivateKeyFromFile(PRIVATE_KEY_FILE,"RSA")));

        return jwtToken1;
    }

}
