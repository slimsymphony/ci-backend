var debounceTimer = null;
var prevClientWidth;

function initWindowParams(){
    prevClientWidth = document.body.clientWidth;
}

function updateMetricsUnitsReal(){
    if (document.body.clientWidth != prevClientWidth){
        updateChartSize();
        prevClientWidth = document.body.clientWidth;
    }
}

function updateMetricsUnits(){
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout("updateMetricsUnitsReal()", 500);
}                   
                    
function minFormatExt() {
    this.cfg.axes.yaxis.tickOptions = {
        formatString : '%.1f'
    };
}

function cntFormatExt() {
    this.cfg.axes.yaxis.tickOptions = {
        formatString : '%.1f'
    };
}

function percentageFormatExt() {
    this.cfg.axes.yaxis.tickOptions = {
        formatString : '%.1f' + '%'
    };
}

function intFormatExt() {
    this.cfg.axes.yaxis.tickOptions = {
        formatString : '%.0f'
    };
}

