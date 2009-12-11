package option;

public interface OptionValidator {
	String isValidDefinition(OptionDefinition optionDefinition);
	String isValidValue(OptionDefinition optionDefinition, String value);
	
	OptionValidator trueValidator = new OptionValidator() {
		public String isValidDefinition(OptionDefinition optionDefinition) {
			return null;
		}
		public String isValidValue(OptionDefinition optionDefinition, String value) {
			return null;
		}
		public String toString() {return "True";}
	};
	OptionValidator nullValidator = new OptionValidator() {
		public String isValidDefinition(OptionDefinition optionDefinition) {
			return null;
		}
		public String isValidValue(OptionDefinition optionDefinition, String value) {
			return value != null ?  null : "you have to specify a value for this argmument: " + optionDefinition;
		}
		public String toString() {return "Value required";}
	};
	OptionValidator unique = new OptionValidator() {
		public String isValidDefinition(OptionDefinition optionDefinition) {
			return optionDefinition.getOptions().size() > 1 ?  "you can't specify more than one value for this argmument: " + optionDefinition: null;
		}
		public String isValidValue(OptionDefinition optionDefinition, String value) {
			return null;
		}
		public String toString() {return "Unique";}
	};
	OptionValidator uniqueValueValidator = new OptionValidator() {
		public String isValidDefinition(OptionDefinition optionDefinition) {
			return null;
		}
		public String isValidValue(OptionDefinition optionDefinition, String value) {
			for (Option option : optionDefinition.getOptions()) {
				if(value == option.getValue() || (value != null && value.equals(value))) {
					return "you can't specify more than one same value for this argmument: " + optionDefinition;
				}
			}
			return null;
		}
		public String toString() {return "Unique value";}
	};
	OptionValidator required = new OptionValidator() {
		public String isValidDefinition(OptionDefinition optionDefinition) {
			return optionDefinition.getOptions().size() > 0?null:"This argmument is required: " + optionDefinition;
		}
		public String isValidValue(OptionDefinition optionDefinition, String value) {
			return null;
		}
		public String toString() {return "Required";}
	};
}
