import com.vaadin.hilla.Endpoint;

/**
 * A test class.
 */
@Endpoint(value = "CustomEndpointName")
public class CustomEndpoint {

    /**
     * Foo endpoint.
     *
     * @param bar
     */
    public void foo(String bar) {
    }

    /**
     * Baz endpoint.
     *
     * @param baz
     * @return
     */
    public String bar(String baz) {
        return baz;
    }

}
