package simulator.view;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	private int _width;
	private int _height;
	private int _rows;
	private int _cols;
	private int _rwidth;
	private int _rheight;

	// Mostramos sólo animales con este estado. Los posibles valores de _currState
	// son null, y los valores deAnimal.State.values(). Si es null mostramos todo.
	private State _currState;

	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<AnimalInfo> _objs;
	volatile private Double _time;

	// Una clase auxilar para almacenar información sobre una especie
	private static class SpeciesInfo {
		private Integer _count;
		private Color _color;

		SpeciesInfo(Color color) {
			_count = 0;
			_color = color;
		}
	}

	// Un mapa para la información sobre las especies
	private Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto
	private Font _font = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no
	private boolean _showHelp;
	// h: toggle help
	// s: show animals of a specific state
	private static final String SHOW_H_HELP = "h: toggle help";
	private static final String SHOW_S_HELP = "s: show animals of a specific state";

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					_showHelp = !_showHelp;
					repaint();
					break;
				case 's':
					MapViewer.this.changeCurrState();
					repaint();
				default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // Esto es necesario para capturar las teclas cuando el ratón está sobre este
								// componente.
			}
		});

		// Por defecto mostramos todos los animales
		_currState = null;

		// Por defecto mostramos el texto de ayuda
		_showHelp = true;
	}

	private void changeCurrState() {
		if (_currState == null)
			_currState = State.values()[0];
		else
			_currState = _currState.nextValue();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto
		g.setFont(_font);

		// Dibujar fondo blanco
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, _width, _height);

		// Dibujar los animales, el tiempo, etc.
		if (_objs != null)
			drawObjects(gr, _objs, _time);

		// Mostrar el texto de ayuda si _showHelp es true. El texto a mostrar es el
		// siguiente (en 2 líneas):
		// h: toggle help
		// s: show animals of a specific state
		if (_showHelp) {
			if (_cols != 0) {
				g.setColor(Color.red);
				g.drawString(SHOW_H_HELP, _width / _cols, _height / _rows);
				g.drawString(SHOW_S_HELP, _width / _cols, _height / _rows * 2);
			}
		}

	}

	private boolean visible(AnimalInfo a) {
		// Devolver true si el animal es visible, es decir si _currState es null o su
		// estado es igual a _currState.
		return _currState == null || _currState.equals(a.get_state());
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

		// Dibujar el grid de regiones
		drawHorizontalLines(g);
		drawVerticalLines(g);

		// Dibujar los animales
		for (AnimalInfo a : animals) {

			// Si no es visible saltamos la iteración
			if (!visible(a))
				continue;

			// La información sobre la especie de 'a'
			SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());

			// Si esp_info es null, añade una entrada correspondiente al mapa. Para el color
			// usa ViewUtils.get_color(a.get_genetic_code())
			if (esp_info == null) {
				esp_info = new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code()));
				_kindsInfo.put(a.get_genetic_code(), esp_info);

			}

			// Incrementar el contador de la especie (es decir el contador dentro de
			// tag_info)
			esp_info._count++;

			// Dibujar el animal en la posicion correspondiente, usando el color
			// tag_info._color. Su tamaño tiene que ser relativo a su edad, por ejemplo
			// edad/2+2. Se puede dibujar usando fillRoundRect, fillRect o fillOval.
			g.setColor(esp_info._color);
			g.fillRoundRect((int) a.get_position().getX(), (int) a.get_position().getY(), (int) a.get_age() * (2),
					(int) a.get_age() * (2), (int) a.get_age(), (int) a.get_age());
//			g.fillRoundRect((int)a.get_position().getX(), (int)a.get_position().getY(), (int)a.get_age(), (int)a.get_age(), (int)a.get_age()/(2), (int)a.get_age()/(2));
		}

		// Dibujar la etiqueta del estado visible, sin no es null.
		if (_currState != null) {
			g.setColor(ViewUtils.get_color(_currState));
			this.drawStringWithRect(g, _width / _cols, _height / _rows * (_rows - 1),
					"State: " + _currState.toString());
		}
		// Dibujar la etiqueta del tiempo. Para escribir solo 3 decimales puede usar
		// String.format("%.3f", time)
		g.setColor(Color.MAGENTA);
		this.drawStringWithRect(g, _width / _cols, _height / _rows * (_rows - 2), String.format("Time: %.3f", _time));

		// Dibujar la información de todas la especies. Al final de cada iteración poner
		// el contador de la especie correspondiente a 0 (para resetear el cuento)
		int y = -3;
		for (Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {
			g.setColor(e.getValue()._color);
			this.drawStringWithRect(g, _width / _cols, _height / _rows * (_rows + y),
					e.getKey() + ": " + e.getValue()._count);
			y--;
			SpeciesInfo info = e.getValue();
			info._count = 0;
			e.setValue(info);
		}
	}

	private void drawVerticalLines(Graphics2D g) {
		for (int i = 0; i < _cols; i++) {
			g.drawLine(i * _rwidth, 0, i * _rwidth, _height);
		}
	}

	private void drawHorizontalLines(Graphics2D g) {
		for (int i = 0; i < _rows; i++) {
			g.drawLine(0, i * _rheight, _width, i * _rheight);
		}

	}

	// Un método que dibujar un texto con un rectángulo
	private void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		_objs = objs;
		_time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		_width = map.get_width();
		_height = map.get_height();
		_cols = map.get_cols();
		_rows = map.get_rows();
		_rwidth = map.get_region_width();
		_rheight = map.get_region_height();
		// Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
		// porque en MapWindow llamamos a pack() después de llamar a reset
		setPreferredSize(new Dimension(map.get_width(), map.get_height()));
		update(animals, time);
	}

}
