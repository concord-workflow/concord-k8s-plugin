package com.walmartlabs.concord.plugins;

import com.walmartlabs.concord.sdk.MockContext;
import org.codehaus.plexus.util.StringUtils;

import java.util.Map;

public class InterpolatingMockContext extends MockContext {

    private Map<String,Object> delegate;

    public InterpolatingMockContext(Map<String, Object> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public Object interpolate(Object v) {
        return StringUtils.interpolate(v.toString(), delegate);
    }
}
