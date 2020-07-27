
package com.qaprosoft.jenkins.jobdsl.factory.pipeline

import groovy.transform.InheritConstructors

@InheritConstructors
class DockerBuildJobFactory extends PipelineFactory {

	def host
	def repo
	def organization
	def branch
	def scmUrl

	public DockerBuildJobFactory(folder, pipelineScript, jobName, host, organization, repo, branch, scmUrl) {
		this.folder = folder
		this.pipelineScript = pipelineScript
		this.name = jobName
		this.host = host
		this.organization = organization
		this.repo = repo
		this.branch = branch
		this.scmUrl = scmUrl
	}

	def create() {
		logger.info("DockerBuildJobFactory->create")

		def pipelineJob = super.create()

		pipelineJob.with {

			parameters {
				configure stringParam('release_version', '', 'SemVer-compliant upcoming release or RC version (e.g. 1.13.1 or 1.13.1.RC1)')
				configure stringParam('branch', 'develop', 'Branch containing sources for component build')
				configure stringParam('dockerfile', 'Dockerfile', 'Name of the dockerfile')
				//configure stringParam('PATH', '.', 'Relative path to your dockerfile')
                configure addHiddenParameter('repo', '', repo)
                configure addHiddenParameter('GITHUB_HOST', '', host)
                configure addHiddenParameter('GITHUB_ORGANIZATION', '', organization)
			}
		}

		return pipelineJob
	}

}