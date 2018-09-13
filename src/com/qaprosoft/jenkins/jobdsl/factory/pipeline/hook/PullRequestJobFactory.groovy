package com.qaprosoft.jenkins.jobdsl.factory.pipeline.hook

import com.qaprosoft.jenkins.pipeline.Configuration
import groovy.transform.InheritConstructors
import com.qaprosoft.jenkins.jobdsl.factory.pipeline.PipelineFactory

@InheritConstructors
public class PullRequestJobFactory extends PipelineFactory {

	def project
    def scmProjectUrl

	public PullRequestJobFactory(folder, pipelineScript, jobName, jobDesc, project, scmProjectUrl) {
		this.folder = folder
		this.pipelineScript = pipelineScript
		this.name = jobName
		this.description = jobDesc
		this.project = project
        this.scmProjectUrl = scmProjectUrl
	}

	def create() {
		_dslFactory.println "DUMP: " + _dslFactory.binding.variables
		def pipelineJob = super.create()

		pipelineJob.with {
			properties {
				//TODO: calculate valid https project URL. OBLIGATORY without .git at the end 
				githubProjectUrl(scmProjectUrl)
				//TODO: test with removed "cron('H/5 * * * *')"
				pipelineTriggers {
					triggers {
						ghprbTrigger {
							gitHubAuthId(getGitHubAuthId(folder))
							adminlist('')
							useGitHubHooks(true)
							triggerPhrase('')
							autoCloseFailedPullRequests(false)
							skipBuildPhrase('.*\\[skip\\W+ci\\].*')
							displayBuildErrorsOnDownstreamBuilds(false)
							cron('H/5 * * * *')
							whitelist('')
							orgslist(getOrganization())
							blackListLabels('')
							whiteListLabels('')
							allowMembersOfWhitelistedOrgsAsAdmin(false)
							permitAll(true)
							buildDescTemplate('')
							blackListCommitAuthor('')
							includedRegions('')
							excludedRegions('')
							onlyTriggerPhrase(false)
							commentFilePath('')
							msgSuccess('')
							msgFailure('')
							commitStatusContext('')
						}
					}
				}
			}

		}
		return pipelineJob
	}

	protected def getOrganization() {
		return 'qaprosoft'
	}

	protected def getGitHubAuthId(project) {
		return "https://api.github.com : ${project}-token"
	}
}