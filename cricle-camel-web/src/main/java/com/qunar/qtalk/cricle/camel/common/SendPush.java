package com.qunar.qtalk.cricle.camel.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qunar.qtalk.cricle.camel.common.consts.DefaultConfig;
import com.qunar.qtalk.cricle.camel.common.dto.SendMessageParam;
import com.qunar.qtalk.cricle.camel.common.dto.UserModelDto;
import com.qunar.qtalk.cricle.camel.common.util.HttpClientUtils;
import com.qunar.qtalk.cricle.camel.common.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.*;


/**
 * SendPush
 *
 * @author binz.zhang
 * @date 2019/1/14
 */
@Component
@Slf4j
public class SendPush {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendPush.class);

    @Value("${url_send_notify}")
    private String sendUrl;

    @Value("${qtalk_send_message}")
    private String sendMessageUrl;

    @Resource
    private DefaultConfig defaultConfig;

    public boolean sendNotify(UserModelDto user, String data) {
        Dictionary<String, Object> args = new Hashtable<>();
        args.put("from", "admin"+"@"+defaultConfig.getSYSTEM_HOST());
        args.put("category", "12");
        args.put("data", data);
        args.put("to", user.getBareJid());
        String res = null;
        try {
            res = HttpClientUtils.postJson(sendUrl, JSON.toJSONString(args));
            log.info("normal send notify to :{}, ret;{}", JSON.toJSONString(args), res);
        } catch (Exception e) {
            log.error("发送通知消息异常,{}", e);
            return false;
        }
        if (!checkPushResult(res)) {
            return false;
        }
        return true;
    }
    public boolean bathSendNotify(List<String> users, String data) {
        Dictionary<String, Object> args = new Hashtable<>();
        args.put("from", "admin"+"@"+defaultConfig.getSYSTEM_HOST());
        args.put("category", "12");
        args.put("data", data);
        args.put("to", users);
        String res = null;
        try {
            res = HttpClientUtils.postJson(sendUrl, JSON.toJSONString(args));
            log.info("normal send notify to :{}, ret;{}", JSON.toJSONString(args), res);
        } catch (Exception e) {
            log.error("发送通知消息异常,{}", e);
            return false;
        }
        if (!checkPushResult(res)) {
            return false;
        }
        return true;
    }

    public boolean sendMessage(String msg) {
        try {
            SendMessageParam sendMessageParam;
            if (Strings.isNullOrEmpty(msg)) {
                return false;
            }
            sendMessageParam = JacksonUtils.string2Obj(msg, SendMessageParam.class);
            List<SendMessageParam.ToEntity> toUsers = sendMessageParam.getTo();
            if (toUsers == null || toUsers.size() == 0) {
                return false;
            }
            toUsers.stream().forEach(x -> {
                String originMsg = makeMessage(sendMessageParam.getType(), sendMessageParam.getFrom() + "@" + sendMessageParam.getFromhost(), x.getUser() + "@" + x.getHost(),
                        sendMessageParam.getExtendinfo(), sendMessageParam.getMsgtype(), sendMessageParam.getContent(), sendMessageParam.getBackupinfo(), sendMessageParam.getAuto_reply());
                String res;
                res = HttpClientUtils.postJson(sendMessageUrl, originMsg);
                LOGGER.info("send message to :{}, ret;{}", msg, res);

            });
        } catch (Exception e) {
            LOGGER.error("send message error,msg is {}", msg);
            return false;
        }
        return true;
    }


    /**
     * sendPush 返回结果校验
     *
     * @param result true:成功
     * @return
     */
    private boolean checkPushResult(String result) {
        boolean resStatus = false;
        try {
            if (!Strings.isNullOrEmpty(result)) {
                JSONObject receivedParam = JSON.parseObject(result);
                resStatus = (Boolean) receivedParam.get("ret");
            }
        } catch (Exception e) {
            LOGGER.error("sendPush 返回结果解析异常", e);
        }
        return resStatus;
    }
    public static String makeMessage(String type, String from, String to,  String extendInfo, String msgType, String content,
                                     String backupinfo, String autoReply) {

        Document document = DocumentHelper.createDocument();
        Element message = document.addElement("message");

        message.addAttribute("from", from);
        message.addAttribute("to", to);
        message.addAttribute("auto_reply", autoReply);
        message.addAttribute("type", type);

        Element body = message.addElement("body");
        body.addAttribute("id", "qtalk-corp-service-" + UUID.randomUUID().toString());
        body.addAttribute("msgType", msgType);
        body.addAttribute("maType", "20");
        body.addAttribute("extendInfo", extendInfo);
        body.addAttribute("backupinfo", backupinfo);
        body.addText(content);

        Map<String, String> args = new HashMap<>();
        args.put("from", from);
        args.put("to", to);
        args.put("message", message.asXML());
        args.put("system", "oa-service");
        args.put("type", type);
        return  JacksonUtils.obj2String(args);
    }
}
