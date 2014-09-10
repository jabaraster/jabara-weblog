/**
 * 
 */
package info.jabara.weblog;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import jabara.general.MapUtil;

import java.util.Map;

import org.junit.Test;

/**
 * @author jabaraster
 */
public class TracerTest {

    /**
     * 
     */
    @SuppressWarnings({ "static-method", "nls" })
    @Test
    public void _toLtsv() {
        final Map<String, Object> m = MapUtil.m( //
                "host", "127.0.0.1" //
                , "message", "ho\tge::piyo" //
        );
        assertThat(Tracer.toLtsv(m), is("message:hogepiyo\thost:127.0.0.1"));
    }

}
