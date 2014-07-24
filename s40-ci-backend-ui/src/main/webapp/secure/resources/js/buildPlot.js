var buildPlotRedraw = true;

function redrawBuildPlot() {
    buildPlotRedraw = true;
    drawBuildPlot();
}

function findBottommostChild()
{
    var bottom = null;
    var cur = 0;
    $('.buildPlotItem').each(function(i) {
        var b = $(this).position().top;
        if(b >= cur) {
            cur = b;
            bottom = $(this);
        }
    });
    return bottom;
}

function numberOfParents(build) {
    var parents = 0;
    $(".buildPlotItem").each(function(i) {
        var children_input = $(this).find('input[name*=children]');
        var children = children_input.val();
        var arr = children.split(',');
        var len = arr.length;
        for(var i=0; i < len; i++) {
            if(build.attr('id') == 'build_' + arr[i]) {
                parents++;
            }
        }
    });
    return parents;
}

function drawBuildPlot()
{   
    // buildPlot not in DOM tree
    if($('#buildPlot').length == 0) {
        return;
    }
    
    // plotView is not shown
    if($('#viewSelect\\:plotView').css('display') == 'none') {
        return;
    } 
    
    // Already drawn
    if(buildPlotRedraw == false) {
        return;
    }
    
    jsPlumb.detachEveryConnection();

    $('.buildPlotItem').first().css('left', (Math.floor($('#buildPlot').width() / 2) - 60) + 'px');
    var top_margin = $('.buildPlotItem').first().height() + 120;
    var left_margin = 50;
    var links = [];
    var num_links = 0;
    var plot_right = $('#buildPlot').position().left + $('#buildPlot').outerWidth() - 20;

    $(".buildPlotItem").each(function(i) {

        var children_input = $(this).find('input[name*=children]');
        var children = children_input.val();

        if(children != "0") {
            var parent = $(this);
            var arr = children.split(',');
            var len = arr.length;
            left_margin = parent.position().left;
            if(len > 1) {
                left_margin -= Math.round((len/2) * 230);
            }

            if(left_margin < 10) {
                left_margin = 10;
            }

            // Adjust div locations
            for(var i=0; i < len; i++) {
                var child = $("#build_" + arr[i]);

                links[num_links] = [];
                links[num_links]["parent"] = parent.attr('id');
                links[num_links]["child"] = child.attr('id');
                
                var parents = numberOfParents(child);
                if(parent.position().left == left_margin && parents > 1 && 
                    left_margin > 250) {
                    left_margin -= 230;
                }
                
                if((parent.position().top + 300) < child.position().top &&
                    parents > 2) {
                    top_margin -= 350;
                }

                child.css('top', top_margin + 'px');
                child.css('left', left_margin + 'px');
                
                if(((child.position().left + child.outerWidth()) > plot_right) ||
                    (child.position().left + child.outerWidth() > $(window).width())){
                    left_margin = 10;
                    child.css('left', left_margin + 'px');
                    top_margin += 100;
                    child.css('top', top_margin + 'px');
                }

                // Check for overlapping
                $(".buildPlotItem").each(function(j) {
                    if(child.attr('id') == $(this).attr('id')) return true;
                    if(!child.position() || !$(this).position()) return true;
                   
                    var right = $(this).position().left + $(this).outerWidth();
                    var bottom = $(this).position().top + $(this).outerHeight();
                   
                    if(child.position().left < right && child.position().left > $(this).position().left) {
                        child.css('left', (right + 5) + 'px');
                    }
                   
                    if(child.position().top < bottom && child.position().top > $(this).position().top) {
                        child.css('top', (bottom + 20) + 'px');
                        top_margin = bottom + 20;
                    }
                   
                    if((child.position().left + child.outerWidth()) > plot_right) {
                        left_margin = 10;
                        child.css('left', left_margin + 'px');
                        top_margin += 100;
                        child.css('top', top_margin + 'px');
                    }
                   
                });
               
                num_links++;

                left_margin += 230;
            }

            top_margin += 120;
        }       
    });

    // Create actual links
    if(num_links > 0) {
        for(var i=0; i < num_links; i++) {
            
            if($('#' + links[i]["parent"]).length == 0 ||
                $('#' + links[i]["child"]).length == 0) continue;
            
            var conn = jsPlumb.connect({
                source: links[i]["parent"],
                target: links[i]["child"],
                endpointsOnTop: false
            });                
        }
    }

    var last_child = findBottommostChild();
    if(last_child) {
        var height = last_child.position().top + last_child.outerHeight() + 10;
        $("#buildPlot").css('height', height + 'px');
    }

    buildPlotRedraw = false;
}

$(document).ready(function() {
   
    jsPlumb.setRenderMode(jsPlumb.SVG);
        
    jsPlumb.importDefaults({
        PaintStyle : {
            lineWidth: 4,
            strokeStyle: '#deea18'
        },
        Connector: "StateMachine",
        ConnectorZIndex: 98,
        HoverPaintStyle : {
            strokeStyle:"#42a62c", 
            lineWidth: 4
        },
        Endpoint : "Blank",
        EndpointStyle : {
            fillStyle: "white"
        },
        ConnectionOverlays : [
        [ "Arrow", {
            location:1
        } ]
        ],
        Anchors : [ "BottomCenter", "TopCenter" ]
    });
    
    $(window).resize(function() {
        buildPlotRedraw = true;
        drawBuildPlot();
    });
    
    $('a[href$="plotView"]').live('click', drawBuildPlot);
    
    $('.buildPlotItem').mouseenter(function(e)
    {
        jsPlumb.select({
            source: $(this).attr('id')
        }).setPaintStyle({
            strokeStyle: "#42a62c"
        }).each(function(conn) {
            $(conn.canvas).css('z-index', 102);
            conn.target.css('border', '1px solid #42a62c');
        });
        
        jsPlumb.select({
            target: $(this).attr('id')
        }).setPaintStyle({
            strokeStyle: '#b1ea18'
        }).each(function(conn) {
            $(conn.canvas).css('z-index', 102);
        });
    }).mouseleave(function(e) 
    {
        jsPlumb.select({
            source: $(this).attr('id')
        }).setPaintStyle({
            strokeStyle: '#deea18'
        }).each(function(conn) {
            $(conn.canvas).css('z-index', 99);
            conn.target.css('border', '1px solid black');
        });
        
        jsPlumb.select({
            target: $(this).attr('id')
        }).setPaintStyle({
            strokeStyle: '#deea18'
        }).each(function(conn) {
            $(conn.canvas).css('z-index', 99);
        });
    });
});