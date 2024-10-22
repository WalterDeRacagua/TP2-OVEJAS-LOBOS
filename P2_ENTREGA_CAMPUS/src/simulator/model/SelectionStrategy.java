package simulator.model;

import java.util.List;

public interface SelectionStrategy {
	Animal select(Animal a, List<Animal> as);

}
