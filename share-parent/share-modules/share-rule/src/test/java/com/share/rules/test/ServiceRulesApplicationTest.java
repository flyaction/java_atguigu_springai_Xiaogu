package com.share.rules.test;

import com.share.rules.config.DroolsHelper;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServiceRulesApplicationTest {
    @Test
    void test1() {
        // 开启会话
        //KieSession kieSession = kieContainer.newKieSession();
        KieSession kieSession = DroolsHelper.loadForRule("rules/TestRule.drl");

        // 触发规则
        kieSession.fireAllRules();
        // 中止会话
        kieSession.dispose();
    }
}
