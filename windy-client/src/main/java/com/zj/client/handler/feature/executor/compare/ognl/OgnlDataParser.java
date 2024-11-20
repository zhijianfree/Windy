package com.zj.client.handler.feature.executor.compare.ognl;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.zj.common.entity.WindyConstants;
import lombok.extern.slf4j.Slf4j;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

@Slf4j
public class OgnlDataParser {

    public Object exchangeOgnlResponseValue(Object object, String expression) {
        if (Objects.isNull(object)) {
            log.info("source obj is null not exchange");
            return null;
        }

        expression = expression.replace(WindyConstants.VARIABLE_CHAR, WindyConstants.RUNTIME_VARIABLE_CHAR);
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(this,
                new DefaultMemberAccess(true),
                new DefaultClassResolver(),
                new DefaultTypeConverter());
        context.setRoot(object);
        context.put(WindyConstants.BODY, object);
        try {
            return Ognl.getValue(Ognl.parseExpression(expression), context, context.getRoot());
        } catch (OgnlException e) {
            log.info("ognl parse error",e);
        }
        return null;
    }

    public Object exchangeOgnlValue(Object object, String expression) {
        if (Objects.isNull(object)) {
            log.info("source obj is null not exchange");
            return null;
        }
        expression = expression.replace(WindyConstants.VARIABLE_CHAR, "#context.");
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(this,
                new DefaultMemberAccess(true),
                new DefaultClassResolver(),
                new DefaultTypeConverter());
        context.setRoot(object);
        context.put("context", object);
        try {
            return Ognl.getValue(Ognl.parseExpression(expression), context, context.getRoot());
        } catch (OgnlException e) {
            log.info("ognl parse error",e);
        }
        return null;
    }

    public boolean judgeExpression(Map<String, Object> map, String expression) {
        try {
            expression = expression.replace(WindyConstants.VARIABLE_CHAR, WindyConstants.RUNTIME_VARIABLE_CHAR);
            Object result = Ognl.getValue(Ognl.parseExpression(expression), map, new Object());
            return Optional.ofNullable(result).map(r -> Boolean.parseBoolean(String.valueOf(result))).orElse(false);
        } catch (OgnlException e) {
            log.info("");
        }
        return false;
    }
}
