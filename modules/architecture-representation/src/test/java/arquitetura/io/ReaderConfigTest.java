package arquitetura.io;

import java.io.FileNotFoundException;

import org.junit.Test;

import junit.framework.Assert;

public class ReaderConfigTest {

	@Test
	public void testDefaultReaderConfiLoad() throws FileNotFoundException {
		ReaderConfig.load();
		Assert.assertNull(ReaderConfig.getNewPathToConfigurationFile());
		Assert.assertNotNull(ReaderConfig.getDirExportTarget());
		Assert.assertNotNull(ReaderConfig.getDirTarget());
		Assert.assertNotNull(ReaderConfig.getPathToProfileConcerns());
		Assert.assertNotNull(ReaderConfig.getPathToProfilePatterns());
		Assert.assertNotNull(ReaderConfig.getPathToProfileRelationships());
		Assert.assertNotNull(ReaderConfig.getPathToProfileSMarty());
		Assert.assertNotNull(ReaderConfig.getPathToTemplateModelsDirectory());
	}

}
