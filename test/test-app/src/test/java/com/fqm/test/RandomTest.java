package com.fqm.test;

import org.apache.commons.lang3.RandomStringUtils;

import com.fqm.framework.common.core.util.RandomUtil;

public class RandomTest {

    public static void main(String[] args) {
        String random = RandomStringUtils.random(16, true, true);
        System.out.println(random);
        // DB F357TzQLHyH9pwnl
        // User oejRU7jeSQEfpn2R
    }

}
