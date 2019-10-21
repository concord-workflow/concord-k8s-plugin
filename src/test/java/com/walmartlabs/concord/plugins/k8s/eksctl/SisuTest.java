package com.walmartlabs.concord.plugins.k8s.eksctl;

import com.walmartlabs.TestSupport;
import com.walmartlabs.concord.sdk.DependencyManager;
import com.walmartlabs.concord.sdk.LockService;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.mockito.Mockito.mock;

public class SisuTest extends InjectedTest {

    @Inject
    private EksCtlTask eskCtlTask;

    @Test
    public void validateConstruction() {

    }


    @Named
    public static class DependencyManagerProvider implements Provider<DependencyManager> {

        @Override
        public DependencyManager get() {
            return new TestSupport.OKHttpDownloadManager("sisu");
        }
    }

    @Named
    public static class LockServiceProvider implements Provider<LockService> {

        @Override
        public LockService get() {
            return mock(LockService.class);
        }
    }
}
