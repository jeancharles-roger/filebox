package option;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptionDefinition {

	private String shortOption;
	private String longOption;
	private String help;
	private OptionValidator[] validators;
	private List<Option> options = new ArrayList<Option>();
	private boolean acceptValue;

	public OptionDefinition(String shortOption, String longOption, String help, boolean acceptValue, OptionValidator... validators) {
		this.shortOption = shortOption;
		this.longOption = longOption;
		this.help = help;
		this.acceptValue = acceptValue;
		this.validators = validators;
	}

	public String getShortOption() {
		return shortOption;
	}

	public void setShortOption(String shortOption) {
		this.shortOption = shortOption;
	}

	public String getLongOption() {
		return longOption;
	}

	public void setLongOption(String longOption) {
		this.longOption = longOption;
	}

	public void setAcceptValue(boolean acceptValue) {
		this.acceptValue = acceptValue;
	}

	public boolean acceptValue() {
		return acceptValue;
	}
	
	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}
	
	public OptionValidator[] getValidators() {
		return validators;
	}
	
	public boolean acceptArgument(String arg) {
		return arg.equals(shortOption) ||	arg.equals(longOption);
	}
	
	public List<Option> getOptions() {
		return options;
	}
	
	public List<String> getOptionsValue() {
		ArrayList<String> optionsValue = new ArrayList<String>();	
		for (Option o : options) {
			optionsValue.add(o.getValue());
		}
		return optionsValue;
	}
	public List<Integer> getOptionsIntValue() {
		ArrayList<Integer> optionsValue = new ArrayList<Integer>();	
		for (Option o : options) {
			optionsValue.add(o.getIntValue());
		}
		return optionsValue;
	}
	public List<Double> getOptionsDoubleValue() {
		ArrayList<Double> optionsValue = new ArrayList<Double>();	
		for (Option o : options) {
			optionsValue.add(o.getDoubleValue());
		}
		return optionsValue;
	}
	public List<File> getOptionsFileValue() {
		ArrayList<File> optionsValue = new ArrayList<File>();	
		for (Option o : options) {
			optionsValue.add(o.getFileValue());
		}
		return optionsValue;
	}
	
	public String addOption() {
		return addOption(null);
	}
	
	public String addOption(String value) {
		StringBuilder builder = new StringBuilder();
		for (OptionValidator validator : validators) {
			String message = validator.isValidValue(this, value);
			if(message != null) {
				if(builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(message);
			}
		}
		if(builder.length() > 0) {
			return builder.toString();
		}
		Option o = new Option(this, value);
		options.add(o);
		return null;
	}
	
	public String validateDefinition() {
		StringBuilder builder = new StringBuilder();
		for (OptionValidator validator : validators) {
			String message = validator.isValidDefinition(this);
			if(message != null) {
				if(builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(message);
			}
		}
		if(builder.length() > 0) {
			return builder.toString();
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(shortOption != null) buf.append(shortOption);
		if(shortOption != null && longOption != null)  buf.append(" | ");
		if(longOption != null) buf.append(longOption);
		return buf.toString();
	}
	

}
