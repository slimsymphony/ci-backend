$(document).ready(function() {
    var link = $("#userInfo");
    var menu = $('#userMenu');

    $(document).ajaxStart(function() {
        NProgress.start();
    });

    $(document).ajaxComplete(function() {
        NProgress.done();
    });

    $(document).mouseup(function(e)
    {
        if (link.has(e.target).length === 0 && menu.has(e.target).length === 0) {
            menu.stop().hide();
        }
    });

    $(document).keyup(function(e) {
        if (e.keyCode === 27) {
            menu.stop().hide();
        }
    });

    link.click(function(e) {
        e.preventDefault();
        $('.ui-tooltip').hide();
        menu.stop().slideToggle(300);
    });

    // Highlight the current page
    var str = location.href.toLowerCase();
    var activeSet = false;
    $('#mainmenu a').each(function() {
        $(this).removeClass('active');
        if (str.indexOf(this.href.toLowerCase()) > -1) {
            $(this).addClass('active');
            activeSet = true;
        }
    });

    // Set up the parent mainmenu item as active if not found
    if (activeSet === false) {
        if (str.indexOf('/admin/') > -1) {
            $('#mainmenu a.mainmenuAdmin').addClass('active');
        } else if (str.indexOf('/page/project') > -1 || str.indexOf('/page/verification') > -1 ||
                str.indexOf('/page/build') > -1) {
            $('#mainmenu a.mainmenuProjects').addClass('active');
        } else if (str.indexOf('/page/metrics/') > -1) {
            $('#mainmenu a.mainmenuMetrics').addClass('active');
        } else if (str.indexOf('/page/info/') > -1 || str.indexOf('/page/help') > -1 ||
                str.indexOf('/page/faq/') > -1) {
            $('#mainmenu a.mainmenuInfo').addClass('active');
        } else {
            $('#mainmenu a.mainmenuHome').addClass('active');
        }
    }

    var $sidebar = $("#mainmenu"),
            $window = $(window),
            offset = $sidebar.offset(),
            topPadding = 15;

    $window.scroll(function() {
        if ($window.scrollTop() > offset.top) {
            var t = window.setTimeout(function() {
                var margin = $window.scrollTop() - offset.top + topPadding;
                if (margin < 0)
                    margin = 0;
                $sidebar.stop().animate({
                    marginTop: margin
                });
            }, 50);
        } else {
            $sidebar.stop().animate({
                marginTop: 0
            });
        }
    });

    // Search input handling
    $('#searchInput input').keydown(function(e) {
        e.stopPropagation();
    });

    // Full screen mode
    var full_screen_state;
    function fullScreen(new_state) {
        if (new_state === full_screen_state) {
            return;
        }
        full_screen_state = new_state;

        if (new_state === true) {
            $('#mainmenuContainer').hide();
            $('#top').hide();
            $('#content, #contentTitle').css('margin-left', '0px');
            $('#contentmenu').css('top', '0px')
        } else {
            $('#mainmenuContainer').show();
            $('#top').show();
            $('#content, #contentTitle').css('margin-left', '100px');
            $('#contentmenu').css('top', '54px');
        }
    }

    var fullScreenTimer;
    if ((window.fullScreen) || (window.innerWidth === screen.width && window.innerHeight === screen.height)) {
        fullScreen(true);
    }

    $(window).resize(function() {
        clearTimeout(fullScreenTimer);
        if ((window.fullScreen) || (window.innerWidth === screen.width && window.innerHeight === screen.height)) {
            fullScreenTimer = setTimeout(function() {
                fullScreen(true);
            }, 100);
        } else {
            fullScreenTimer = setTimeout(function() {
                fullScreen(false);
            }, 100);
        }
    });

    // Enable link transitions
    $("body").removeClass("preload");
});
