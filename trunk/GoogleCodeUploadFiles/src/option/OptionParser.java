package option;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class OptionParser {

	public class Group {
		int position;
		String display;
		String help;
		int lastPosition = -1;

		public Group(int position, String display, String help) {
			super();
			this.position = position;
			this.display = display;
			this.help = help;
		}

	}

	private String usage;
	private List<OptionDefinition> optionsDefinition = new ArrayList<OptionDefinition>();
	private List<Group> groups = new ArrayList<Group>();
	private OptionDefinition mainArgs;
	private List<String> sampleUsages = new ArrayList<String>();
	private OptionDefinition helpOption;
	private boolean valid = true;

	public OptionParser(String usage, String... sampleUsage) {
		this.usage = usage;
		helpOption = new OptionDefinition("-h", "--help", "display help usage", false);
		optionsDefinition.add(helpOption);
		mainArgs = new OptionDefinition(null, null, usage, true);
		for (String s : sampleUsage) {
			sampleUsages.add(s);
		}
	}

	public void addSampleUsage(String s) {
		sampleUsages.add(s);
	}

	public void newGroup(String display, String help) {
		if (groups.size() > 0) {
			Group group = groups.get(groups.size() - 1);
			group.lastPosition = optionsDefinition.size();
		}
		groups.add(new Group(optionsDefinition.size(), display, help));
	}

	public OptionDefinition addOptionDefinition(String shortOption, String longOption, String help, boolean acceptValue, OptionValidator... validators) {
		OptionDefinition def = new OptionDefinition(shortOption, longOption, help, acceptValue, validators);
		optionsDefinition.add(def);
		return def;
	}

	public String getUsage() {
		return usage;
	}

	public List<Option> parseArgs(String[] args) {
		StringBuilder builder = new StringBuilder();
		ArrayList<Option> mainOptions = new ArrayList<Option>();
		OptionDefinition currentDef = null;
		for (String arg : args) {
			if (currentDef == null || !currentDef.acceptValue()) {
				currentDef = null;
				for (OptionDefinition def : optionsDefinition) {
					if (def.acceptArgument(arg)) {
						currentDef = def;
						if (!def.acceptValue()) {
							String message = def.addOption();
							if (message != null) {
								builder.append(message);
								builder.append("\n");
							}
						}
						break;
					}
				}
				if (currentDef == null) {
					mainOptions.add(new Option(mainArgs, arg));
				}
			} else {
				String message = currentDef.addOption(arg);
				if (message != null) {
					builder.append(message);
					builder.append("\n");
				}
				currentDef = null;
			}
		}
		for (OptionDefinition def : optionsDefinition) {
			String message = def.validateDefinition();
			if (message != null) {
				builder.append(message);
				builder.append("\n");
			}
		}
		if (builder.length() > 0) {
			printError(builder.toString(), System.err);
			printUsage(System.err);
			valid = false;
		} else if (helpOption.getOptions().size() > 0) {
			printUsage(System.out);
			valid = false;
		}
		return mainOptions;
	}

	public boolean isValid() {
		return valid;
	}

	public void printError(String message, PrintStream out) {
		out.println(message);
	}

	public void printUsage(PrintStream out) {
		out.println(usage);
		out.println();
		if (groups.size() > 0 && optionsDefinition.size() > 0) {
			for (Group group : groups) {
				out.print(group.display);
				if (group.help != null) {
					out.print(": ");
					out.print(group.help);
				}
				out.println();
				if (group.lastPosition == -1) {
					group.lastPosition = optionsDefinition.size();
				}
				int i = group.position;
				while (i < group.lastPosition) {
					OptionDefinition def = optionsDefinition.get(i++);
					displayOptionDefinition(out, def);
				}
				out.println();
			}
		} else {
			for (OptionDefinition def : optionsDefinition) {
				displayOptionDefinition(out, def);
			}
		}
		if (sampleUsages.size() > 0) {
			out.println("Sample Usage: ");
			for (String sampleUsage : sampleUsages) {
				out.println(sampleUsage);
			}
		}

	}

	private void displayOptionDefinition(PrintStream out, OptionDefinition def) {
		out.print("    ");
		out.print(def);
		out.print(": ");
		out.print(def.getHelp());
		OptionValidator[] validators = def.getValidators();
		if(validators != null && validators.length > 0) {
			out.print(" (");
			for (int i = 0; i < validators.length; i++) {
				out.print(validators[i]);
				if(i + 1 <validators.length) {
					out.print(", ");
				}
				
			}
			out.print(")");
		}
		out.println();
	}
}
