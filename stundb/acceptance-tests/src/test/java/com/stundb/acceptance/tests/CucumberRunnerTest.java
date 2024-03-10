package com.stundb.acceptance.tests;

import static io.cucumber.core.options.Constants.*;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, json:target/cucumber/report.json, html:target/cucumber/report.html")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.stundb.acceptance.tests.steps")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
public class CucumberRunnerTest {}
