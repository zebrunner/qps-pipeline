package com.qaprosoft.jenkins.pipeline.runner

import com.qaprosoft.jenkins.BaseObject
import java.nio.file.Paths

import com.qaprosoft.jenkins.pipeline.Configuration
import static com.qaprosoft.jenkins.Utils.*
import static com.qaprosoft.jenkins.pipeline.Executor.*

public abstract class AbstractRunner extends BaseObject {
    // organization folder name of the current job/runner
    protected String organization = ""
    protected String buildNameTemplate = "${BUILD_NUMBER}"
    protected String buildName = ""
    protected final String BUILD_NAME_SEPARATOR = "|"

    public String getBuildName() {
        return this.buildName
    }

    private void setBuildName(String buildNameTemplate) {
        if (!isParamEmpty(buildNameTemplate)) {
            this.buildNameTemplate = replaceMultipleSymbolsToOne(buildNameTemplate, BUILD_NAME_SEPARATOR)
            //set exact values instead of name-strings
            for (item in buildNameTemplate.split(BUILD_NAME_SEPARATOR)) {
                this.buildName += Configuration.get("${item}") + "|"
            }
            //delete last character "|"
            this.buildName = this.buildName.substring(0, this.buildName.length() - 1)
        }
    }

    public AbstractRunner(context) {
        super(context)
        initOrganization()
        setBuildName("BUILD_NUMBER|branch")
    }

    //Methods
    abstract public void build()

    //Events
    abstract public void onPush()

    abstract public void onPullRequest()

    /*
     * Execute custom pipeline/jobdsl steps from Jenkinsfile
     */

    protected void jenkinsFileScan() {
        def isCustomPipelineEnabled = getToken(Configuration.CREDS_CUSTOM_PIPELINE)

        if (!isCustomPipelineEnabled) {
            logger.warn("Custom pipeline execution is not enabled")
            return
        }

        if (!context.fileExists('Jenkinsfile')) {
            logger.warn("Jenkinsfile doesn't exist in your repository")
            return
        }

        context.stage('Jenkinsfile Stage') {
            context.script {
                context.jobDsl targets: 'Jenkinsfile'
            }
        }
    }

    /*
     * Get organization folder value
     * @return organization String
     */

    protected def getOrgFolder() {
        return this.organization
    }

    /*
     * Determined current organization folder by job name
     */

    @NonCPS
    protected void initOrganization() {
        String jobName = context.env.getEnvironment().get("JOB_NAME")
        //Configuration.get(Configuration.Parameter.JOB_NAME)
        int nameCount = Paths.get(jobName).getNameCount()

        def orgFolderName = ""
        if (nameCount == 1 && (jobName.contains("qtest-updater") || jobName.contains("testrail-updater"))) {
            // testrail-updater - i.e. stage
            orgFolderName = ""
        } else if (nameCount == 2 && (jobName.contains("qtest-updater") || jobName.contains("testrail-updater"))) {
            // stage/testrail-updater - i.e. stage
            orgFolderName = Paths.get(jobName).getName(0).toString()
        } else if (nameCount == 2) {
            // carina-demo/API_Demo_Test - i.e. empty orgFolderName
            orgFolderName = ""
        } else if (nameCount == 3) { //TODO: need to test use-case with views!
            // qaprosoft/carina-demo/API_Demo_Test - i.e. orgFolderName=qaprosoft
            orgFolderName = Paths.get(jobName).getName(0).toString()
        } else {
            throw new RuntimeException("Invalid job organization structure: '${jobName}'!")
        }

        this.organization = orgFolderName
    }

    /*
     * Get token key from Jenkins credentials based on organization
     *
     * @param tokenName Jenkins credentials id
     * @return token value
     */

    protected def getToken(tokenName) {
        def tokenValue = ""

        if (!isParamEmpty(this.organization)) {
            tokenName = "${this.organization}" + "-" + tokenName
        }

        if (getCredentials(tokenName)) {
            context.withCredentials([context.usernamePassword(credentialsId: tokenName, usernameVariable: 'KEY', passwordVariable: 'VALUE')]) {
                tokenValue = context.env.VALUE
            }
        }
        logger.debug("tokenName: ${tokenName}; tokenValue: ${tokenValue}")
        return tokenValue
    }

    /*
     * Get username and password from Jenkins credentials based on organization
     * 
     * @param tokenName Jenkins credentials id
     * @return name and password
     */

    protected def getUserCreds(tokenName) {
        def name = ""
        def password = ""

        if (!isParamEmpty(this.organization)) {
            tokenName = "${this.organization}" + "-" + tokenName
        }

        if (getCredentials(tokenName)) {
            context.withCredentials([context.usernamePassword(credentialsId: tokenName, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                name = context.env.USERNAME
                password = context.env.PASSWORD
            }
        }
        logger.debug("tokenName: ${tokenName}; name: ${name}; password: ${password}")
        return [name, password]
    }

    /*
     * set DslClasspath to support custom JobDSL logic
     */

    protected void setDslClasspath(additionalClasspath) {
        factoryRunner.setDslClasspath(additionalClasspath)
    }


}
