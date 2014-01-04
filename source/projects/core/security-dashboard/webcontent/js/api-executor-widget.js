( function($) {

		$.fn.apiExplorerWidget = function(configuration) {
			if (configuration.apiType == 'query') {
				drawQueryAPI(this, configuration);
			} else if (configuration.apiType == 'submit') {
				drawSubmitAPI(this, configuration);
			} else if (configuration.apiType == 'delete') {
				drawDeleteAPI(this, configuration);
			} else {
				console.log("invalid apiType specified");
			}

			return this;
		};

	}(jQuery));

function display() {
	var configuration = {
		apiType : 'query',
		url : 'http://someurl.org/',
		bindVariables : [{
			name : 'PatientID',
			required : true,
			defaultValue : '',
			type : '',
			description : ''
		}, {
			name : 'PatientID',
			required : true,
			defaultValue : '',
			type : '',
			description : ''
		}],
		mime : true,
		description : "API for patient Data",
		widgetId : 'someuniqueid',
		apiKey : 'adfadfdsfasdf'
	};
}

function drawQueryAPI(element, configuration) {
	var pan = "<div class='panel' apiWidget='#apiWidget#'><div class='panel-heading'>" + "<div class='row'><div class='col-lg-3'><h4>URL</h4></div><div class='col-lg-9'><h5>" + configuration.url + "</h5></div></div>" + "<div class='row'><div class='col-lg-3'><h4>Description</h4></div><div class='col-lg-9'><h5>" + configuration.description + "</h5></div></div>" + "</div>";

	var content = pan + "<div class='hidden' apiWidget='#apiWidget#' configuration >" + JSON.stringify(configuration) + "</div>";
	content = content + "<div class='row'><div class='col-lg-12'>";

	var table = "<table apiWidget='#apiWidget#' class='table table-striped table-bordered'><thead><tr><th>Name</th><th>Value</th><th>Description</th><th>Default Value</th><th>Required</th></tr></thead>";
	var tbody = "<tbody>";

	configuration.bindVariables.forEach(function(bindVar) {
		tbody = tbody + "<tr>";
		tbody = tbody + "<td>" + bindVar.name + "</td>";
		tbody = tbody + "<td><input class='form-control' type='text' apiWidget='#apiWidget#'  bindVar='" + bindVar.name + "'></td>";
		tbody = tbody + "<td>" + bindVar.description + "</td>";
		tbody = tbody + "<td>" + bindVar.defaultValue + "</td>";
		tbody = tbody + "<td>" + bindVar.required + "</td>";
		tbody = tbody + "</tr>";
	});
	tbody = tbody + "</tbody>";
	table = table + tbody + "</table>";

	content = content + table;
	content = content + "</div></div>";
	content = content + "<div class='row'><div class='col-lg-12'><a class='btn-default btn-small' apiWidget='#apiWidget#' onclick='executeQueryAPI(\"#apiWidget#\")'>Submit</a></div></div>";
	content = content + "</div>";
	var regEx = new RegExp("#apiWidget#", 'g');
	content = content.replace(regEx, configuration.widgetId);
	$(element).html(content);
}

function drawDeleteAPI(element, configuration) {
	var pan = "<div class='panel' apiWidget='#apiWidget#'><div class='panel-heading'>" + "<div class='row'><div class='col-lg-3'><h4>URL</h4></div><div class='col-lg-9'><h5>" + configuration.url + "</h5></div></div>" + "<div class='row'><div class='col-lg-3'><h4>Description</h4></div><div class='col-lg-9'><h5>" + configuration.description + "</h5></div></div>" + "</div>";

	var content = pan + "<div class='hidden' apiWidget='#apiWidget#' configuration >" + JSON.stringify(configuration) + "</div>";
	content = content + "<div class='row'><div class='col-lg-12'>";

	var table = "<table class='table table-striped table-bordered'><thead><tr><th>Name</th><th>Value</th><th>Description</th><th>Default Value</th><th>Required</th></tr></thead>";
	var tbody = "<tbody>";

	configuration.bindVariables.forEach(function(bindVar) {
		tbody = tbody + "<tr>";
		tbody = tbody + "<td>" + bindVar.name + "</td>";
		tbody = tbody + "<td><input class='form-control' type='text' apiWidget='#apiWidget#'  bindVar='" + bindVar.name + "'></td>";
		tbody = tbody + "<td>" + bindVar.description + "</td>";
		tbody = tbody + "<td>" + bindVar.defaultValue + "</td>";
		tbody = tbody + "<td>" + bindVar.required + "</td>";
		tbody = tbody + "</tr>";
	});
	tbody = tbody + "</tbody>";
	table = table + tbody + "</table>";

	content = content + table;
	content = content + "</div></div>";
	content = content + "<div class='row'><div class='col-lg-12'><a class='btn-default btn-small' apiWidget='#apiWidget#' onclick='executeDeleteAPI(\"#apiWidget#\")'>Submit</a></div></div>";
	content = content + "</div>";
	var regEx = new RegExp("#apiWidget#", 'g');
	content = content.replace(regEx, configuration.widgetId);
	$(element).html(content);

}

function drawSubmitAPI(element, configuration) {
	var form = "<form method='post' action='#actionUrl#' enctype='multipart/form-data' target='_blank'>".replace("#actionUrl#" , configuration.url + "?api_key=" + configuration.apiKey);
	var pan = "<div class='panel' apiWidget='#apiWidget#'><div class='panel-heading'>" + "<div class='row'><div class='col-lg-3'><h4>URL</h4></div><div class='col-lg-9'><h5>" + configuration.url + "</h5></div></div>" + "<div class='row'><div class='col-lg-3'><h4>Description</h4></div><div class='col-lg-9'><h5>" + configuration.description + "</h5></div></div>" + "</div>";

	var content = pan + "<div class='hidden' apiWidget='#apiWidget#' configuration >" + JSON.stringify(configuration) + "</div>";
	content = content + "<div class='row'><div class='col-lg-12'>";

	if (configuration.mime == true) {
		var submitInput = "<div class='form-group'><label>File input</label><input  type='file'></div><p></p>";
		content = content + submitInput;
	} else  {
		var submitInput = "<textarea apiWidget='#apiWidget#' class='form-control input-xlarge' rows='10'  placeholder='Content to Upload'></textarea><p></p>";
		content = content + submitInput;
	} 

	content = content + "</div></div>";
	content = content + "<div class='row'><div class='col-lg-12'><a class='btn-default btn-small' apiWidget='#apiWidget#' onclick='executeSubmitAPI(\"#apiWidget#\")'>Submit</a></div></div>";
	content = content + "</div>";
	var regEx = new RegExp("#apiWidget#", 'g');
	content = content.replace(regEx, configuration.widgetId);
	form = form + content + "</form>";
	$(element).html(form);

}

function executeQueryAPI(widgetId) {
	var selector = "div[configuration][apiWidget='#apiWidget#']".replace("#apiWidget#", widgetId);
	var content = $(selector).html();
	var configuration = JSON.parse(content);

	var queryParams = {};
	selector = "input[apiWidget='#apiWidget#'][bindVar]".replace("#apiWidget#", widgetId);
	$(selector).each(function() {
		var paramName = $(this).attr('bindVar');
		var paramValue = $(this).val();
		if (paramValue && paramValue != "") {
			queryParams[paramName] = paramValue;
		}
	});
	queryParams["api_key"] = configuration.apiKey;

	var url = configuration.url + "?" + $.param(queryParams);
	//  open url in another tab
	window.open(url);

}
//<form method='post' enctype='multipart/form-data' target='_blank'>
function executeSubmitAPI(widgetId) {
	var selector = "div[configuration][apiWidget='#apiWidget#']".replace("#apiWidget#", widgetId);
	var content = $(selector).html();
	var configuration = JSON.parse(content);
	
	if(configuration.mime == true)
	{
		// upload file
		$(selector).closest('form').submit();
	}
	else 
	{
		var textareaSelector = "textarea[apiWidget='#apiWidget#']".replace("#apiWidget#", widgetId);
		var text = $(textareaSelector).attr('value');
		var url = configuration.url + "?api_key" + configuration.apiKey;
		console.log("Submitting data \n" + text + "\tTo " + url);
		$.post(url, text, function(data, status, response) {
					alert(response.responseText);
				});
	}
}

function executeDeleteAPI(widgetId) {
	var selector = "div[configuration][apiWidget='#apiWidget#']".replace("#apiWidget#", widgetId);
	var content = $(selector).html();
	var configuration = JSON.parse(content);

	var queryParams = {};
	selector = "input[apiWidget='#apiWidget#'][bindVar]".replace("#apiWidget#", widgetId);
	$(selector).each(function() {
		var paramName = $(this).attr('bindVar');
		var paramValue = $(this).val();
		if (paramValue && paramValue != "") {
			queryParams[paramName] = paramValue;
		}
	});
	queryParams["api_key"] = configuration.apiKey;

	var url = configuration.url + "?" + $.param(queryParams);

	$.ajax({
		url : url,
		type : 'DELETE',
		crossDomain : true,
		dataType : 'text',
		success : function(result) {
			alert('Server Response\n' + result);
		},
		error : function(errorObj, textStatusLine, errorThrown) {
			alert(errorObj.responseText);
			console.log('Server Error Code:' + errorThrown);
		}
	});

}

