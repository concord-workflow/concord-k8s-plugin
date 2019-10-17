package com.walmartlabs;

import org.junit.Before;

import java.io.File;

public abstract class TestSupport {

    private String basedir;

    @Before
    public void setUp() {
        basedir = new File("").getAbsolutePath();
    }

    protected File target(String name) {

        File target = new File(basedir, "target/" + name);
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        return target;
    }

}
