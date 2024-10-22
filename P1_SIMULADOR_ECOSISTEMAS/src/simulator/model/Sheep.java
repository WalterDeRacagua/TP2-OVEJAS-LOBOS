package simulator.model;

import simulator.misc.Vector2D;
import simulator.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Sheep extends Animal {

	private static final double INITIAL_SIGHT = 40.0;
	private static final double INITIAL_SPEED = 35.0;
	private static final double DEAD_PARAM = 8.0;
	private static final double OUT_PARAM = 8.0;
	private static final double MAX_PARAM = 100.0;
	private static final double MIN_PARAM = 0.0;
	private static final double ENERGY_PARAM = 0.007;
	private static final double ENERGY_PARAM_2 = 20.0;
	private static final double ENERGY_PARAM_3 = 1.2;
	private static final double DESIRE_PARAM = 40.0;
	private static final double DESIRE_PARAM_2 = 65.0;
	private static final double SPEED_PARAM = 2.0;
	private static final double BABY_PROB_PARAM = 0.1;

	private static final String GENETIC_CODE = "Sheep";

	private SelectionStrategy _danger_strategy;
	private Animal _danger_source;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super(GENETIC_CODE, Diet.HERBIVORE, INITIAL_SIGHT, INITIAL_SPEED, mate_strategy, pos);

		this._danger_strategy = danger_strategy;
		this._danger_source = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this._danger_strategy = p1._danger_strategy;
		this._mate_strategy = p1._mate_strategy;
		this._danger_source = null;
	}

	private void updateNormalState(double dt) {
		this.advance(dt);
	}

	private void changeNormalState() {
		if (this._danger_source == null) {
			this.findDanger();
		}
		if (this._danger_source != null) {
			this.changeStateToDanger();
		} else if (this._danger_source == null && this._desire > DESIRE_PARAM_2) {
			this.changeStateToMate();
		}
	}

	private void updateDangerState(double dt) {
		if (this._danger_source != null && !this._danger_source.isAlive()) {
			this._danger_source = null;
		}
		if (this._danger_source == null) {
			this.advance(dt);
		} else
			scape(dt);

	}

	private void scape(double dt) {
		this._dest = _pos.plus(_pos.minus(this._danger_source.get_position()).direction());
		this.move(SPEED_PARAM * this._speed * dt * Math.exp((this._energy - MAX_PARAM) * ENERGY_PARAM));
		this._age += dt;
		this.addEnergy(-ENERGY_PARAM_2 * ENERGY_PARAM_3 * dt);
		this.addDesire(DESIRE_PARAM * dt);
	}

	private void changeDangerState() {
		if (this._danger_source == null || !this.isInRange(this._danger_source)) {
			this.findDanger();
			if (this._danger_source == null) {
				if (this._desire < DESIRE_PARAM_2) {
					this.changeStateToNormal();
				} else {
					this.changeStateToMate();
				}
			}
		}
	}

	private void findDanger() {
		Predicate<Animal> filter = animal -> animal.get_diet().equals(Diet.CARNIVORE);// Por si se añaden más peligros
		List<Animal> animals_in_range = this._region_mngr.get_animals_in_range(this, filter);
		this._danger_source = this._danger_strategy.select(this, animals_in_range);

	}

	private void updateMateState(double dt) {

		if (this._mate_target != null && (!this._mate_target.isAlive() || !this.isInRange(this._mate_target)))
			this._mate_target = null;

		if (this._mate_target == null) {
			if (!this.findMate()) {
				this.advance(dt);
			}
		} else {
			tryMate(dt);
		}
	}

	private void tryMate(double dt) {
		this._dest = this._mate_target.get_position();
		this.move(SPEED_PARAM * this._speed * dt * Math.exp((this._energy - MAX_PARAM) * ENERGY_PARAM));
		this._age += dt;
		this.addEnergy(-ENERGY_PARAM_2 * ENERGY_PARAM_3 * dt);
		this.addDesire(DESIRE_PARAM * dt);
		if (this.get_position().distanceTo(this._mate_target.get_position()) < OUT_PARAM) {
			mate();
		}
	}

	private void mate() {
		this._desire = MIN_PARAM;
		this._mate_target._desire = MIN_PARAM;
		if (this._baby == null) {
			if (Utils._rand.nextDouble(1) >= BABY_PROB_PARAM) {
				this._baby = new Sheep(this, this._mate_target);
			}
			this._mate_target = null;
		}
	}

	private void changeMateState() {
		if (this._danger_source == null)
			this.findDanger();
		if (this._danger_source != null)
			this.changeStateToDanger();
		else if (this._danger_source == null && this._desire < DESIRE_PARAM_2)
			this.changeStateToNormal();
	}

	private boolean findMate() {
		Predicate<Animal> filter = animal -> animal.get_genetic_code().equals(this.get_genetic_code())
				&& animal.get_state().equals(State.MATE);
		List<Animal> animals_in_range = new ArrayList<Animal>();
		animals_in_range = this._region_mngr.get_animals_in_range(this, filter);
		this._mate_target = this._mate_strategy.select(this, animals_in_range);
		if (this._mate_target != null)
			return true;
		else
			return false;
	}

	private void advance(double dt) {

		if (this.get_position().distanceTo(this._dest) < OUT_PARAM) {
			this._dest = Vector2D.get_random_vector(0, _region_mngr.get_height(), 0, _region_mngr.get_width());
		}

		this.move(this._speed * dt * Math.exp((this._energy - MAX_PARAM) * ENERGY_PARAM));
		this._age += dt;
		this.addEnergy(-dt * ENERGY_PARAM_2);
		this.addDesire(dt * DESIRE_PARAM);

	}

	@Override
	protected void changeStateToNormal() {
		super.changeStateToNormal();
		this._danger_source = null;
	}

	@Override
	protected void changeStateToDanger() {
		super.changeStateToDanger();
		this._mate_target = null;
	}

	@Override
	protected void changeStateToMate() {
		super.changeStateToMate();
		this._danger_source = null;
	}

	@Override
	protected void changeStateToHunger() {
		// No hace nada porque no tiene estado Hunger

	}

	@Override
	protected Sheep clone() {
		Sheep a = new Sheep(this._mate_strategy, this._danger_strategy, this.get_position());
		return a;
	}

	@Override
	protected double paramOld() {
		return DEAD_PARAM;
	}

	@Override
	protected void updateState(double dt) {
		if (this._state.equals(State.NORMAL)) {
			updateNormalState(dt);
			changeNormalState();
		} else if (this._state.equals(State.DANGER)) {
			updateDangerState(dt);
			changeDangerState();
		} else if (this._state.equals(State.MATE)) {
			updateMateState(dt);
			changeMateState();
		}
	}

}
