package ca.vanzyl.concord.plugins;

import com.walmartlabs.concord.plugins.ConcordTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfiguratorTest extends ConcordTestSupport
{

    @Test
    public void validateConfiguration() throws Exception {
        Configurator configurator = new Configurator();
        ObjectToConfigure object = new ObjectToConfigure();
        configurator.configure(object, mapBuilder()
                .put("string0", "string0Value")
                .put("boolean0", "true")
                .put("char0", 'a')
                .put("byte0", "100")
                .put("short0", "30000")
                .put("int0", "100000000")
                .put("long0", "100000000000000")
                .put("float0", "1.456")
                .put("double0", "1.456789012345678")
                .build());

        assertEquals("string0Value", object.string0);
        assertEquals('a', object.char0);
        assertEquals(true, object.boolean0);
        assertEquals(100, object.byte0);
        assertEquals(30000, object.short0);
        assertEquals(100000000, object.int0);
        assertEquals(100000000000000L, object.long0);
        assertEquals(1.456f, object.float0, 0);
        assertEquals(1.456789012345678, object.double0, 0);
    }

    class ObjectToConfigure {

        public String string0;
        public char char0;
        public boolean boolean0;
        public byte byte0;
        public short short0;
        public int int0;
        public long long0;
        public float float0;
        public double double0;
    }
}
