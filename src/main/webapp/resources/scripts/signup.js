$(document).ready(function() {
$("#submitBtn").bind('click', submitSignUpForm);

$('#zipCode').mask("99999?-9999");

//	"use strict";
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
});

function submitSignUpForm() {
	if ($("#firstname").val().trim() == '' || $("#middlename").val().trim() == ''
		|| $("#lastname").val().trim() == '' || $("#birthDate").val().trim() == ''
		|| $("#gender").val().trim() == '' || $("#email").val().trim() == ''
		|| $("#zipCode").val().trim() == '' || $("#password").val().trim() == ''
			) {
		$("#msg").html("All fields are required.");		
		return;
	} else if ($("#password").val().trim() != $("#confirmPw").val().trim()) {
		$("#msg").html("Password and confirm password fields do not match.");
		return;
	} 
	else {
		$("#signUpForm").submit();
	}
}