package pl.edu.agh.gem.internal.job

enum class ReportJobState {
    STARTING,
    FETCHING_ACTIVITIES,
    FETCHING_BALANCES,
    FETCHING_SETTLEMENTS,
    FORMAT_SELLECTION,
    GENERATING_XSLM_REPORT,
    UPLOAD_REPORT,
    SAVING,
    ERROR,
}
