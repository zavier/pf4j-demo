package com.zavier.github;

import com.google.common.collect.Lists;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要实现接口及增加Extension注解
 */
@Extension
public class EmailNotice implements Notice {

    @Override
    public boolean notice(List<Long> userIds) {
        // todo逻辑实现
        final ArrayList<Object> objects = Lists.newArrayList();
        System.out.println("email notice");
        return true;
    }
}
