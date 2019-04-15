package com.qaprosoft.jenkins.jobdsl.factory.pipeline

@Grab('org.testng:testng:6.8.8')

import static com.qaprosoft.jenkins.Utils.*
import org.testng.xml.XmlSuite
import com.qaprosoft.jenkins.jobdsl.selenium.grid.ProxyInfo
import groovy.transform.InheritConstructors

@InheritConstructors
public class RegisterRepositoryJobFactory extends PipelineFactory {

	def organization
	def pipelineLibrary
	def runnerClass

	public RegisterRepositoryJobFactory(folder, name, jobDesc, organization, pipelineLibrary, runnerClass) {
		this.folder = folder
		this.name = name
		this.description = jobDesc
		this.organization = organization
		this.pipelineLibrary = pipelineLibrary
		this.runnerClass = runnerClass
	}

	def create() {
		logger.info("RegisterRepositoryJobFactory->create")
		def pipelineJob = super.create()
		pipelineJob.with {
			parameters {
				configure stringParam('organization', organization, 'GitHub organization')
				configure stringParam('repo', '', 'GitHub repository for scanning')
				configure stringParam('branch', '', 'It is highly recommended to use master branch for each scan operation')
				configure addHiddenParameter('scmURL', "GitHub repository https URL with token (read permissions only is enough)", '' )
				configure stringParam('pipelineLibrary', pipelineLibrary, 'Groovy JobDSL/Pipeline library, for example: https://github.com/qaprosoft/qps-pipeline/releases')
				configure stringParam('runnerClass', runnerClass, '')
			}
		}
		return pipelineJob
	}

	String getPipelineScript() {
		return "@Library(\'${pipelineLibrary}\')\nimport com.qaprosoft.jenkins.pipeline.Repository;\nnew Repository(this).register()"
	}

}