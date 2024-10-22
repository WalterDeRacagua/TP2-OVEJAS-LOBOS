package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
			return null;
		} else {
			Animal closest = null;
			double minimun = a.get_sight_range();
			for (Animal an : as) {

				if (a.get_position().distanceTo(an.get_position()) < minimun) {
					minimun = a.get_position().distanceTo(an.get_position());
					closest = an;
				}
			}
			return closest;
		}
	}

}
