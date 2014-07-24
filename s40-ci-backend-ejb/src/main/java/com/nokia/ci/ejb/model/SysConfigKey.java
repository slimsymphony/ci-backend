/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

/**
 *
 * @author jajuutin
 */
public enum SysConfigKey {

    ALLOW_LOGIN("Log in is allowed for non-admin users"),
    ALLOW_NEXT_USERS("Log in is allowed for users with NEE account"),
    BASE_URL("URL of CI UI"),
    BUILDGROUP_REAPER_TIMER_TIMEOUT("Timeout for buildgroup reaper timer service"),
    BUILDGROUP_REAPER_JOBSTART_TIMEOUT("Timeout when not starting build groups will be updated to NOT BUILD"),
    BUILDGROUP_REAPER_TIMEOUT("Timeout when started but not finished builds will be updated to NOT BUILD"),
    CPU_USAGE_STACK_SAMPLING_SIZE("Size of Cpu usage history, which is used to calculate average cpu usage"),
    CPU_USAGE_THRESHOLD_IN_PERCENT("Lower boundary of cpu usage from where system is unstable"),
    DETACH_RESERVED_SLAVE_TIMER_INTERVAL("Interval of detaching expired slave reservations (ms)"),
    ENABLE_SEARCH_AND_INDEXING("Searching and indexing are enabled"),
    EMAIL_NEWLINE_CHAR("Character(s) used as newline character in e-mail message"),
    EMAIL_SENDER_ADDRESS("Sender address used in e-mail"),
    GERRIT_LISTENER_CHECK_TIMEOUT("Timeout for checking if we still need to listen this Gerrit (in seconds)"),
    GERRIT_LISTENER_LOOP_INTERVAL("Interval when checking Gerrit events (ms)"),
    GERRIT_LISTENER_TIMER_TIMEOUT("Timeout for Gerrit Listener Manager timer"),
    GERRIT_TIMER_TIMEOUT("Timeout for Gerrit Timer used for polling changes from Gerrit"),
    GERRIT_NORESPONSE_TIMEOUT("Timeout to disconnect if nothing is received (minutes)"),
    GERRIT_MESSAGE_NEWLINE_CHAR("Newline character(s) used in Gerrit messages"),
    GERRIT_PROJECT_ACCESS_PORT("Port used in checking gerrit project access rights"),
    GERRIT_PROJECT_ACCESS_USERNAME("Username used in checking gerrit project access rights"),
    GERRIT_SERVER_ALIVE_COUNT_MAX("Max number of server alive messages to be sent before disconnect"),
    GERRIT_SOCKET_TIMEOUT("Socket session timeout (ms)"),
    GIT_ASYNC_REPO_UPDATE_TIMEOUT("Timeout for git repository update methods"),
    GIT_CLONE_LOAD("Integer to describe load caused by git clone. Relative to GIT_MAX_LOAD"),
    GIT_FETCH_FAILURE_THRESHOLD("Maximum number of unsuccessful 'git fetch' for each branch"),
    GIT_FETCH_LOAD("Integer to describe load caused by git fetch. Relative to GIT_MAX_LOAD"),
    GIT_MAX_LOAD("Integer to describe max git load"),
    GIT_ROOT_DIRECTORY("Root directory for all local Git repositories"),
    GIT_TIMER_TIMEOUT("Timeout for Git Timer"),
    HTTP_CLIENT_CONNECTION_TIMEOUT("Responce timeout for HTTP client connections"),
    HTTP_CLIENT_SOCKET_TIMEOUT("Socket timeout for HTTP client connections"),
    INDEX_OPTIMIZATION_CRON("Cron expression for search index optimization (default: 0 0 23 * * ?), uses server's timezone"),
    MAX_RESERVED_SLAVE_INSTANCES_PER_MASTER("Load Balancer limit for max reserved slaves per master"),
    MAXIMUM_OWNER_EMAILS_PARAM_LENGTH("Maximum owner emails parameter length"),
    MIN_PROJECT_ACCESS_CACHE_READ_INTERVAL("Minimum time that is waited between two project access cache checks from same http session bean"),
    MEM_USAGE_STACK_SAMPLING_SIZE("Size of Mem usage history, which is used to calculate average mem usage"),
    MEM_USAGE_THRESHOLD_IN_PERCENT("Lower boundary of mem usage from where system is unstable"),
    METRICS_SERVER_URL("URL of Metrics server"),
    SLAVE_RESERVATION_TIMEOUT("Slave reservation expiration timeout (ms)"),
    SYSTEM_LOAD_STACK_SAMPLING_SIZE("Size of System load history, which is used to calculate average system load"),
    SYSTEM_LOAD_THRESHOLD_IN_PERCENT("Lower boundary of system load from where system is unstable"),
    SYSUSER_REAPER_CRON("Cron expression for sys user reaper (default: 0 0 0 * * ?), uses server's timezone"),
    TAS_PRODUCT_SERVER_URL("Url for tas product server"),
    UNSUCCESSFUL_LOGIN_THRESHOLD_FOR_USER_SIGNIN("Maximum number of unsuccessful 'signing in' for one user within UNSUCCESSFUL_LOGIN_MONITOR_PERIOD period"),
    UNSUCCESSFUL_LOGIN_MONITOR_PERIOD("Unsuccessful 'signing in' is counted within this period of time (minutes)"),
    USAGE_SAMPLING_TIMEOUT("Timeout for SystemUsage timer"),
    USER_FILE_UPLOAD_PATH("Server end full path to store user uploaded files.");
    private String desc;

    /**
     * @param desc
     */
    private SysConfigKey(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
