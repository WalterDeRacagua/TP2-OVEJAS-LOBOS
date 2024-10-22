package simulator.view;

import javax.swing.SwingWorker;

import simulator.control.Controller;

public class SimulatorWorker extends SwingWorker<Void, Integer> {

	private int _delay;
	private double _dt;
	private int _steps;
	private Controller _ctrl;

	public SimulatorWorker(Controller ctrl, double dt, int steps, int delay) {

		this._ctrl = ctrl;
		this._dt = dt;
		this._steps = steps;
		this._delay = delay;

	}

	@Override
	protected Void doInBackground() throws Exception {

		for (int i = 0; i < this._steps; i++) {

			if (this.isCancelled()) {

				return null;
			}

			this._ctrl.advance(this._dt);
			this.publish(i + 1);
			if (_delay == 0)
				_delay = 1;
			Thread.sleep(this._delay*3);
		}

		return null;
	}

}
