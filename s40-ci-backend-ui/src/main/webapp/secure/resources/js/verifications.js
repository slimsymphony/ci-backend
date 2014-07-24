function toggleOptionalButtons(elem) {

    var checked = elem.find('input').prop("checked");
    if (checked === true) {
        elem.nextAll('.verificationExtraButton').css('display', 'inline-block');
    }
    else {
        elem.nextAll('.verificationExtraButton').hide();
    }
}

function updateVerificationConfs() {
    $('.verificationConfSelect').each(function(i) {
        toggleOptionalButtons($(this));
    });
}

$(document).ready(function() {

    $('.verificationConfSelect').live('change', function(e) {
        e.preventDefault();
        toggleOptionalButtons($(this));
    });

    $('.verificationConfSelectable').live('click', function(e) {
        e.preventDefault();
        var input = $(this).next('input');
        var val = input.val();

        if (val === 'MANDATORY') {
            $(this).removeClass('verificationConfMandatory').addClass('verificationConfOptional');
            input.val('OPTIONAL');
        }
        else if (val === 'OPTIONAL') {
            $(this).removeClass('verificationConfOptional').addClass('verificationConfMandatory');
            input.val('MANDATORY');
        }
    });

    // Initial setup
    updateVerificationConfs();
});

