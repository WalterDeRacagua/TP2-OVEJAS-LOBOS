package simulator.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

public class InfoTable extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String _title;
	private TableModel _tableModel;

	public InfoTable(String title, TableModel tableModel) {
		_title = title;
		_tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout(10, 10));

		Border b = BorderFactory.createLineBorder(Color.black, 2, false);
		this.setBorder(BorderFactory.createTitledBorder(b, this.getTitle(), TitledBorder.LEFT, TitledBorder.TOP));

		JTable t = new JTable(this._tableModel);
		t.getTableHeader().setResizingAllowed(false);
		t.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollBar = new JScrollPane(t);
		this.add(scrollBar, BorderLayout.CENTER);

	}

	private String getTitle() {

		return this._title;
	}
}
