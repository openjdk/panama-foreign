Introduction:

The goal of this project is to cover testing of the Vector API.

Setup:

1. Run bash gen-template.sh from this directory. This will generate tests files from the templates to each vector shape and type.

2. Run jtreg. Example:
jtreg -ea -esa -avm -va -nr *VectorTests.java

3. Alternatively to jtreg, you can run tests directly with the provided scripts.
  - Run bash build-tests.sh to build the tests.
 
	4. Download the TestNG plugin. You can find jars following this link:
	    https://dl.bintray.com/testng-team/testng-eclipse-release/zipped/6.13.0.201712040650/org.testng.eclipse.updatesite.zip
	
	5. Unzip the plugin, make sure it has the "plugins" directory. It should contain the two following files:
	    - com.beust.jcommander_1.72.0.jar
	    - org.testng_6.13.1.r201712040515.jar
	
	6. Export the unzipped folder path to the TESTNG_PLUGIN environment variable.
 
  7. Run bash run-tests.sh to run the tests with your own installation of TestNG.
