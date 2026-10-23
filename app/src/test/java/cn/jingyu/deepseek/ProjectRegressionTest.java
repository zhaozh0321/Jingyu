package cn.jingyu.deepseek;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;

public final class ProjectRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    @Test public void coreRequestsAndWriters() throws Exception {
        File repo = new File(System.getProperty("jingyu.repoRoot"));
        HostQualityTest.main(new String[] {
            temporary.newFolder("core").getAbsolutePath(),
            new File(repo, "test/fixtures/original-signer.der").getAbsolutePath()
        });
    }
    @Test public void publishingAndRequests() throws Exception {
        File repo = new File(System.getProperty("jingyu.repoRoot"));
        PublishingQualityTest.main(new String[] {
            temporary.newFolder("documents").getAbsolutePath(),
            new File(repo, "test/fixtures/original-signer.der").getAbsolutePath()
        });
    }
    @Test public void outputAndMemoryRouting() throws Exception { ExperienceQualityTest.main(new String[0]); }
    @Test public void streamingAndAnchoring() { StreamQualityTest.main(new String[0]); }
}
