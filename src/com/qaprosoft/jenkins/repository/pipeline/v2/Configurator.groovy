package com.qaprosoft.jenkins.repository.pipeline.v2

import com.cloudbees.groovy.cps.NonCPS
import java.util.List

public class Configurator {

    private def context

    public Configurator(context) {
        this.context = context
        this.loadContext()
    }

    //list of job vars/params as a map
    protected static Map params = [:]
    protected static Map vars = [:]

    public enum Parameter {

        //vars
        ADMIN_EMAILS("ADMIN_EMAILS", "qps-auto@qaprosoft.com"),
        CARINA_CORE_VERSION("CARINA_CORE_VERSION", "5.2.4.106"),
        CORE_LOG_LEVEL("CORE_LOG_LEVEL", "INFO"),
        GITHUB_HOST("GITHUB_HOST", "REPLACE_ME"),
        GITHUB_API_URL("GITHUB_API_URL", "REPLACE_ME"),
        GITHUB_ORGANIZATION("GITHUB_ORGANIZATION", "REPLACE_ME"),
        GITHUB_HTML_URL("GITHUB_HTML_URL", "REPLACE_ME"),
        GITHUB_OAUTH_TOKEN("GITHUB_OAUTH_TOKEN", "REPLACE_ME"),
        GITHUB_SSH_URL("GITHUB_SSH_URL", "REPLACE_ME"),
        JACOCO_BUCKET("JACOCO_BUCKET", "jacoco.qaprosoft.com"),
        JACOCO_ENABLE("JACOCO_ENABLE", "false"),
        JOB_MAX_RUN_TIME("JOB_MAX_RUN_TIME", "60"),
        QPS_PIPELINE_GIT_BRANCH("QPS_PIPELINE_GIT_BRANCH", "REPLACE_ME"),
        QPS_PIPELINE_GIT_URL("QPS_PIPELINE_GIT_URL", "REPLACE_ME"),
        SELENIUM_PROTOCOL("SELENIUM_PROTOCOL", "REPLACE_ME"),
        SELENIUM_HOST("SELENIUM_HOST", "REPLACE_ME"),
        SELENIUM_PORT("SELENIUM_PORT", "REPLACE_ME"),
        SELENIUM_URL("SELENIUM_URL", "REPLACE_ME"),
        ZAFIRA_ACCESS_TOKEN("ZAFIRA_ACCESS_TOKEN", "REPLACE_ME"),
        ZAFIRA_SERVICE_URL("ZAFIRA_SERVICE_URL", "http://zafira:8080/zafira-ws"),
        JOB_URL("JOB_URL", ""),
        JOB_NAME("JOB_NAME", ""),
        JOB_BASE_NAME("JOB_BASE_NAME", ""),
        BUILD_NUMBER("BUILD_NUMBER", ""),
        NGINX_HOST("NGINX_HOST", "localhost"),
        NGINX_PORT("NGINX_PORT", "80"),
        NGINXT_PROTOCOL("NGINXT_PROTOCOL", "http"),

        private final String key;
        private final String value;

        Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @NonCPS
        public String getKey() {
            return key
        }

        @NonCPS
        public String getValue() {
            return value;
        }

    }

    @NonCPS
    public static String get(Parameter param) {
        return vars.get(param.getKey())
    }

    public static void set(Parameter param, String value) {
        return vars.put(param.getKey(), value)
    }

    @NonCPS
    public static String get(String paramName) {
        return params.get(paramName)
    }

    public static void set(String paramName, String value) {
        return params.put(paramName, value)
    }

    @NonCPS
    public void loadContext() {
        //1. load all Parameter key/values to vars
        def enumValues  = Parameter.values()
        for (enumValue in enumValues) {
                vars.put(enumValue.getKey(), enumValue.getValue())
        }
        for (var in vars) {
            context.println(var)
        }
        //2. load all string keys/values from env
        def envVars = context.env.getEnvironment()
        for (var in envVars) {
            if (var.value != null) {
                vars.put(var.key, var.value)
            }
        }
        for (var in vars) {
            context.println(var)
        }
        //3. load all string keys/values from params
        def jobParams = context.currentBuild.rawBuild.getAction(ParametersAction)
        for (param in jobParams) {
            if (param.value != null) {
                params.put(param.name, param.value)
            }
        }
        for (param in params) {
            context.println(param)
        }
        //4. TODO: investigate how private pipeline can override those values
    }

}