$(document).ready(function() {
	var token = $("input[name='_csrf']").val();
	var header = "X-CSRF-TOKEN";
	$(document).ajaxSend(function(e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
	$(document).ajaxStart($.blockUI).ajaxStop($.unblockUI);
	
	if ($("#password").length) {
		var options = {};
		options.common = {
		        onLoad: function () {
		            $('#messages').text('Start typing password');
		        },
		        onKeyUp: function (evt, data) {
		            $("#length-help-text").text("Current length: " + $(evt.target).val().length + " and score: " + data.score);
		        },
		        onScore: function (options, word, totalScoreCalculated) {
		            // If my word meets a specific scenario, I want the min score to
		            // be the level 1 score, for example.
		            if (word.length === 20 && totalScoreCalculated < options.ui.scores[3]) {
		                // Score doesn't meet the score[1]. So we will return the min
		                // numbers of points to get that score instead.
		                return options.ui.score[1]
		            }
		            // Fall back to the score that was calculated by the rules engine.
		            // Must pass back the score to set the total score variable.
		            return totalScoreCalculated;
		        }
		    };
		    $("#password").pwstrength(options);	
	}
	
});

function submitRequest() {
	$("#msg").html('');
	if (!($("#pwRecoveryForm")[0].checkValidity())) {
		return false;
	} else {
		$.post("retrievePassword.htm", {
			email : $("#email").val(),
		},
				function(response) {
					$("#email").val('');
					$("#msg").html("<p class='alert alert-success text-success'>If a valid email was entered, a password reset email will be sent shortly.</p>");
					return false;
				});
	}
}

function submitResetPasswordForm() {
	$('#msg').html('');	
	if ($('#password').val() == $('#confirmPw').val()) {		
		$.post("resetPassword.htm", {
			password : $('#password').val(),
		},
		function(response) {
			if (response == "success") {
				$('#resetPasswordForm').submit();
			}
			else {
				$('#msg').html('<div class="alert alert-danger">'+response+'</div>');
			}
			return false;
		});	
	}
	else {
		$('#msg').html('<div class="alert alert-danger">Passwords must match</div>');
	}
}