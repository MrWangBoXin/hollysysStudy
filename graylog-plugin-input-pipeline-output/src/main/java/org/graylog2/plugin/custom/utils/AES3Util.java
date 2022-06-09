package org.graylog2.plugin.custom.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * AES对称加密算法
 *  ===========================================================================================================
 *  这里演示的是其Java6.0的实现,理所当然的BouncyCastle也支持AES对称加密算法
 *  另外,我们也可以以AES算法实现为参考,完成RC2,RC4和Blowfish算法的实现
 *  ===========================================================================================================
 *  由于DES的不安全性以及DESede算法的低效,于是催生了AES算法(Advanced Encryption Standard)
 *  该算法比DES要快,安全性高,密钥建立时间短,灵敏性好,内存需求低,在各个领域应用广泛
 *  目前,AES算法通常用于移动通信系统以及一些软件的安全外壳,还有一些无线路由器中也是用AES算法构建加密协议
 *  ===========================================================================================================
 *  由于Java6.0支持大部分的算法,但受到出口限制,其密钥长度不能满足需求
 *  所以特别需要注意的是:如果使用256位的密钥,则需要无政策限制文件(Unlimited Strength Jurisdiction Policy Files)
 *  不过Sun是通过权限文件local_poblicy.jar和US_export_policy.jar做的相应限制,我们可以在Sun官网下载替换文件,减少相关限制
 *  网址为http://www.Oracle.com/technetwork/java/javase/downloads/index.html
 *  在该页面的最下方找到Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 6,点击下载
 *  http://download.oracle.com/otn-pub/java/jce_policy/6/jce_policy-6.zip
 *  http://download.oracle.com/otn-pub/java/jce/7/UnlimitedJCEPolicyJDK7.zip
 *  然后覆盖本地JDK目录和JRE目录下的security目录下的文件即可
 *  ===========================================================================================================
 *  关于AES的更多详细介绍,可以参考此爷的博客http://blog.csdn.net/kongqz/article/category/800296
 * @create Jul 17, 2012 6:35:36 PM
 * @author 玄玉(http://blog.csdn/net/jadyer)
 */
public class AES3Util {
    /** 密钥算法 */
    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_CSERVER = "keyvalue8cserver";
    /**加解密算法/工作模式/填充方式,Java6.0支持PKCS5Padding填充方式,BouncyCastle支持PKCS7Padding填充方式 */
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 转换密钥
     */
    public static Key toKey(byte[] key) throws Exception {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }


    /**
     * 加密数据
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return 加密后的数据
     */
    public static String encrypt(String data, String key) throws Exception {
        //Key k = toKey(Base64.decodeBase64(key));//还原密钥
        Key k = toKey(KEY_CSERVER.getBytes());
        //使用PKCS7Padding填充方式,这里就得这么写了(即调用BouncyCastle组件实现)
        //实例化Cipher对象，它用于完成实际的加密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化Cipher对象，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        //执行加密操作。加密后的结果通常都会用Base64编码进行传输
        return Base64.encodeBase64URLSafeString(cipher.doFinal(data.getBytes()));
    }


    /**
     * 解密数据
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return 解密后的数据
     */
    public static String decrypt(String data, String key) throws Exception {
        Key k = toKey(KEY_CSERVER.getBytes());
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化Cipher对象，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        //执行解密操作
        return new String(cipher.doFinal(Base64.decodeBase64(data)));
    }

    public static void main(String[] args) throws Exception {
        String loginName = "13032972012";
        String currentTimeMillis=String.valueOf(System.currentTimeMillis());
        System.out.println("时间戳：" + currentTimeMillis);
        System.out.println("时间戳前5位：" + currentTimeMillis.substring(0,5));
        System.out.println("时间戳剩余：" + currentTimeMillis.substring(5));
        String salt=currentTimeMillis.substring(0,5)+loginName.substring(0,5)+currentTimeMillis.substring(5);
        System.out.println("salt:"+salt);
        String key="hollysys_1234567";
        String sign = encrypt(salt, key);
        System.out.println("加密sign：" + sign);

        String decryptData = decrypt(sign, key);
        System.out.println("解密salt: " + decryptData);

        if(!salt.equals(decrypt(sign, key))){
            System.out.println(false);
        }
    }
}