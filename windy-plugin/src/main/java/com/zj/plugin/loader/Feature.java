package com.zj.plugin.loader;

import java.util.List;

public interface Feature {
    List<FeatureDefine> scanFeatureDefines();
}