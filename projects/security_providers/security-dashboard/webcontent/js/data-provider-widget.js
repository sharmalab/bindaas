/**
 * Widget for data-provider input.
 * Can be used both for createDataProvider and viewDataProvider
 * @param {Object} $
 */

( function($) {

		$.fn.dataProviderWidget = function(configuration) {

			configuration.target = this;
			drawDataProviderWidget(configuration);
			return this;
		};
	}(jQuery));

function drawDataProviderWidget(configuration) {
	var widgetRegex = new RegExp('#widgetId#', 'g');
	var form = "<form widgetId='#widgetId#'>#fieldset#</form>";
	var fieldset = " #lastUpdated# #updatedBy# #name# #description# #dataProvider# #dataSourceConfiguration# <div  widgetId='#widgetId#' name='errorMessageContainer'></div>  #buttons# ";
	var lastUpdated = "";
	var updatedBy = "";
	var dataProvider = "";
	var disabled = "";
	var name = "<div class='form-group'><label>Name</label><input type='text' name='name' class='form-control' widgetId='#widgetId#' placeholder='Name' editable='false' value='#dataSourceName#'></input></div>";
	var description = "<div class='form-group'><label>Description</label><input type='text' name='description' class='form-control' widgetId='#widgetId#' placeholder='Description' editable='true' value='#desc#' ></input></div>";
	var dsn = "";
	var desc = "";

	var buttons;
	if (configuration.mode == 'display') {
		if (configuration.data) {
			disabled = "disabled";
			lastUpdated = "<div class='form-group'><label>Last Modified : #lastUpdated# </label></div>".replace("#lastUpdated#", configuration.data.lastModified);
			updatedBy = "<div class='form-group'><label>Modified By : #updatedBy# </label></div>".replace("#updatedBy#", configuration.data.modifiedBy);
			dataProvider = "<div class='form-group'><label>Database</label><input type='text' name='dataProvider' class='form-control' widgetId='#widgetId#' data-value='#id#' placeholder='Database' value='#val#' editable='false'></div>";
			dataProvider = dataProvider.replace("#id#", configuration.data.dataSourceProvider.id).replace("#val#", configuration.data.dataSourceProvider.name);
			buttons = "<div class='form-group'><button type='button' widgetId='#widgetId#' name='submit' class='btn btn-default'>Edit</button><button type='button' widgetId='#widgetId#' name='cancel' class='btn btn-default hide'>Cancel</button></div>";
			dsn = configuration.data.dataSourceName;
			desc = configuration.data.description;

		} else {
			throw "data should be specified in display mode";
		}
	} else if (configuration.mode == 'create') {
		dataProvider = "<select widgetId='#widgetId#' class='form-control' name='dataProvider' >#options#</select>";
		var options = "";
		configuration.dataSourceProviderList.forEach(function(val) {
			options = options + "<option value='#id#'>#val#</option>".replace("#id#", val.id).replace("#val#", val.name);
		});
		dataProvider = dataProvider.replace("#options#", options);
		dataProvider = "<div class='form-group'><label>Database</label> #content# </div>".replace("#content#", dataProvider);
		buttons = "<div class='form-group'><button type='button' widgetId='#widgetId#' name='submit' class='btn btn-default'>Save</button></div>";
	} else
		throw "Invalid mode specified";

	// attach  cancel button handler

	$(configuration.target).on("finalize".replace(widgetRegex, configuration.widgetId) , function() {
		$("button[widgetId='#widgetId#'][name='cancel']".replace(widgetRegex, configuration.widgetId)).click(function() {
			// referesh page
			location.reload();
		});

	});

	// attach edit button handler

	var dataProviderConfiguration;

	// if dataSourceConfigurationSchema is available use that to create UI
	if (configuration.dataSourceConfigurationSchema) {

		dataProviderConfiguration = "<div class='form-group' widgetId='#widgetId#' name='dataSourceConfigurationContainer'></div><textarea name='dataSourceConfiguration' class='form-control hidden' widgetId='#widgetId#' placeholder='{}' ></textarea>";

		$(configuration.target).on("finalize".replace(widgetRegex, configuration.widgetId) , function(){

			// initialize jsonForm
			var jsonForm = $("div[widgetId='#widgetId#'][name='dataSourceConfigurationContainer']".replace("#widgetId#", configuration.widgetId)).jsonForm(configuration.dataSourceConfigurationSchema);

			// if there is data initialize the UI elements with that
			$("button[widgetId='#widgetId#'][name='submit']".replace(widgetRegex, configuration.widgetId)).click(function() {

				var validateAndSave = false;
				if (configuration.mode == 'create' || (configuration.mode == 'display' && $(this).text() == 'Save')) {
					validateAndSave = true;
				}

				if (configuration.mode == 'display') {
					if ($(this).text() == 'Edit')// enter edit mode
					{
						$(this).text('Save');

						// enable all fields
						$(configuration.target).find("*").not("[editable='false']").removeAttr('disabled', 'disabled');

						// show cancel button
						$("button[widgetId='#widgetId#'][name='cancel']".replace(widgetRegex, configuration.widgetId)).removeClass('hide');
					} else// enter save mode
					{
						$(this).text('Edit');
					}
				}

				if (validateAndSave) {
					var retVal = jsonForm.validate();
					if (retVal) {

						// construct payload to submit to the server
						var serverPayload = {};
						serverPayload.name = $(configuration.target).find("input[name='name']").val();
						serverPayload.description = $(configuration.target).find("input[name='description']").val();

						if (configuration.mode == 'create') {
							serverPayload.dataSourceProviderId = $("select[name='dataProvider'] option:selected").attr('value');
						} else {
							serverPayload.dataSourceProviderId = $(configuration.target).find("input[name='dataProvider']").attr('data-value');
						}

						serverPayload.dataSourceConfiguration = retVal;
						var dataToSubmit = JSON.stringify(serverPayload);
						console.log("Submitting to server :\n" + dataToSubmit);

						// ajax call to submit data on success reload , on failure display error
						$.post(configuration.url, dataToSubmit).done(function(data) {
							// refresh page
							location.reload();
						}).fail(function(jqXHR, textStatus, errorThrown) {
							$("div[widgetId='#widgetId#'][name='errorMessageContainer']".replace(widgetRegex, configuration.widgetId)).html("<div class='alert'><a class='close' data-dismiss='alert' href='#'>&times;</a><strong>Error Saving Changes to the server !!</strong><div>#message#</div></div>".replace("#message#", errorThrown));
							console.log(errorThrown);
							console.log(textStatus);
						}).always(function() {
							console.log('request completed');
						});

					}

				}

			});

			if (configuration.mode == 'display') {
				jsonForm.populate(configuration.data.dataSourceConfiguration);
				// disable all inputs
				$(configuration.target).find("input,textarea,select").attr('disabled', 'disabled');

			}

		});

	} else {
		// Just leave the textarea field as is
		dataProviderConfiguration = "<div class='form-group'><label>Configuration</label><textarea name='dataSourceConfiguration' class='form-control input-lg' widgetId='#widgetId#' placeholder='{}' editable='true'></textarea></div>";

		// attach submit handler
		
		$(configuration.target).on("finalize".replace(widgetRegex, configuration.widgetId) , function(){
			$("button[widgetId='#widgetId#'][name='submit']".replace(widgetRegex, configuration.widgetId)).click(function() {

				var validateAndSave = false;
				if (configuration.mode == 'create' || (configuration.mode == 'display' && $(this).text() == 'Save')) {
					validateAndSave = true;
				}

				if (configuration.mode == 'display') {
					if ($(this).text() == 'Edit')// enter edit mode
					{
						$(this).text('Save');

						// enable all fields
						$(configuration.target).find("*").not("[editable='false']").removeAttr('disabled', 'disabled');

						// show cancel button
						$("button[widgetId='#widgetId#'][name='cancel']".replace(widgetRegex, configuration.widgetId)).removeClass('hide');
					} else// enter save mode
					{
						$(this).text('Edit');
					}
				}

				if (validateAndSave) {
					var retVal;
					try {
						var val = $("textarea[widgetId='#widgetId#'][name='dataSourceConfiguration']".replace(widgetRegex, configuration.widgetId)).val();
						retVal = JSON.parse(val);
					} catch(e) {
					}
					if (retVal) {

						// construct payload to submit to the server
						var serverPayload = {};
						serverPayload.name = $(configuration.target).find("input[name='name']").val();
						serverPayload.description = $(configuration.target).find("input[name='description']").val();

						if (configuration.mode == 'create') {
							serverPayload.dataSourceProviderId = $("select[name='dataProvider'] option:selected").attr('value');
						} else {
							serverPayload.dataSourceProviderId = $(configuration.target).find("input[name='dataProvider']").attr('data-value');
						}

						serverPayload.dataSourceConfiguration = retVal;
						var dataToSubmit = JSON.stringify(serverPayload);
						console.log("Submitting to server :\n" + dataToSubmit);

						// ajax call to submit data on success reload , on failure display error
						$.post(configuration.url, dataToSubmit).done(function(data) {
							// refresh page
							location.reload();
						}).fail(function(jqXHR, textStatus, errorThrown) {
							$("div[widgetId='#widgetId#'][name='errorMessageContainer']".replace(widgetRegex, configuration.widgetId)).html("<div class='alert'><a class='close' data-dismiss='alert' href='#'>&times;</a><strong>Error Saving Changes to the server !!</strong><div>#message#</div></div>".replace("#message#", errorThrown));
							console.log(errorThrown);
							console.log(textStatus);
						}).always(function() {
							console.log('request completed');
						});

					}

				}

			});

			if (configuration.mode == 'display') {
				$("textarea[widgetId='#widgetId#'][name='dataSourceConfiguration']".replace(widgetRegex, configuration.widgetId)).text(JSON.stringify(configuration.data.dataSourceConfiguration));
				// disable all inputs
				$(configuration.target).find("input,textarea,select").attr('disabled', 'disabled');
			}
		});

	}

	fieldset = fieldset.replace("#lastUpdated#", lastUpdated).replace("#updatedBy#", updatedBy).replace("#name#", name).replace("#description#", description).replace("#dataProvider#", dataProvider).replace("#dataSourceConfiguration#", dataProviderConfiguration).replace("#dataSourceName#", dsn).replace("#desc#", desc).replace("#buttons#", buttons);

	form = form.replace("#fieldset#", fieldset);

	form = form.replace(widgetRegex, configuration.widgetId);
	$(configuration.target).html(form);
}

// mode: [create|display]
var configuration = {
	widgetId : 'randomId',
	mode : 'create',
	data : {
		project : '',
		dataSourceName : '',
		description : '',
		dataSourceProvider : {
			name : 'MongoDB',
			id : "edu.emory.bindaas"
		},
		lastModified : '',
		modifiedBy : '',
		dataSourceConfiguration : {}
	},
	url : 'http://url/to/post/data',
	dataSourceProviderList : [{
		'name' : 'MongoDB',
		id : "edu.emory.bindaas"
	}, {
		'name' : 'DB2',
		id : "edu.emory.bindaas"
	}],

	dataSourceConfigurationSchema : {
		schema : {
			name : {
				type : 'string',
				title : 'Name',
				required : true
			},
			age : {
				type : 'number',
				title : 'Age'
			}
		}
	},
	onError : function(e) {
	},
	onSuccess : function(e) {
	}
};

