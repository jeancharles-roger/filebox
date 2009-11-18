package org.kawane.filebox.ui.toolkit;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This toolkit provides methods to build input fields and dialog boxes.
 * @author Didier Simoneau
 */
public class ToolKit {
	
	protected int flatStyle = SWT.NONE;
	
	public void setFlatStyle() {
		flatStyle = SWT.FLAT;
	}
	
	// ********** Fields creation **********


	public Label header(Composite parent, String message) {
		Label labl = new Label(parent, SWT.WRAP);
		labl.setText(message);
		labl.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		labl.setLayoutData(gridData);
		return labl;
	}
	
	public Label message(Composite parent, String message) {
		Label labl = new Label(parent, SWT.WRAP);
		labl.setBackground(parent.getBackground());
		labl.setText(message);
		labl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		return labl;
	}

	public Label separator(Composite parent) {
		Label labl = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		labl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		return labl;
	}
	
	public Text text(Composite parent, String label, String value, int style) {
		Label labl = new Label(parent, SWT.NONE);
		labl.setBackground(parent.getBackground());
		labl.setText(label);
		Text text = new Text(parent, flatStyle | SWT.BORDER | style);
		text.setText(value);
		boolean multiLines = (style & SWT.MULTI ) != 0;
		if (multiLines) {
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gridData.minimumHeight = text.getLineHeight() * 3;
			text.setLayoutData(gridData);
		} else {
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		return text;
	}
	
	public Text textField(Composite parent, String label, String value) {
		return text(parent, label, value, SWT.SINGLE);
	}
	
	public Text textArea(Composite parent, String label, String value) {
		return text(parent, label, value, SWT.MULTI);
	}

	public Button checkBox(Composite parent, String label, boolean value) {
		new Label(parent, SWT.NONE);
		Button button = new Button(parent, flatStyle | SWT.CHECK);
		button.setBackground(parent.getBackground());
		button.setText(label);
		button.setSelection(value);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		return button;
	}

	public Button[] buttons(Composite parent, String... buttonLabels) {
		Button[] result = new Button[buttonLabels.length];
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getBackground());
		composite.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 3, 1));
		composite.setLayout(new GridLayout(buttonLabels.length, true));
		for (int i = 0; i < buttonLabels.length; i++) {
			Button button = new Button(composite, flatStyle | SWT.PUSH);
			button.setBackground(composite.getBackground());
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
			gridData.minimumWidth = 90;
			button.setLayoutData(gridData);
			button.setText(buttonLabels[i]);
			result[i] = button;
		}
		return result;
	}

	// ********** Composite fields **********
	
	public Group group(Composite parent, String label) {
		Group group = new Group(parent, SWT.NONE);
		group.setBackground(parent.getBackground());
		group.setText(label);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		group.setLayout(fieldContainerLayout());
		return group;
	}
	
	// ********** Dialog creation **********

	public Shell dialogShell(Shell parentShell, String title) {
		if (parentShell == null) parentShell = Display.getCurrent().getActiveShell();
		Shell dialog = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		dialog.setText(title);
		dialog.setImage(parentShell.getImage());
		dialog.setLayout(fieldContainerLayout());
		return dialog;
	}
	
	public void computeSizes(Shell dialog, int width) {
		Point computedSize = dialog.computeSize(width, SWT.DEFAULT, true);
		dialog.setSize(width, computedSize.y);
		dialog.setMinimumSize(width, computedSize.y);
	}
	
	// ********** Misc. **********
	
	protected GridLayout fieldContainerLayout() {
		return new GridLayout(3, false);
	}

	public int waitSelectedButton(Button[] buttons) {
		final int[] result = new int[] {-1 };
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result[0] = (Integer) e.widget.getData();
			}
		};
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setData(i);
			buttons[i].addSelectionListener(selectionListener);
		}
		Shell shell = buttons[0].getShell();
		Display display = shell.getDisplay();
		while (!shell.isDisposed() && result[0] == -1) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return result[0];
	}
	
	
	public Text folderField(final Composite parent, String label, String value) {
		return internalFileField(parent, label, value, true);
	}
	
	public Text fileField(final Composite parent, String label, String value) {
		return internalFileField(parent, label, value, false);
	}
	
	private Text internalFileField(final Composite parent, final String label, final String value, final boolean folder) {
		Label labl = new Label(parent, SWT.NONE);
		labl.setBackground(parent.getBackground());
		labl.setText(label);
		final Text text = new Text(parent, flatStyle | SWT.BORDER | SWT.SINGLE);
		text.setText(value);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Button browse = new Button(parent, SWT.PUSH);
		browse.setText("Browse\u2026");
		browse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				String result = null;
				if ( folder ) {
					DirectoryDialog fileDialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);
					if(new File(text.getText()).exists()) {
						fileDialog.setFilterPath(text.getText());
					}
					result = fileDialog.open();
				} else {
					FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN);
					if(new File(text.getText()).exists()) {
						fileDialog.setFilterPath(text.getText());
					}
					result = fileDialog.open();
				}
				if(result != null) {
					text.setText(result);
				}
			}
		});
		return text;
	}
	
	
	public static void main(String [] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		ToolKit tk = new ToolKit();
//		tk.setFlatStyle();
		Shell dialog = tk.dialogShell(shell, "Dialog");
//		dialog.setBackground(dialog.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		tk.header(dialog, "Quel est votre nom?");
		Text firstName = tk.textField(dialog, "First Name:", "John");
		Text lastName = tk.textArea(dialog, "Last Name:", "Smith");
		tk.separator(dialog);
		tk.message(dialog, "Checked if you wish to select it (this is a very very very very long text):");
		tk.checkBox(dialog, "Male", true);
		Group group = tk.group(dialog, "more options");
		tk.checkBox(group, "1", true);
		tk.checkBox(group, "2", false);
		tk.checkBox(group, "3", true);
		tk.text(group, "Age", "20", SWT.PASSWORD);
		tk.separator(dialog);
		Button[] buttons = tk.buttons(dialog, "Ok", "Cancel");
		tk.computeSizes(dialog, 400);
		dialog.open();
		if (tk.waitSelectedButton(buttons) == 0) {
			System.out.println("firstName " + firstName.getText());
			System.out.println("lastName " + lastName.getText());
		}
		System.out.println("Closing");
		dialog.dispose();
		System.out.println("Done");
	}


	
}
