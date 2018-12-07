$(document).ready(function() {
	$("#usersDropDown").bind("change", showUserDetails);
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function(e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
	//$("#submitConsentForm").bind("click",submitConsentForm);
});

function showUserDetails() {
	var id = $("#usersDropDown").val();
	$(".userDetails").hide();
	$("#userDetails" + id).show();
}

function updateUserDetails(id, rolesLength) {
	var roles = new Array();
	while (rolesLength > 0) {
		if ($("#" + rolesLength + id).is(":checked")) {
			roles.push(rolesLength.toString());
		}
		rolesLength--;
	}
	$.post("updateUserDetails.htm", {
		id : id,
		firstName : $("#firstName" + id).val(),
		lastName : $("#lastName" + id).val(),
		email : $("#email" + id).val(),
		roles : JSON.stringify(roles),
	}, function(response) {
		if (response == "success") {

		} else {
			return false;
		}
	});

}