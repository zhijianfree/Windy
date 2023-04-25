package com.zj.common.utils;

import java.util.Collections;
import java.util.List;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * 对象拷贝
 */
public class OrikaUtil {
    private static MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    public static MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    public static <T> T convert(Object source, Class<T> target) {
        return mapperFactory.getMapperFacade().map(source, target);
    }

    public static <S, D> List<D> convertList(Iterable<S> source, Class<D> target) {
        if (source == null) {
            return Collections.emptyList();
        }
        return mapperFactory.getMapperFacade().mapAsList(source, target);

    }
}
