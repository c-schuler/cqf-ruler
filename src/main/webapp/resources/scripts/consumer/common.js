var timeout;

function calcOffset() {
    var serverTime = getCookie('serverTime');
    serverTime = serverTime==null ? null : Math.abs(serverTime);
    var clientTimeOffset = (new Date()).getTime() - serverTime;
    setCookie('clientTimeOffset', clientTimeOffset);
}

function checkSession() {
    var sessionExpiry = Math.abs(getCookie('sessionExpiry'));
    var timeOffset = Math.abs(getCookie('clientTimeOffset'));
    var localTime = (new Date()).getTime();
    if (localTime - timeOffset - (15000) > (sessionExpiry)) {
    	navigateTimeout();
    }  	
    else if (localTime - timeOffset + ((5 * 60 * 1000) + 15000) > (sessionExpiry)) {
    	timeout = setTimeout(function(){ navigateTimeout(); }, (5 * 60 * 1000));
    	showAlertDialog("Your session will expire in 5 minutes. Click OK to extend your session.",extendSession, this);
    } else {
        setTimeout('checkSession()', 10000);
    }
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
    	
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
function setCookie(cname, cvalue) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + 1 + ";path=/";
}

function extendSession(status, caller) {
	if (status) {
		$.post("resetSession.htm", null, function(response) {
			clearTimeout(timeout);
		});
    	setTimeout('checkSession()', 10000);
	}	
}

function navigateTimeout() {
	document.getElementById('logoutType').value = "timeout";
	document.getElementById('logout').submit();
}

$(function() {
	$("#alertDialog").dialog({
		title : "",
		autoOpen : false,
		modal : true,
		resizable : true,
		open: function(event, ui) { $(".ui-dialog-titlebar-close").hide(); },
		width : "auto"
	});
});

function showAlertDialog(msg, callback, caller) {
    $("#alertDialog").dialog({
        buttons: {
            "OK": function () {
            	$(this).dialog("close");
            	callback(true,caller);
            }
        }
    });
	$("#alertDialog").dialog("open");
	$("#alertDialog").html("<h4 class=''>"+msg+"</h4>");
}

function escapeChars(str) {
	str = str.replace(/["]/g, '\\"');
	str = str.replace(/[']/g, "&#39;");
	return str;
}

/** White Label Advanced Entity Search **/

$(function() {
	$("#advancedEntitySearch").dialog({
		title : "Advanced Entity Search",
		autoOpen : false,
		modal : true,
		resizable : true,
		open: function(event, ui) { $(".ui-dialog-titlebar-close").show(); },
		width : "auto"
	});
});

function advancedEntitySearch() {
	$("#advancedEntitySearch").empty();
	$("#advancedEntitySearch").append('<form id="advancedEntitySearchForm" action="javascript:searchAdvancedEntity();">');
	//$("#advancedEntitySearch").append('<p>Enter your criteria below and click search to return results.<br>You may then use the search criteria to further filter your results.</p>');
	$("#advancedEntitySearch").append('<label for="entityType">Entity Type'
	+ '<select class="form-control" id="advancedEntitySearchType" name="entityType" onchange="setEntitySearchCriteria()">'
	+ '<option value="PROVIDER" selected>Provider</option>'
	+ '<option value="ORGANIZATION">Organization</option>'
	+ '</select></label>');
	$("#advancedEntitySearch").append('<div id="entitySearchCriteria"></div>');
	// <label for="entityType">Entity Type<input type="text" class="form-control" required="required" id="entityType" placeholder="" /></label>');
	//$("#advancedEntitySearch").append('<button id="advancedEntitySearchBtn" type="submit"><span class="glyphicon glyphicon-search"></span> Search</button> ');
	// <div class="row equal"> </div>
	$("#advancedEntitySearch").append('<div id="entitySearchExtraCriteria"></div>');
	$("#advancedEntitySearch").append('<div id="entitySearchResults"></div>');
	$("#advancedEntitySearch").append('</form>');
	$("#advancedEntitySearch").dialog("open");
	setEntitySearchCriteria();
	$("#advancedEntitySearch").dialog("option", "position", {my: "center", at: "center", of: window});
}

function setEntitySearchCriteria() {
	$("#entitySearchCriteria").empty();
	$("#entitySearchExtraCriteria").empty();
	$("#entitySearchResults").empty();	
	//$("#entitySearchCriteria").append('<hr>');
	if ('PROVIDER' == $('#advancedEntitySearchType').val()) {
		$("#entitySearchCriteria").append('<label for="entityFirstName">First Name<input type="text" class="form-control" onkeyup="filterAdvancedSearchResults()" required="required" id="entityFirstName" placeholder="" /></label> ');
		$("#entitySearchCriteria").append('<label for="entityLastName">Last Name<input type="text" class="form-control" onkeyup="filterAdvancedSearchResults()" required="required" id="entityLastName" placeholder="" /></label> ');
	}
	else if ('ORGANIZATION' == $('#advancedEntitySearchType').val()) {
		$("#entitySearchCriteria").append('<label for="entityOrganizationName">Organization Name<input type="text" class="form-control" onkeyup="filterAdvancedSearchResults()" required="required" id="entityOrganizationName" placeholder="" /></label> ');
	}
	$("#entitySearchCriteria").append('<button id="advancedEntitySearchBtn" type="submit" class="btn btn-sm btn-primary" onclick="javascript:searchAdvancedEntity();"><span class="glyphicon glyphicon-search"></span> Search</button>');
}

function searchAdvancedEntity() {
	if ('PROVIDER' == $('#advancedEntitySearchType').val()) {
		var name = $("#entityFirstName").val().trim();
		if ($("#entityLastName").val().trim() != '') name += ' ' + $("#entityLastName").val().trim();
		$.getJSON("getPractitionersByName.htm",
				{
					searchName : name,
				},
				function(j) {
					displayAdvancedSearchResults(j);
				})
				  .fail(function() {
					  $("#entitySearchExtraCriteria").empty();
					  $("#entitySearchResults").empty();
					  $("#entitySearchResults").append("No Results Found.");
				  })
				.always(function() {
					$("#advancedEntitySearch").dialog({height: 400});
					$("#advancedEntitySearch").dialog("option", "position", {my: "center", at: "center", of: window});
				});
	}
	else if ('ORGANIZATION' == $('#advancedEntitySearchType').val()) {
		$.getJSON("scdGetPractitioners.htm",
				{
					searchName : $("#entityOrganizationName").val().trim(),
					searchType : "ORGANIZATION"
				},
				function(j) {
					displayAdvancedSearchResults(j);
				})
				  .fail(function() {
					  $("#entitySearchExtraCriteria").empty();
					  $("#entitySearchResults").empty();
					  $("#entitySearchResults").append("No Results Found.");
				  })
				.always(function() {
					$("#advancedEntitySearch").dialog({height: 400});
					$("#advancedEntitySearch").dialog("option", "position", {my: "center", at: "center", of: window});
				});
	}
	
	
}

function displayAdvancedSearchResults(j) {
	$("#entitySearchExtraCriteria").empty();
	$("#entitySearchResults").empty();
	var results = "";
	results += '<table id="advancedEntitySearchResultsTable" class="table table-striped"><thead>';
	if ('PROVIDER' == $('#advancedEntitySearchType').val()) {
		var orgSelect = '<label for="advancedEntitySearchOrgFilter">Organziation<select class="form-control" id="advancedEntitySearchOrgFilter" name="advancedEntitySearchOrgFilter" onchange="filterAdvancedSearchResults()"><option value=""></option>';
		results += '<tr><th class="col-md-4">Doctor\'s Name</th><th class="col-md-4">Organizations</th><th class="col-md-2">&nbsp;</th></tr>';
		results += '</thead><tbody id="advancedEntitySearchResultsTableBody">';
		if (j.length > 0) {
			for (var i = 0; i < j.length; i++) {
				results += '<tr>';
				//$("#entitySearchResults").append(j);
				results += '<td><span class="firstName">'+j[i].firstName +'</span> <span class="lastName">'+ j[i].lastName+'</td>';
				var orgs = "";
				$.each(j[i].organizations, function(k, org) {
					
					orgSelect += '<option value="'+org.name+'">'+org.name+'</option>'
						
					if (k > 0) orgs +=', ';
					orgs += org.name;
				});				
				results += '<td><span class="orgs">'+orgs+'</span></td>';
				results += '<td><button id="advancedEntitySearchSelect" class="btn btn-xs btn-primary" type="button" onclick="javascript:advancedEntitySearchSelect(this);">Select</button></td>';
				results +='</tr>';
	
			}
		}
		orgSelect += '</select></label>';
		$("#entitySearchExtraCriteria").append(orgSelect);
		addEntitySearchExtraCriteria('PROVIDER');
	}
	else if ('ORGANIZATION' == $('#advancedEntitySearchType').val()) {
		results += '<tr><th class="col-md-4">Organizations</th><th class="col-md-4">Address</th><th class="col-md-2"></th></tr>';
		results += '</thead><tbody id="advancedEntitySearchResultsTableBody">';
		if (j.length > 0) {
			for (var i = 0; i < j.length; i++) {
				if (j[i].organizations != null) {
					for (var x = 0; x < j[i].organizations.length; x++) {
						var org = j[i].organizations[x];
						results += '<tr>';
						results += '<td><span class="organizationName">'+org.name +'</span></td><td><span class="address">'+org.address+' ' +org.city+', '+org.state+' ' +org.postalCode+'</td>';
						//results += '<td><span class="orgs">'+orgs+'</span></td>';
						results += '<td><button id="advancedEntitySearchSelect" class="btn btn-xs btn-primary" type="button" onclick="javascript:advancedEntitySearchSelect(this);">Select</button></td>';
						results +='</tr>';
					}
				}
			}
		}

	}
	results += '</tbody></table>';
	$("#entitySearchResults").append(results);
}

function filterAdvancedSearchResults() {
	if ($('#advancedEntitySearchResultsTableBody') != null && $('#advancedEntitySearchResultsTableBody tr').length > 1) {
		if ('PROVIDER' == $('#advancedEntitySearchType').val()) {
			var firstName = $('#entityFirstName').val().toUpperCase();
			var lastName = $('#entityLastName').val().toUpperCase();
			var organizations = $('#advancedEntitySearchOrgFilter').val().toUpperCase();
		
		//var rows = $('#advancedEntitySearchResultsTableBody tr');
			$('#advancedEntitySearchResultsTableBody tr').each(function (i, row) {
			var $row = $(row);
			var fName = $row.find('.firstName');
			var lName = $row.find('.lastName');
			var orgs = $row.find('.orgs');
			$row.show();
			if (fName.html().toUpperCase().indexOf(firstName) < 0)
				$row.hide();
			if (lName.html().toUpperCase().indexOf(lastName) < 0)
				$row.hide();
			if (orgs.html().toUpperCase().indexOf(organizations) < 0)
				$row.hide();
		    });
		}
		else if ('ORGANIZATION' == $('#advancedEntitySearchType').val()) {
			var orgName = $('#entityOrganizationName').val().toUpperCase();
			$('#advancedEntitySearchResultsTableBody tr').each(function (i, row) {
				var $row = $(row);
				var org = $row.find('.organizationName');
				$row.show();
				if (org.html().toUpperCase().indexOf(orgName) < 0)
					$row.hide();
			});
		}
	}
}


function addEntitySearchExtraCriteria(type) {
	if (type == 'PROVIDER') {
		var html = "";
		
		
		//$("#entitySearchExtraCriteria").append(html +'<hr>');
	}
}

function advancedEntitySearchSelect() {
	
}
/** White Label Advanced Entity Search **/



