$(document).ready(function() {
    $('.fullscreenToggle').click(function(e) {
        e.preventDefault();
        var div = $(this).parent();
        var link = $(this);
        var chart = div.find('.jqplot-target');
        if(chart.length <= 0) {
            chart = div.find('.ui-timeline-container');
        }

        if(chart.length > 0) {
            chart.hide();
            
            if(link.hasClass('fullscreenExit')) {
                div.removeClass('overlay');
                updateChartSize();
                link.removeClass('fullscreenExit').addClass('fullscreenBtn');
            }
            else {
                div.addClass('overlay');
                updateChartSize();
                link.removeClass('fullscreenBtn').addClass('fullscreenExit');
            }
        }
    });
});