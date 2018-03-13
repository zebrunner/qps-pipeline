package com.qaprosoft.jenkins.repository.pipeline

import groovy.json.JsonSlurper;

//TODO: get and transfer to children jobs parent_ci_url and parent_ci_build if any
def runCron() {
    node('master') {
        git branch: '${branch}', url: '${GITHUB_SSH_URL}/${project}', changelog: false, poll: false, shallow: true

        def listPipelines = []

	def jenkinsFile = ".jenkinsfile.json"
	if (!fileExists("${WORKSPACE}/${jenkinsFile}")) {
		println "Skip repository scan as no .jenkinsfile.json discovered! Project: ${project}"
	}

	def suiteFilter = "src/test/resources/**"
	Object subProjects = parseJSON(jenkinsFile).sub_projects
	subProjects.each {
		if (it.name.equals(sub_project)) {
			suiteFilter = it.suite_filter
		}
	}

	//TODO: try to avoid hardcoding fodler name
        def folderName = "Automation"

	def subProjectFilter = sub_project
	if (sub_project.equals(".")) {
		subProjectFilter = "**"
	}


	def files = findFiles(glob: subProjectFilter + "/" + suiteFilter + "/**")
        if(files.length > 0) {
            println "Number of Test Suites to Scan Through: " + files.length
            for (int i = 0; i < files.length; i++) {
                parsePipeline(readFile(files[i].path), listPipelines)
            }

            println "Finished Dynamic Mapping: " + listPipelines
            def sortedPipeline = sortPipelineList(listPipelines)
            println "Finished Dynamic Mapping Sorted Order: " + sortedPipeline

            this.executeStages(folderName, sortedPipeline)
        } else {
            println "No Test Suites Found to Scan..."
        }
    }
}

def parsePipeline(String file, List listPipelines) {
    def jobName = retrieveRawValues(file, "jenkinsJobName")
    println "jobName: ${jobName}"
    def pipelineInfo = retrieveRawValues(file, "jenkinsRegressionPipeline")
    def priorityNum = retrieveRawValues(file, "jenkinsJobExecutionOrder")
    def executionMode = retrieveRawValues(file, "jenkinsJobExecutionMode")

    def envs = retrieveRawValues(file, "jenkinsPipelineEnvironments")
//    println "supported envs: ${envs}"

    def browsers = retrieveRawValues(file, "jenkinsPipelineBrowsers")
    println "browsers: " + browsers

    def desiredBrowser = params["browser"]
    if (desiredBrowser != null) {
        println "desiredBrowser: " + desiredBrowser
    }


    def envName = params["env"]
    println "current env: ${envName}"

    if (!pipelineInfo.contains("null")) {
        for (def pipeName : getInfo(pipelineInfo).split(",")) {
		for (def envInfo : getInfo(envs).split(",")) {
		    if (!envInfo.equals(envName)) {
			//launch test only if current suite support cron regression execution for current env
			continue;
		    }
                    for (def browser : getInfo(browsers).split(",")) {
                        if ("${JOB_BASE_NAME}".equalsIgnoreCase(pipeName)) {
                            echo "Pipeline job: " + pipeName
                            def emailList = getInfo(retrieveRawValues(file, "jenkinsEmail"))
                            if (!"${email_list}".isEmpty()) {
                                emailList = "${email_list}"
                            }
                            println "emailList: " + emailList

                            println "browser: " + browser
                            if (desiredBrowser != null) {
                                if (!desiredBrowser.equals(browser)) {
                                    println "Skip launch for non desiredBrowser! browser: ${browser}; desiredBrowser: ${desiredBrowser}"
                                    continue;
                                }
                            }

                            def pipelineMap = [:]

                            pipelineMap.put("browser", browser)
                            pipelineMap.put("name", pipeName)
                            pipelineMap.put("branch", "${branch}")
                            pipelineMap.put("retry_count", "${retry_count}")
                            pipelineMap.put("thread_count", "${thread_count}")
                            pipelineMap.put("jobName", getInfo(jobName))
                            pipelineMap.put("environment", envName)
                            pipelineMap.put("priority", getInfo(priorityNum))
                            pipelineMap.put("executionMode", getInfo(executionMode))
                            pipelineMap.put("emailList", emailList.replace(", ", ","))

                            listPipelines.add(pipelineMap)
                        }
                    }
		}
        }
    }
}

def retrieveRawValues(String file, String parameter) {
    def splitFile = ""
    if (file.length() > 0) {
        splitFile = file.split("<")
    }

    return splitFile.find { it.toString().contains(parameter)}.toString()
}

def getInfo(String line) {
    def valueStr = "value=\""
    def beginValue = line.indexOf(valueStr)
    def endValue = line.indexOf("\"/>")

    if (beginValue > 0 && endValue > 0) {
        retrievedValues = line.substring(beginValue + valueStr.toString().size(), endValue)
        return retrievedValues
    }

    //handle case when there is a space at the closing tag
    endValue = line.indexOf("\" />")

    if (beginValue > 0 && endValue > 0) {
        retrievedValues = line.substring(beginValue + valueStr.toString().size(), endValue)
        return retrievedValues
    }

    return ""
}

def buildOutStage(String folderName, Map entry) {
    stage(String.format("Stage: %s Environment: %s Browser: %s", entry.get("jobName"), entry.get("environment"), entry.get("browser"))) {
        println "Dynamic Stage Created For: " + entry.get("jobName")
        println "Checking EmailList: " + entry.get("emailList")

	if (!entry.get("browser").isEmpty()) {
       	    build job: folderName + "/" + entry.get("jobName"),
                propagate: true,
                    parameters: [
                        string(name: 'branch', value: entry.get("branch")),
                        string(name: 'env', value: entry.get("environment")),
                        string(name: 'browser', value: entry.get("browser")),
                        string(name: 'email_list', value: entry.get("emailList")),
                        string(name: 'thread_count', value: entry.get("thread_count")),
                        string(name: 'retry_count', value: entry.get("retry_count")),
                    ], 
                wait: false
	} else {
       	    build job: folderName + "/" + entry.get("jobName"),
                propagate: true,
                    parameters: [
                        string(name: 'branch', value: entry.get("branch")),
                        string(name: 'env', value: entry.get("environment")),
                        string(name: 'email_list', value: entry.get("emailList")),
                        string(name: 'thread_count', value: entry.get("thread_count")),
                        string(name: 'retry_count', value: entry.get("retry_count")),
                    ], 
                wait: false
	}
    }
}

def buildOutStages(String folderName, Map entry) {
    return {
        buildOutStage(folderName, entry)
    }
}

def executeStages(String folderName, List sortedPipeline) {
    def mappedStages = [:]

    boolean parallelMode = true

    for (Map entry : sortedPipeline) {
	echo "entry.get(priority): " + entry.get("priority")
        if (!entry.get("priority").toString().contains("null") && entry.get("priority").toString().length() > 0 && parallelMode) {
            parallelMode = false
        }
	echo "parallelMode: " + parallelMode
//        if (parallelMode) {
//            mappedStages[String.format("Stage: %s Environment: %s Browser: %s", entry.get("jobName"), entry.get("environment"), entry.get("browser"))] = buildOutStages(folderName, entry)
//        } else {
//            executeSingleStage(folderName, entry)
//        }
//
    }
//    if (parallelMode) {
//        parallel mappedStages
//    }
}

def executeSingleStage(folderName, entry) {
    if (!entry.get("executionMode").toString().contains("continue")) {
        buildOutStage(folderName, entry)
    } else {
        catchError {
            buildOutStage(folderName, entry)
        }
    }
}

@NonCPS
def sortPipelineList(List pipelineList) {
    return pipelineList.sort { map1, map2 -> !map1.priority ? !map2.priority ? 0 : 1 : !map2.priority ? -1 : map1.priority.toInteger() <=> map2.priority.toInteger() }
}


@NonCPS
Object parseJSON(String path) {
	def inputFile = new File("${WORKSPACE}/${path}")
	def content = new groovy.json.JsonSlurperClassic().parseFile(inputFile, 'UTF-8')
	return content
}