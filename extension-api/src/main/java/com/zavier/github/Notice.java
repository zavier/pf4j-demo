package com.zavier.github;

import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * 通知功能
 */
public interface Notice extends ExtensionPoint {

    boolean notice(List<Long> userIds);
}
