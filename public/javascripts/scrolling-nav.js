//jQuery to collapse the navbar on scroll
$(window).scroll(function() {
    if ($(".navbar").offset().top > 50) {
        $(".navbar-fixed-top").addClass("top-nav-collapse");
    } else {
        $(".navbar-fixed-top").removeClass("top-nav-collapse");
    }
});

//jQuery for page scrolling feature - requires jQuery Easing plugin
$(function() {
    $('a.page-scroll').bind('click', function(event) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: $($anchor.attr('href')).offset().top
        }, 1500, 'easeInOutExpo');
        event.preventDefault();
    });
});

// check recaptcha via ajax call
function reCaptchaCallback() {
  // $('#g-recaptcha-response').val()
  if (!grecaptcha) return;
  var dbt = $('#directBankTransfer');
  var resp = grecaptcha.getResponse();
  $.ajax({
    type: "POST",
    url: "/api/recaptcha/verify",
    data: { response: resp },
    success: function(data) {
      dbt.html("<p><b>IBAN</b>: " + data.iban + "</p>" +
        "<p><b>BIC</b>: "+ data.bic + "</p>")
    },
    error: function(err) {
      dbt.html("Something went wrong! Please " +
        "<a href=\"/#contact\">contact me</a> directly..")
    },
    dataType: "json"
  });
}
