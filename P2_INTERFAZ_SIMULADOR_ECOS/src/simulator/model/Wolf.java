package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import java.util.List;
import java.util.function.Predicate;
import java.util.ArrayList;

public class Wolf extends Animal {

	private static final double INITIAL_SIGHT = 50.0;
	private static final double INITIAL_SPEED = 60.0;
	private static final double DEAD_PARAM = 14.0;
	private static final double MAX_ENERGY = 100.0;
	private static final double PARAM_SPEED = 0.007;
	private static final double PARAM_ENERGY = 18.0;
	private static final double PARAM_ENERGY_2 = 1.2;
	private static final double PARAM_ENERGY_TO_MATE = -10.0;
	private static final double PARAM_DESIRE = 30.0;
	private static final double PARAM_DISTANCE = 8.0;
	private static final double SPEED_PARAM_WOLF_RAPID = 3.0;
	private static final double SPEED_PARAM_WOLF_NORMAL = 1.0;

	private static final String GENETIC_CODE = "Wolf";

	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super(GENETIC_CODE, Diet.CARNIVORE, INITIAL_SIGHT, INITIAL_SPEED, mate_strategy, pos);
		this._hunting_strategy = hunting_strategy;
		this._hunt_target = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunt_target = null;
		this._hunting_strategy = p1._hunting_strategy;
		this._mate_strategy = p1._mate_strategy;
	}

	private void avanzaPosition(double dt) {
		if (this._dest.distanceTo(_pos) < PARAM_DISTANCE) {
			this._dest = Vector2D.get_random_vector(0, _region_mngr.get_height(), 0, _region_mngr.get_width());
		}
		this.moveWolf(dt, SPEED_PARAM_WOLF_NORMAL);
		this._age += dt;
		addEnergy(-PARAM_ENERGY * dt);
		addDesire(PARAM_DESIRE * dt);
	}

	private void updateHungerState(double dt) {
		if (this._hunt_target == null || !this._hunt_target.isAlive() || !isInRange(this._hunt_target)) {
			searchOtherToHunt();
		}
		if (this._hunt_target == null)
			avanzaPosition(dt);
		else
			tryHunt(dt);
	}

	private void searchOtherToHunt() {
		Predicate<Animal> filter = animal -> !animal.get_diet().equals(this._diet);
		List<Animal> animals_in_range = new ArrayList<Animal>();
		animals_in_range = this._region_mngr.get_animals_in_range(this, filter);
		this._hunt_target = this._hunting_strategy.select(this, animals_in_range);
	}

	private void tryHunt(double dt) {
		changeDest(this._hunt_target.get_position());
		this.moveWolf(dt, SPEED_PARAM_WOLF_RAPID);
		this._age += dt;
		addEnergy(-PARAM_ENERGY * PARAM_ENERGY_2 * dt);
		addDesire(PARAM_DESIRE * dt);
		if (isReachable(this._hunt_target))
			hunt();
	}

	private void hunt() {
		this._hunt_target._state = State.DEAD;
		this._hunt_target = null;
		addEnergy(50.0);
	}

	private void updateMateState(double dt) {
		if (this._mate_target != null
				&& (this._mate_target.get_state().equals(State.DEAD) || !isInRange(this._mate_target))) {
			this._mate_target = null;
		}
		if (this._mate_target == null) {
			if (!searchOtherToMate()) {
				avanzaPosition(dt);
			}
		} else {
			tryToMate(dt);
		}
	}

	private boolean searchOtherToMate() {
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

	private void tryToMate(double dt) {
		changeDest(this._mate_target.get_position());
		this.moveWolf(dt, SPEED_PARAM_WOLF_RAPID);
		this._age += dt;
		addEnergy(-PARAM_ENERGY * PARAM_ENERGY_2 * dt);
		addDesire(PARAM_DESIRE * dt);
		if (isReachable(this._mate_target))
			mate();
	}

	private void changeDest(Vector2D pos) {
		this._dest = pos;
	}

	private boolean isReachable(Animal a) {
		if (this._pos.distanceTo(a.get_position()) < PARAM_DISTANCE)
			return true;
		else
			return false;

	}

	private void mate() {
		this.resetDesire();
		this._mate_target.resetDesire();
		if (this._baby == null) {
			if (Utils._rand.nextDouble(1) >= 0.10) {
				this._baby = new Wolf(this, _mate_target);
			}
			addEnergy(PARAM_ENERGY_TO_MATE);
			this._mate_target = null;
		}
	}

	private void moveWolf(double dt, double in_speed) {
		this.move(in_speed * this._speed * dt * Math.exp((this._energy - MAX_ENERGY) * PARAM_SPEED));
	}

	private void changeNormalState() {
		if (this._energy < 50.0)
			this.changeStateToHunger();
		else if (this._desire > 65.0)
			this.changeStateToMate();
	}

	private void changeHungerState() {
		if (this._energy > 50.0) {
			if (this._desire > 65.0)
				this.changeStateToMate();
			else
				this.changeStateToNormal();
		}
	}

	private void changeMateState() {
		if (this._energy < 50.0)
			this.changeStateToHunger();
		else if (this._desire < 65.0)
			this.changeStateToNormal();
	}

	@Override
	protected void updateState(double dt) {
		if (this._state.equals(State.NORMAL)) {
			avanzaPosition(dt);
			changeNormalState();
		} else if (this._state.equals(State.HUNGER)) {
			updateHungerState(dt);
			changeHungerState();
		} else if (this._state.equals(State.MATE)) {
			updateMateState(dt);
			changeMateState();
		}
	}

	@Override
	protected double paramOld() {
		return DEAD_PARAM;
	}

	@Override
	protected void changeStateToNormal() {
		super.changeStateToNormal();
		this._hunt_target = null;

	}

	@Override
	protected void changeStateToMate() {
		super.changeStateToMate();
		this._hunt_target = null;
	}

	@Override
	protected void changeStateToDanger() {
		// No puede estar en peligro de momento
	}

	@Override
	protected void changeStateToHunger() {
		super.changeStateToHunger();
		this._mate_target = null;
	}

	@Override
	protected Wolf clone() {
		Wolf a = new Wolf(this._mate_strategy, this._hunting_strategy, this._pos);
		return a;
	}

}
