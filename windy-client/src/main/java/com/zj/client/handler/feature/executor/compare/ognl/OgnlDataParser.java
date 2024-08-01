package com.zj.client.handler.feature.executor.compare.ognl;

import java.util.Map;
import java.util.Optional;

import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

public class OgnlDataParser {
    private static final String VARIABLE_CHAR = "$";
    public static final String RUNTIME_VARIABLE_CHAR = "#";
    public static final String BODY = "body";

    public Object exchangeOgnlParamValue(Object object, String expression) {
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
