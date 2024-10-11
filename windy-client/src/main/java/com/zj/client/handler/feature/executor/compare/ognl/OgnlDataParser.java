package com.zj.client.handler.feature.executor.compare.ognl;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

@Slf4j
public class OgnlDataParser {
    private static final String VARIABLE_CHAR = "$";
    public static final String RUNTIME_VARIABLE_CHAR = "#";
    public static final String BODY = "body";

    public Object exchangeOgnlParamValue(Object object, String expression) {
        if (Objects.isNull(object)) {
            log.info("source obj is null not exchange");
            return null;
        }

        expression = expression.replace(VARIABLE_CHAR, RUNTIME_VARIABLE_CHAR);
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(this,
                new DefaultMemberAccess(true),
                new DefaultClassResolver(),
                new DefaultTypeConverter());
        context.setRoot(object);
        context.put(BODY, object);

        Object ans = null;
        try {
            ans = Ognl.getValue(Ognl.parseExpression(expression), context, context.getRoot());
        } catch (OgnlException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public boolean judgeExpression(Map<String, Object> map, String expression) {
        try {
            expression = expression.replace(VARIABLE_CHAR, RUNTIME_VARIABLE_CHAR);
            Object result = Ognl.getValue(Ognl.parseExpression(expression), map, new Object());
            return Optional.ofNullable(result).map(r -> Boolean.parseBoolean(String.valueOf(result))).orElse(false);
        } catch (OgnlException e) {
            e.printStackTrace();
        }
        return false;
    }
}
