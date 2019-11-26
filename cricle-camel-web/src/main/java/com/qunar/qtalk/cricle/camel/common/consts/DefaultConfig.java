package com.qunar.qtalk.cricle.camel.common.consts;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author binz.zhang
 * @Date: 2019-11-26    14:23
 */
@Component
@Getter
public class DefaultConfig {

    @Value("${startalk_host}")
    public String SYSTEM_HOST;
}
