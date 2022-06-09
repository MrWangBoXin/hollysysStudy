package siddhiTest;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @BelongsProject: twoday
 * @BelongsPackage: siddhiTest
 * @Author: wangboxin
 * @CreateTime: 2022-05-25  16:05
 * @Description: TODO
 * @Version: 1.0
 */
public class Test3 {
    public static void main(String[] args) {
        String file="{\"key\":\"123\"}";
        JSONObject jsonObject = JSON.parseObject(file);
        System.out.println(jsonObject);
        jsonObject.replace("key","wang");
        System.out.println(jsonObject);

    }
}
