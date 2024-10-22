package simulator.model;

import java.util.List;

public class SelectOldest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {

		if (as.isEmpty()) {
			return null;

		} else {
			Animal oldest = as.get(0);

			for (Animal animal : as) {
				if (animal.get_age() > oldest.get_age()) {
					oldest = animal;
				}
			}

			return oldest;
		}
	}

}
