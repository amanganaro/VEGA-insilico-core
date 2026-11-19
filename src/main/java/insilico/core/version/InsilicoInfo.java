package insilico.core.version;

import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Name and version of the InSilico Core library.
 *
 * <p>The values are not maintained by hand: they are read from {@code core-version.properties}, a resource that
 * Maven generates at build time by filtering {@code src/main/resources-filtered}, so the version reported here is
 * always the version declared in the pom the jar was built from. The properties file is read once and cached.</p>
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoInfo {

    /** Resource holding the filtered values, looked up next to this class. */
    private static final String PROPERTIES_SOURCE = "core-version.properties";

    /** Values read from the resource, loaded on the first use and shared by every instance. */
    private static Properties properties;

    private final String name;
    private final String version;

    /**
     * Constructor. Reads the name and the version of the library.
     *
     * @throws InitFailureException if the generated resource is missing or unreadable, which means the library was
     *                              not built through its Maven build
     */
    public InsilicoInfo() throws InitFailureException {
        Properties values = load();
        this.name = values.getProperty("name", "");
        this.version = values.getProperty("version", "");
    }

    /**
     * Reads the generated resource, once.
     *
     * @return the properties it contains
     * @throws InitFailureException if the resource cannot be found or read
     */
    private static synchronized Properties load() throws InitFailureException {
        if (properties != null) {
            return properties;
        }
        try (InputStream stream = InsilicoInfo.class.getResourceAsStream(PROPERTIES_SOURCE)) {
            if (stream == null) {
                throw new IOException(PROPERTIES_SOURCE + " not found on the classpath");
            }
            Properties values = new Properties();
            values.load(stream);
            properties = values;
            return properties;
        } catch (IOException e) {
            throw new InitFailureException(
                    String.format(StringSelectorCore.getString("isinfo_read_core_fail"), e.getMessage()));
        }
    }

    /**
     * @return the name of the library, as declared in the pom
     */
    public String getName() {
        return name;
    }

    /**
     * Provides the library version as a single string, exactly as declared in the pom (for example {@code 1.3.21}).
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }
}
