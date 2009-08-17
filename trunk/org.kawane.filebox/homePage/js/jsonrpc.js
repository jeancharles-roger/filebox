// JQUERY dependent

function formToObject(form) {
	var o = new Object();
	length = form.elements.length;
	for ( var i = 0; i < length; i++) {
		e = form.elements[i];
		if (e.name != "") {
			name = form.elements[i].name;
			value = form.elements[i].value;
			o[name] = value;
		}
	}
	return o;
}

function json(o) {
	return JSON.stringify(o, function(key, value) {
		return value;
	}, '\t')
}

function submitForm(form) {
	var o = formToObject(form);
	var id = $.trim(o.name).replace(" ", "_");
	$.post(form.action + "/" + id, json(o));
	// do not use default submit function
	return false;
}

function parseValue(str, cursor) {
	var result = "";
	if (str[cursor++] != '{') {
		return [ null, --cursor ];
	}
	while (cursor < str.length) {
		var c = str[cursor++];
		if (c == '}') {
			break;
		}
		result += c;
	}
	return [ result, --cursor ];
}

function templateStr(str, data) {
	var result = "";
	for ( var cursor = 0; cursor < str.length; cursor++) {
		var c = str[cursor];
		if (c == '$') {
			var val = parseValue(str, ++cursor);
			if (val[0] != null) {
				cursor = val[1];
				result += eval("data." + val[0]);
				continue;
			} else {
				cursor = val[1] - 1;
			}
		}
		result += c;
	}
	return result;
}

function template(element, data) {
	return templateStr(element.html(), data);
}


