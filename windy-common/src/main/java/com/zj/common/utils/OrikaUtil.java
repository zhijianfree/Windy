package com.zj.common.utils;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 对象拷贝
 */
public class OrikaUtil {
    private static final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    public static <T> T convert(Object source, Class<T> target) {
        return mapperFactory.getMapperFacade().map(source, target);
    }

    public static <S, D> List<D> convertList(Iterable<S> source, Class<D> target) {
        if (Objects.isNull(source)) {
            return Collections.emptyList();
        }
        return mapperFactory.getMapperFacade().mapAsList(source, target);

    }
}
