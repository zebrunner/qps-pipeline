package com.qaprosoft.jenkins

@Grab('org.testng:testng:6.8.8')
import org.testng.xml.Parser
import org.testng.xml.XmlSuite

class Utils {

    static def printStackTrace(Exception e) {
        def stringStacktrace = ""
        e.getStackTrace().each { traceLine ->
            stringStacktrace = stringStacktrace + "\tat " + traceLine + "\n"
        }
        return "${e.getClass().getName()}: ${e.getMessage()}\n" + stringStacktrace
    }

    static def encodeToBase64(stringValue) {
        return stringValue.bytes.encodeBase64().toString()
    }

    static XmlSuite parseSuite(String path) {
        def xmlFile = new Parser(path)
        xmlFile.setLoadClasses(false)
        List<XmlSuite> suiteXml = xmlFile.parseToList()
        XmlSuite currentSuite = suiteXml.get(0)
        return currentSuite
    }

    static boolean isParamEmpty(value) {
        if (value == null) {
            return true
        }  else {
            return value.toString().isEmpty() || value.toString().equalsIgnoreCase("NULL")
        }
    }

    static def getSuiteParameter(defaultValue, parameterName, currentSuite){
        def value = defaultValue
		if (!isParamEmpty(currentSuite.getParameter(parameterName))) {
			value = currentSuite.getParameter(parameterName)
		}
		
        return value
    }

    static def replaceTrailingSlash(value) {
        return value.replaceAll(".\$","")
    }

    static def replaceStartSlash(String value) {
        if (value[0].equals("/")) {
            value = value.replaceFirst("/", "")
        }
        return value
    }

    static def replaceSlashes(String value, String str) {
        if (value.contains("/")) {
            value = value.replaceAll("/", str)
        }
        return value
    }

    static boolean getBooleanParameterValue(parameter, currentSuite){
        return !isParamEmpty(currentSuite.getParameter(parameter)) && currentSuite.getParameter(parameter).toBoolean()
    }

    static def getZafiraServiceUrl(orgName) {
        def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
            com.cloudbees.plugins.credentials.Credentials.class,
            Jenkins.instance,
            null,
            null
        )
        for (creds in jenkinsCredentials) {
            if(creds.id == orgName + "-zafira_access_token"){
                return creds.password
            }
        }
    }

    static def getZafiraAccessToken(orgName) {
        def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                com.cloudbees.plugins.credentials.Credentials.class,
                Jenkins.instance,
                null,
                null
        )
        for (creds in jenkinsCredentials) {
            if (creds.id == orgName + "-zafira_service_url") {
                return creds.password
            }
        }
    }

    static def getZafiraServiceParameters(orgName) {
        def resultList
        def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                com.cloudbees.plugins.credentials.Credentials.class,
                Jenkins.instance,
                null,
                null
        )
        for (creds in jenkinsCredentials) {
            if (creds.id == orgName + "-zafira_service_url") {
                resultList.add(creds.password)
            }
            if(creds.id == orgName + "-zafira_access_token"){
                resultList.add(creds.password)
            }
        }
        return resultList
    }
}