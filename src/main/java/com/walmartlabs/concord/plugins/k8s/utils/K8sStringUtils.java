package com.walmartlabs.concord.plugins.k8s.utils;

import ca.vanzyl.concord.plugins.TaskSupport;
import com.github.javafaker.Faker;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Comparator.reverseOrder;

@Named("k8sStringUtils")
public class K8sStringUtils extends TaskSupport {

    private final Faker faker = new Faker();

    public String name() {
        return faker.animal().name();
    }

    public static void main(String[] args) throws Exception {
        K8sStringUtils u = new K8sStringUtils();
        System.out.println(u.name());
    }
}
