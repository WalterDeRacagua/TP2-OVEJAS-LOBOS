package simulator.model;

public class DefaultRegion extends Region {

	private static final double PARAM_1 = 60.0;
	private static final double PARAM_2 = 5.0;
	private static final double PARAM_3 = 2.0;

	@Override
	public void update(double dt) {
		// Do nothing
	}

	@Override
	public double get_food(Animal a, double dt) {
		if (a.get_diet().equals(Diet.CARNIVORE)) {
			return 0.0;
		}
		int n = this.herviboresCount();
		return PARAM_1 * Math.exp(-Math.max(0, n - PARAM_2) * PARAM_3) * dt;

	}

}
