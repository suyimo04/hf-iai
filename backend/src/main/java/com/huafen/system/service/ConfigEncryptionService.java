package com.huafen.system.service;

/**
 * 配置加密服务接口
 * 使用AES-256-GCM算法对敏感配置进行加密解密
 */
public interface ConfigEncryptionService {

    /**
     * 加密明文
     * @param plainText 明文
     * @return Base64编码的 IV+密文
     */
    String encrypt(String plainText);

    /**
     * 解密密文
     * @param encrypted Base64编码的 IV+密文
     * @return 明文
     */
    String decrypt(String encrypted);

    /**
     * 掩码显示敏感值
     * @param value 原始值
     * @return 格式: "前2位****后2位"
     */
    String mask(String value);
}
