function stripeResponseHandler(status, response) {
  var $form = $('#payment-form');
  if (response.error) {
    // Show the errors on the form
    $form.find('.payment-errors').text(response.error.message);
    $form.find('.submit').prop('disabled', false); // Re-enable submission
  } else {
    var token = response.id;
    $form.append($('<input type="hidden" name="stripeToken">').val(token));
    $form.get(0).submit();
  }
};

$(function() {
  $('#number').payment('formatCardNumber');
  $('#amount').payment('restrictNumeric');
  $('#exp').payment('formatCardExpiry');
  $('#cvc').payment('formatCardCVC');

  var $form = $('#payment-form');
  $form.submit(function(event) {
    // Disable the submit button to prevent repeated clicks:
    $form.find('.submit').prop('disabled', true);
    // Request a token from Stripe:
    Stripe.card.createToken($form, stripeResponseHandler);

    // Prevent the form from being submitted:
    return false;
  });
});

