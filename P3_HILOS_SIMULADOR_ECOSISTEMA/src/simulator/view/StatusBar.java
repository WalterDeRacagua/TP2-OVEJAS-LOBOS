package simulator.view;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;

class StatusBar extends JPanel implements EcoSysObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private JLabel _time;
	private JLabel _animals;
	private JLabel _dimensions;
	private double time;
	private MapInfo map;
	private List<AnimalInfo> animals;

	public StatusBar(Controller ctrl) {
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		initGUI();
	}

	private void initGUI() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createBevelBorder(1));

		this.time = Math.round(this.time * 1000.0) / 1000.0;
		_time = new JLabel("Time:  " + time + " ");
		JPanel timePanel = new JPanel();
		timePanel.setPreferredSize(new Dimension(80, 20));
		timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		timePanel.add(_time);
		add(timePanel);

		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));
		add(s);

		this._animals = new JLabel("Total Animals:  " + animals.size() + "  ");
		JPanel animalsCountPanel = new JPanel();
		animalsCountPanel.setPreferredSize(new Dimension(150, 20));
		animalsCountPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		animalsCountPanel.add(this._animals);
		add(animalsCountPanel);

		JSeparator s2 = new JSeparator(JSeparator.VERTICAL);
		s2.setPreferredSize(new Dimension(10, 20));
		add(s2);

		_dimensions = new JLabel("Dimension:  " + map.get_width() + "x" + map.get_height() + " " + map.get_cols() + "x"
				+ map.get_rows());
		add(this._dimensions);

	}

	private void updateStatus() {
		this.time = Math.round(this.time * 1000.0) / 1000.0;
		this._time.setText("Time: " + this.time + " ");
		this._animals.setText("Total animals: " + this.animals.size() + " ");
		this._dimensions.setText(
				"Dimension: " + map.get_width() + "x" + map.get_height() + " " + map.get_cols() + "x" + map.get_rows());
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		this.time = time;
		this.map = map;
		this.animals = animals;
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.time = time;
		this.map = map;
		this.animals = animals;
		this.updateStatus();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this.time = time;
		this.map = map;
		this.animals = animals;
		this.updateStatus();
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this.map = map;
		this.updateStatus();
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

		SwingUtilities.invokeLater(() -> {
			this.time = time;
			this.map = map;
			this.animals = animals;
			this.updateStatus();
		});

	}

}
