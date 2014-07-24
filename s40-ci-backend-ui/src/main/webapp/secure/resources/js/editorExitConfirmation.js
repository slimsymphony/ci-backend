var warn_on_unload="";
$(':input').one('change', function() 
{
    warn_on_unload = "Leaving this page will cause any unsaved data to be lost!";
});

$('button').live('click', function(e) {
    warn_on_unload="";
});

window.onbeforeunload = function() {
    if(warn_on_unload !== '') {
        return warn_on_unload;
    }
}
