package version;

import insilico.core.version.InsilicoInfo;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the version the library reports at runtime is the one declared in the pom.
 *
 * <p>The value travels through the Maven resource filtering of {@code src/main/resources-filtered}: if that
 * configuration is dropped or the generated resource stops being packaged, this test fails instead of the library
 * silently reporting an empty or stale version in every report it writes.</p>
 */
class InsilicoInfoTest {

    @Test
    void reportsTheVersionDeclaredInThePom() throws Exception {
        assertThat(new InsilicoInfo().getVersion()).isEqualTo(versionFromPom());
    }

    @Test
    void reportsTheNameDeclaredInThePom() throws Exception {
        assertThat(new InsilicoInfo().getName()).isEqualTo(nameFromPom());
    }

    /**
     * @return the content of /project/version in the pom of this project
     */
    private static String versionFromPom() throws Exception {
        return childOfProject("version");
    }

    /**
     * @return the content of /project/name in the pom of this project
     */
    private static String nameFromPom() throws Exception {
        return childOfProject("name");
    }

    /**
     * Reads a direct child of the root element of the pom. Surefire runs with the project directory as the working
     * directory, so the file is simply pom.xml; only direct children are considered, otherwise the first match
     * would be a version or a name coming from the dependencies.
     *
     * @param tag the name of the element to read
     * @return its text content
     */
    private static String childOfProject(String tag) throws Exception {
        Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("pom.xml"));
        Element project = pom.getDocumentElement();
        for (Node node = project.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE && tag.equals(node.getNodeName())) {
                return node.getTextContent().trim();
            }
        }
        throw new AssertionError("No <" + tag + "> element in the pom");
    }
}
