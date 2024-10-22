package simulator.model;

public enum State {
	NORMAL, MATE, HUNGER, DANGER, DEAD;

	public State nextValue() {
		int next = this.ordinal() + 1;
		if (next == State.values().length) {
			return null;
		} else
			return State.values()[next];

	}
}
