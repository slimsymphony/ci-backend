
$(document).ready(function() {

    $("#loading").show();

    //Handle top operation bar
    var s40CIProducts = document.getElementById("s40_ci_products");
    var s40CIBranchs = document.getElementById("s40_ci_branches");
    var s40CICovTypes = document.getElementById("s40_ci_covtypes");
    var s40CITestTypes = document.getElementById("s40_ci_testtypes");

    initOperations();

    //$( "#full_screen", "#operations" ).button();
    $( "#full_screen", "#operations" ).click(function() {
        url_to_open = window.location.href;
        window.open(url_to_open, '_blank');
        return false;
    })
    //$( "#go", "#operations" ).button();
    $( "#go", "#operations" ).click(function() { 

        $("#loading").show();

        var dataurl = s40_ci_metrics_server_prefix + "/s40-ci-metrics-restful-service/metrics/test_component_coverage?productName=" + s40CIProducts.options[s40CIProducts.selectedIndex].value 
        + "&branchName=" + s40CIBranchs.options[s40CIBranchs.selectedIndex].value + "&covType=" + s40CICovTypes.options[s40CICovTypes.selectedIndex].value
        + "&testType=" + s40CITestTypes.options[s40CITestTypes.selectedIndex].value
        + "&callback=?";

        $.ajax({
            url: dataurl,
            method: 'GET',
            dataType: 'json',
            success: onDataReceived
        });

    });


    /*
                jQuery.getJSON("http://localhost:8080/s40-ci-metrics-restful-service/metrics/jsonp_test?symbol=IBM&callback=?", 
                function(data) {
                alert(data);
                alert("Symbol: " + data.symbol + ", Price: " + data.price);
                });
                         */

    //var dataurl = "test_data/test_data_3.json";
    var dataurl = s40_ci_metrics_server_prefix + "/s40-ci-metrics-restful-service/metrics/test_component_coverage?testType=isattcn&covType=ctc&callback=?";


    function showTooltip(x, y, contents) {
        $('<div id="tooltip">' + contents + '</div>').css( {
            position: 'absolute',
            display: 'none',
            top: y + 5,
            left: x + 5,
            border: '1px solid #fdd',
            padding: '2px',
            'background-color': '#fee',
            opacity: 0.80
        }).appendTo("body").fadeIn(200);
    }


    //var previousPoint = null;
    $("#placeholder").bind("plothover", function (event, pos, item) {
        $("#x").text(pos.x.toFixed(2));
        $("#y").text(pos.y.toFixed(2));

        //if ($("#enableTooltip:checked").length > 0) {
        if (item) {
            //if (previousPoint != item.dataIndex) {
            previousPoint = item.dataIndex;

            $("#tooltip").remove();
            var x = item.datapoint[0].toFixed(2),
            y = item.datapoint[1].toFixed(2) - item.datapoint[2].toFixed(2),
            yAccum = item.datapoint[1].toFixed(2);
            //y = item.datapoint[1].toFixed(2);

            if (item.datapoint[2].toFixed(2) != 0){

                var percentage = Math.round(y / yAccum * 100);

                showTooltip(item.pageX, item.pageY,
                    item.series.label + " = " + y + "(" + percentage + "%)");                        
            }else{

                showTooltip(item.pageX, item.pageY,
                    item.series.label + " = " + y);                        
            }

        //}
        }
        else {
            $("#tooltip").remove();
            previousPoint = null;            
        }
    //}
    });



    function onDataReceived(data) {

        var xTickWidth = (data.ticks.length <= 10 ? 10 : data.ticks.length);

        var chartWidth = xTickWidth * 90;


        $("#placeholder").width(chartWidth);

        var multipleBarFlag = true, bars = true, lines = false, steps = false;


        var options = {
            series: {
                multipleBars: multipleBarFlag,
                lines: {
                    show: lines, 
                    fill: true, 
                    steps: steps
                },
                bars: {
                    show: bars, 
                    barWidth: 0.3
                }
            },
            grid: {
                hoverable: true, 
                clickable: true
            },
            xaxis: {
                ticks: data.ticks,
                max: xTickWidth
            }
        };

        var placeholder = $("#placeholder");

        $.plot(placeholder, [], options);

        $.plot(placeholder, data.multidata, options);

        //document.getElementById("s40_ci_extrainfo").innerHTML = data.extrainfo;

        //alert("done");

        $("#loading").hide();


    }

    $.ajax({
        url: dataurl,
        method: 'GET',
        dataType: 'json',
        success: onDataReceived
    });

});