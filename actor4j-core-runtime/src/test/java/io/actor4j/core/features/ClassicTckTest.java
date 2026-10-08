package io.actor4j.core.features;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(AllFeaturesTest.class)
public class ClassicTckTest {
	static {
		System.setProperty("actor4j.runtime", "classic");
	}
}
