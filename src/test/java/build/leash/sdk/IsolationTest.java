package build.leash.sdk;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the SDK remains isolated from web framework dependencies.
 * Only java.net.http, com.google.gson, and the standard library should be used.
 */
class IsolationTest {

    /** Banned import prefixes — no source file in src/main/ should reference these. */
    private static final List<String> BANNED_IMPORT_PREFIXES = List.of(
            "org.springframework",
            "javax.servlet",
            "jakarta.servlet",
            "io.micronaut",
            "io.quarkus",
            "io.javalin"
    );

    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.]+)");

    /**
     * Scan all .java files under src/main/ and verify none of them import
     * web framework packages.
     */
    @Test
    void noWebFrameworkImportsInSourceFiles() throws IOException {
        Path srcMain = projectRoot().resolve("src/main");
        assertTrue(Files.isDirectory(srcMain), "src/main directory must exist");

        List<String> violations = new ArrayList<>();

        try (Stream<Path> javaFiles = Files.walk(srcMain)
                .filter(p -> p.toString().endsWith(".java"))) {
            javaFiles.forEach(file -> {
                try {
                    List<String> lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size(); i++) {
                        var matcher = IMPORT_PATTERN.matcher(lines.get(i));
                        if (matcher.find()) {
                            String imported = matcher.group(2);
                            for (String banned : BANNED_IMPORT_PREFIXES) {
                                if (imported.startsWith(banned)) {
                                    violations.add(String.format(
                                            "%s:%d imports banned package %s",
                                            srcMain.relativize(file), i + 1, banned));
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read " + file, e);
                }
            });
        }

        assertTrue(violations.isEmpty(),
                "Source files must not import web framework packages:\n  " +
                        String.join("\n  ", violations));
    }

    /**
     * Parse pom.xml and verify that no non-test dependency pulls in a web framework.
     */
    @Test
    void noWebFrameworkDependenciesInPom() throws Exception {
        Path pomPath = projectRoot().resolve("pom.xml");
        assertTrue(Files.exists(pomPath), "pom.xml must exist at project root");

        List<String> bannedGroupPrefixes = List.of(
                "org.springframework",
                "javax.servlet",
                "jakarta.servlet",
                "io.micronaut",
                "io.quarkus",
                "io.javalin"
        );

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Disable external entities for safety
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(pomPath.toFile());

        NodeList dependencies = doc.getElementsByTagName("dependency");
        List<String> violations = new ArrayList<>();

        for (int i = 0; i < dependencies.getLength(); i++) {
            Element dep = (Element) dependencies.item(i);
            String groupId = getChildText(dep, "groupId");
            String artifactId = getChildText(dep, "artifactId");
            String scope = getChildText(dep, "scope");

            // Skip test-scoped dependencies
            if ("test".equals(scope)) {
                continue;
            }

            for (String banned : bannedGroupPrefixes) {
                if (groupId != null && groupId.startsWith(banned)) {
                    violations.add(String.format(
                            "%s:%s has banned groupId prefix %s",
                            groupId, artifactId, banned));
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "pom.xml must not contain non-test web framework dependencies:\n  " +
                        String.join("\n  ", violations));
    }

    // ---- helpers ----

    private static Path projectRoot() {
        // Walk up from the test class output directory to find the project root (where pom.xml lives)
        Path candidate = Paths.get(System.getProperty("user.dir"));
        while (candidate != null) {
            if (Files.exists(candidate.resolve("pom.xml"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        // Fallback — assume CWD is the project root
        return Paths.get(System.getProperty("user.dir"));
    }

    private static String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }
}
