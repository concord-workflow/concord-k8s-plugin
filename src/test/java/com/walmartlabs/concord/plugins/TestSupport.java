package com.walmartlabs.concord.plugins;

import org.junit.Before;

import java.io.File;

public class TestSupport {

    protected String basedir;

    @Before
    public void setUp() {
        basedir = new File("").getAbsolutePath();
    }

    protected File file(String name) {
        return new File(new File(basedir, "src/test"), name);
    }
}
