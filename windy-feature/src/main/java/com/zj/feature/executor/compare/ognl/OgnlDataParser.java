package com.zj.feature.executor.compare.ognl;

import java.util.Map;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

public class OgnlDataParser {

    public static final String BODY = "body";

    public Object parserExpression(Object object, String expression) {
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

    public Object parserExpression(Map<String, Object> map, String expression,Object obj) {
        Object ans = null;
        try {
            ans = Ognl.getValue(Ognl.parseExpression(expression), map, obj);
        } catch (OgnlException e) {
            e.printStackTrace();
        }
        return ans;
    }
}
