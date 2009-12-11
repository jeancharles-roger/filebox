package option;
import java.io.File;

public class Option {
	private OptionDefinition def;

	private String value;

	public Option(OptionDefinition def) {
		super();
		this.def = def;
	}

	public Option(OptionDefinition def, String value) {
		super();
		this.def = def;
		this.value = value;
	}

	public OptionDefinition getDefinition() {
		return def;
	}

	public boolean hasValue() {
		return value != null;
	}

	public String getValue() {
		return value;
	}

	public int getIntValue() {
		return Integer.parseInt(value);
	}

	public double getDoubleValue() {
		return Double.parseDouble(value);
	}

	public File getFileValue() {
		return new File(value);
	}

}
