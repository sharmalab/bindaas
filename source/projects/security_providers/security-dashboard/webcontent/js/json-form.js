( function($) {

		$.fn.jsonForm = function(configuration) {
			var jsonFormObject = {};
			jsonFormObject.target = this;
			jsonFormObject.configuration = configuration;
			jsonFormObject.validate = function() {
				return validate(configuration, this);
			};

			var content = drawForm(configuration, this);
			$(this).html(content);

			jsonFormObject.populate = function(obj) {
				populate(configuration, this, obj);
			};

			return jsonFormObject;
		};
	}(jQuery));

function populate(configuration, target, obj2populate) {
	$.each(configuration.schema, function(name, prop) {
		if (prop.viewControl.id) {

			switch(prop.viewControl.id) {

				case 'textfield':
					$("input[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).val(obj2populate[name]);

					break;
				case 'textarea':
					$("textarea[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).val(obj2populate[name]);

					break;
				case 'enumeration':
					$("select[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).find('option').each(function(idx, option) {
						if ($(option).attr('value') == obj2populate[name]) {
							$(option).attr('selected', 'selected');
						} else {
							$(option).removeAttr('selected');
						}
					});

					break;
				// input type='radio'  jfid='#jfid#' jfref='#fieldName#'
				case 'radio' :
					value = $("input[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).each(function(idx, option) {
						if ($(option).attr('value') == obj2populate[name]) {
							$(option).attr('checked', 'checked');
						} else {
							$(option).removeAttr('checked');
						}
					});

					break;

				default :
					throw 'not a valid viewControl';
			}

		} else {
			throw 'Configuration Missing viewControl.id field';
		}
	});

}

/**
 * Validate the form and return a fully contsturcted JSON object if successful else return null
 * @param {Object} configuration
 * @param {Object} target
 */
function validate(configuration, target) {

	var retVal = {};
	var incomplete = false;
	$.each(configuration.schema, function(name, prop) {
		if (prop.viewControl.id) {
			var value;
			switch(prop.viewControl.id) {

				case 'textfield':
					value = $("input[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).val();

					if (prop.required && !value) {
						alert(prop.title + " not specified ");
						$("input[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).focus();
						incomplete = true;
					}

					break;
				case 'textarea':
					value = $("textarea[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).val();
					if (prop.required && !value) {
						alert(prop.title + " not specified ");
						$("textarea[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).focus();
						incomplete = true;
					}
					break;
				case 'enumeration':
					value = $("select[jfid='#jfid#'][jfref='#jfref#'] option:selected".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).attr('value');
					if (prop.required && !value) {
						alert(prop.title + " not specified ");
						$("select[jfid='#jfid#'][jfref='#jfref#']".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).focus();
						incomplete = true;
					}
					break;
				// input type='radio'  jfid='#jfid#' jfref='#fieldName#'
				case 'radio' :
					value = $("input[jfid='#jfid#'][jfref='#jfref#']:checked".replace("#jfid#", configuration.jfid).replace("#jfref#", name)).val();

					if (prop.required && !value) {
						alert(prop.title + " not specified ");
						incomplete = true;
					}

					break;

				default :
					throw 'not a valid viewControl';
			}

			if (prop.validator) {
				var ret = prop.validator(name, value);
				if (ret == false)
					incomplete = true;
			}

			retVal[name] = value;

		} else {
			throw 'Configuration Missing viewControl.id field';
		}
	});

	if (!incomplete)
		return retVal;
	else
		return null;
}

/**
 * Draw jsonForm
 * @param {Object} configuration
 * @param {Object} target
 */
function drawForm(configuration, target) {
	var templateFormGroup = "<div class='form-group'><label>#field-title#</label> #field# </div>";
	var schema = configuration.schema;
	var content = "";
	$.each(schema, function(name, prop) {

		if (prop.viewControl.id) {
			var field;
			switch(prop.viewControl.id) {
				case 'textfield':
					field = drawTextField(name, prop, configuration.jfid);
					break;
				case 'textarea':
					field = drawTextArea(name, prop, configuration.jfid);
					break;
				case 'enumeration':
					field = drawEnumeration(name, prop, configuration.jfid);
					break;
				case 'radio':
					field = drawRadio(name, prop, configuration.jfid);
					break;
				default :
					throw 'not a valid viewControl';
			}

			var formGrp = templateFormGroup.replace("#field-title#", prop.title).replace("#field#", field);
			content = content + formGrp;
		} else {
			throw 'Configuration Missing viewControl.id field';
		}

	});

	return content;
}

function drawTextField(fieldName, properties, jfid) {
	var textfieldTemplate = "<input type='#type#' placeholder='#placeholder#' class='form-control' jfid='#jfid#' jfref='#fieldName#' value='#defaultValue#'>";

	var placeholder = '';
	if (properties.viewControl.placeholder) {
		placeholder = properties.viewControl.placeholder ;
	};

	var defaultValue = '';
	if (properties.viewControl.defaultValue) {
		defaultValue = properties.viewControl.defaultValue ;
	};
	
	var type = 'text';
	if(properties.viewControl.type)
	{
		type = properties.viewControl.type; 
	}

	var textfield = textfieldTemplate.replace("#placeholder#", placeholder).replace("#defaultValue#", defaultValue).replace("#jfid#", jfid).replace("#fieldName#", fieldName).replace("#type#", type);

	return textfield;
}

function drawTextArea(fieldName, properties, jfid) {
	var textareaTemplate = "<textarea placeholder='#placeholder#' class='form-control input-lg' jfid='#jfid#' jfref='#fieldName#' >#defaultValue#</textarea>";

	var placeholder = '';
	if (properties.viewControl.placeholder) {
		placeholder = properties.viewControl.placeholder
	};

	var defaultValue = '';
	if (properties.viewControl.defaultValue) {
		defaultValue = properties.viewControl.defaultValue
	};

	var textarea = textareaTemplate.replace("#placeholder#", placeholder).replace("#defaultValue#", defaultValue).replace("#jfid#", jfid).replace("#fieldName#", fieldName);

	return textarea;
}

function drawRadio(fieldName, properties, jfid) {
	var template = "<div class='radio'><label><input type='radio'  jfid='#jfid#' jfref='#fieldName#' name='#fieldName#' #selected# value='#optionValue#'> #optionTitle#  </label></div>";
	var fieldNameRegEx = new RegExp("#fieldName#", 'g');
	var jfidRegEx = new RegExp("#jfid#", 'g');
	var content = "";
	if (properties.viewControl.options) {
		properties.viewControl.options.forEach(function(option) {

			var field = template.replace(fieldNameRegEx, fieldName).replace("#optionValue#", option.value).replace("#optionTitle#", option.title);
			var selected = "";
			if (properties.viewControl.defaultSelected && option.value == properties.viewControl.defaultSelected) {
				selected = 'checked';
			}

			content = content + field.replace("#selected#", selected);
		});
	}

	content = content.replace(jfidRegEx, jfid);
	return content;
}

function drawEnumeration(fieldName, properties, jfid) {
	var template = "<option  jfid='#jfid#'  #selected# value='#optionValue#'> #optionTitle# </option>";
	var fieldNameRegEx = new RegExp("#fieldName#", 'g');
	var jfidRegEx = new RegExp("#jfid#", 'g');
	var content = "<select class='form-control' jfref='#fieldName#' jfid='#jfid#'> #options# </select> ";
	var options = "";
	if (properties.viewControl.options) {
		properties.viewControl.options.forEach(function(option) {

			var field = template.replace("#optionValue#", option.value).replace("#optionTitle#", option.title);
			var selected = "";
			if (properties.viewControl.defaultSelected && option.value == properties.viewControl.defaultSelected) {
				selected = "selected='selected'";
			}

			options = options + field.replace("#selected#", selected);
		});
	}

	content = content.replace("#options#", options).replace(fieldNameRegEx, fieldName);
	content = content.replace(jfidRegEx, jfid);
	return content;
}

var configuration = {
	jfid : 'randomId',
	schema : {
		"name" : {
			type : 'string',
			title : "Name",
			required : true,
			validator : function(name, text) {
				return true;
			},
			viewControl : {
				id : 'textfield',
				placeholder : 'placeholder',
				defaultValue : ''
			}
		},
		"bloodgroup" : {
			type : 'string',
			title : "Bloodgroup",
			required : true,
			validator : function(name, text) {
				return true;
			},
			viewControl : {
				id : 'enumeration',
				defaultSelected : "A+",
				options : [{
					value : 'AB+',
					title : 'AB+'
				}, {
					value : 'A+',
					title : 'A+'
				}]
			}
		},
		"gender" : {
			type : 'string',
			title : "Gender",
			required : false,
			validator : function(name, text) {
				return true;
			},
			viewControl : {
				id : 'radio',
				options : [{
					value : 'M',
					title : 'Male'
				}, {
					value : 'F',
					title : 'Female'
				}],
				defaultSelected : 'M'
			}
		},
		"about" : {
			type : 'string',
			title : "About Me",
			required : false,
			viewControl : {
				id : 'textarea',
				placeholder : 'placeholder',
				defaultValue : ''
			}
		}
	}
};
