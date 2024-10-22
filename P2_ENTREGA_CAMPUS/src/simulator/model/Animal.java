package simulator.model;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

	private static final double SPEED_PARAM = 0.1;
	private static final double INITIAL_ENERGY = 100.0;
	private static final double PARAM_POS = 60.0;
	private static final double MUTATION_PARAM = 0.2;

	private static final double MAX_ENERGY = 100.0;
	private static final double MIN_ENERGY = 0.0;
	private static final double MIN_DESIRE = 0.0;
	private static final double MAX_DESIRE = 100.0;

	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {

		this._genetic_code = genetic_code;
		this._diet = diet;
		this._sight_range = sight_range;
		this._mate_strategy = mate_strategy;
		this._pos = pos;
		this._speed = Utils.get_randomized_parameter(init_speed, SPEED_PARAM);
		this._energy = INITIAL_ENERGY;
		this._desire = 0.0;
		this._dest = null;
		this._baby = null;
		this._region_mngr = null;
		this._state = State.NORMAL;

	}

	protected Animal(Animal p1, Animal p2) {
		this._state = State.NORMAL;
		this._genetic_code = p1._genetic_code;
		this._diet = p1._diet;
		this._energy = (p1._energy + p2._energy) / 2;
		this._pos = this.randomPos(p1, p2);
		this._sight_range = this.sightRangeMutation(p1, p2);
		this._speed = this.speedMutation(p1, p2);
		this._desire = 0.0;
		this._region_mngr = p1._region_mngr;
		init_dest();
		this._baby = null;
	}

	protected abstract void updateState(double dt);

	protected abstract double paramOld();

	private void init_dest() {
		this._dest = Vector2D.get_random_vector(0, _region_mngr.get_height(), 0, _region_mngr.get_width());
	}

	private Vector2D randomPos(Animal p1, Animal p2) {

		return p1.get_position()
				.plus(Vector2D.get_random_vector(-1, 1).scale(PARAM_POS * (Utils._rand.nextGaussian() + 1)));
	}

	private double sightRangeMutation(Animal p1, Animal p2) {

		return Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2, MUTATION_PARAM);
	}

	private double speedMutation(Animal p1, Animal p2) {

		return Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, MUTATION_PARAM);
	}

	public void init(AnimalMapView reg_mngr) {
		this._region_mngr = reg_mngr;
		if (this._pos == null) {
			this._pos = Vector2D.get_random_vector(0, _region_mngr.get_width(), 0, _region_mngr.get_height());
		} else if (this._pos != null) {
			this._pos.adjustPosition(this._region_mngr.get_width(), this._region_mngr.get_height());
		}

		this._dest = Vector2D.get_random_vector(0, _region_mngr.get_width(), 0, _region_mngr.get_height());

	}

	protected Animal deliver_baby() {
		Animal baby;
		try {
			baby = (Animal) this._baby.clone();
			set_pregnant();
			return baby;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	protected void move(double speed) {

		this._pos = this._pos.plus(this._dest.minus(this._pos).direction().scale(speed));
	}

	private void set_pregnant() {
		this._baby = null;
	}

	protected Boolean isAlive() {
		return !this._state.equals(State.DEAD);
	}

	protected Boolean notEnergy() {
		return this._energy <= 0.0;
	}

	protected void resetDesire() {
		this._desire = 0.0;
	}

	protected boolean isInRange(Animal a) {

		return a.get_position().distanceTo(this._pos) <= this._sight_range;
	}

	protected void requestFood(double food) {

		this.addEnergy(food);
	}

	protected void addEnergy(double factor) {

		this._energy += factor;
		if (this.notEnergy())
			this._energy = MIN_ENERGY;
		else if (this._energy > MAX_ENERGY)
			this._energy = MAX_ENERGY;

	}

	protected void addDesire(double dt) {
		this._desire += dt;
		if (this._desire < MIN_DESIRE)
			this._desire = MIN_DESIRE;
		else if (this._desire > MAX_DESIRE)
			this._desire = MAX_DESIRE;
	}

	protected void updatePosition() {
		if (this._pos.isOut(this._region_mngr.get_width(), this._region_mngr.get_height())) {
			this._pos.adjustPosition(this._region_mngr.get_width(), this._region_mngr.get_height());
			this.changeStateToNormal();
		}
	}

	protected void changeStateToNormal() {
		this._mate_target = null;
		this._state = State.NORMAL;
	}

	protected void changeStateToMate() {
		this._state = State.MATE;
	}

	protected void changeStateToDanger() {
		this._state = State.DANGER;
	}

	protected void changeStateToHunger() {
		this._state = State.HUNGER;
	}

	protected Boolean isOld() {
		return this._age > paramOld();
	}

	protected void mostrarAtributos() {
		/*
		 * atributos para depuracion
		 */
		System.err.println("codigo genetico: " + this._genetic_code);
		System.err.println("estado: " + this._state.name());
		System.err.println("energia: " + this._energy);
		System.err.println("edad: " + this._age);
		System.err.println("campo visual: " + this._sight_range);
		System.err.println("dieta: " + this._diet.name());
		System.err.println("destino: " + this._dest.toString());
		System.err.println("distanci destino: " + this._dest.distanceTo(_pos));
		System.err.println("deseo: " + this._desire);
		System.err.println("velocidad: " + this._speed);
		System.err.println("-------------------");
	}

	@Override
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(this._pos.getX());
		ja.put(this._pos.getY());
		jo.put("pos", ja);
		jo.put("gcode", this._genetic_code);
		jo.put("diet", this._diet.name());
		jo.put("state", this._state.name());
		return jo;
	}

	@Override
	public State get_state() {
		return this._state;
	}

	@Override
	public Vector2D get_position() {
		return this._pos;
	}

	@Override
	public String get_genetic_code() {
		return this._genetic_code;
	}

	@Override
	public Diet get_diet() {
		return this._diet;
	}

	@Override
	public double get_speed() {
		return this._speed;
	}

	@Override
	public double get_sight_range() {
		return this._sight_range;
	}

	@Override
	public double get_energy() {
		return this._energy;
	}

	@Override
	public double get_age() {
		return this._age;
	}

	@Override
	public Vector2D get_destination() {
		return this._dest;
	}

	@Override
	public boolean is_pregnant() {
		if (this._baby == null) {
			return false;
		} else
			return true;

	}

	@Override
	public void update(double dt) {
		if (isAlive()) {
			updateState(dt);
			updatePosition();
			if (notEnergy() || isOld()) {
				this._state = State.DEAD;
			}
			if (isAlive()) {
				requestFood(_region_mngr.get_food(this, dt));
			}
		}
	}
}
