package desarrolloInterfaces;

// ============================================================
// IMPORTS (qué es cada clase)
// ============================================================

// BasicStroke: define COMO es el trazo (grosor, extremos, uniones).
import java.awt.BasicStroke;
// BorderLayout: organiza la ventana en 5 zonas: NORTH, SOUTH, EAST, WEST, CENTER.
import java.awt.BorderLayout;
// Color: representa un color (Color.BLACK, new Color(r,g,b)...).
import java.awt.Color;
// Graphics: "pincel" basico que Swing da a un componente para pintar.
import java.awt.Graphics;
// Graphics2D: version mejorada de Graphics (grosor, antialiasing, formas...).
import java.awt.Graphics2D;
// RenderingHints: ajustes de calidad de dibujo (por ejemplo, antialiasing).
import java.awt.RenderingHints;
// ActionEvent: objeto con la informacion de un clic en boton/menu.
import java.awt.event.ActionEvent;
// ActionListener: interfaz con el metodo actionPerformed (clic en boton/menu).
import java.awt.event.ActionListener;
// MouseAdapter: implementa MouseListener con todos los metodos vacios.
// Solo sobrescribes los que necesitas (pressed, released...).
import java.awt.event.MouseAdapter;
// MouseEvent: objeto con la info del raton (getX(), getY()...).
import java.awt.event.MouseEvent;
// MouseMotionAdapter: igual que MouseAdapter pero para movimiento
// (mouseDragged y mouseMoved).
import java.awt.event.MouseMotionAdapter;
// BufferedImage: imagen en memoria sobre la que podemos dibujar.
// Guarda los pixeles de forma permanente.
import java.awt.image.BufferedImage;

// JButton: boton normal.
import javax.swing.JButton;
// JColorChooser: dialogo para elegir un color.
import javax.swing.JColorChooser;
// JFrame: ventana principal con barra de titulo, minimizar, cerrar...
import javax.swing.JFrame;
// JMenu: menu desplegable (ej: "Forma").
import javax.swing.JMenu;
// JMenuBar: barra de menus que va arriba de la ventana.
import javax.swing.JMenuBar;
// JMenuItem: cada opcion dentro de un menu.
import javax.swing.JMenuItem;
// JPanel: contenedor/zona rectangular. Aqui es nuestra zona de dibujo.
import javax.swing.JPanel;
// JSlider: barra deslizante para elegir un numero dentro de un rango.
import javax.swing.JSlider;
// JToolBar: barra de herramientas (botones agrupados).
import javax.swing.JToolBar;
// ChangeEvent: evento de "ha cambiado algo" (lo usa el JSlider).
import javax.swing.event.ChangeEvent;
// ChangeListener: interfaz con el metodo stateChanged (cambio en el slider).
import javax.swing.event.ChangeListener;

// JFrame es la ventana. Al heredar, Paint ES una ventana.
public class PaintExplicaciones extends JFrame {

	// ============================================================
	// ATRIBUTOS
	// ============================================================

	// Zona blanca donde el usuario dibuja.
	private JPanel panelDibujo;

	// Imagen en memoria donde queda guardado TODO lo dibujado.
	// Si dibujaramos directo en pantalla, se borraria al redimensionar.
	private BufferedImage lienzo;

	// Color actual del pincel.
	private Color color = Color.BLACK;

	// Grosor actual del pincel/goma en pixeles (lo cambia el slider).
	private int grosor = 3;

	// Herramienta activa. Valores posibles:
	// "LIBRE", "LINEA", "CIRCULO", "OVALO", "CUADRADO", "GOMA".
	private String forma = "LIBRE";

	// true solo mientras el boton del raton esta pulsado.
	// Sirve para mostrar la previsualizacion de la forma.
	private boolean arrastrando = false;

	// Punto donde empieza el trazo (donde se pulso el raton).
	private int xInicio;
	private int yInicio;

	// Posicion actual del raton mientras se arrastra.
	private int xActual;
	private int yActual;

	// ============================================================
	// CONSTRUCTOR
	// ============================================================

	public PaintExplicaciones() {
		// El orden importa: configurarRaton() necesita que panelDibujo ya
		// exista (si no, NullPointerException).
		crearVentana();
		crearPanelDibujo();
		crearMenu();
		crearBarraHerramientas();
		configurarRaton();
	}

	// ============================================================
	// VENTANA
	// ============================================================

	private void crearVentana() {
		// Texto de la barra de titulo.
		setTitle("Paint Basico Adrián Luque");
		// Tamano en pixeles (ancho, alto).
		setSize(1000, 600);
		// Al pulsar la X, termina el programa (si no, la ventana se cierra
		// pero el programa sigue ejecutandose).
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// null = centrar la ventana en la pantalla.
		setLocationRelativeTo(null);
		// BorderLayout: el CENTER ocupa todo el espacio sobrante.
		setLayout(new BorderLayout());
	}

	// ============================================================
	// PANEL DE DIBUJO
	// ============================================================

	private void crearPanelDibujo() {
		// Clase anonima que HEREDA de JPanel y sobrescribe paintComponent.
		// Al estar dentro de Paint, puede usar sus atributos (lienzo, forma...).
		panelDibujo = new JPanel() {

			// paintComponent lo llama Swing automaticamente cuando hay que
			// pintar el panel (al abrirlo, redimensionarlo o al llamar a
			// repaint()). NUNCA se llama directamente: se usa repaint().
			// "g" es el pincel que Swing nos da para este panel.
			@Override
			protected void paintComponent(Graphics g) {
				// Limpia el panel y pinta su fondo. Siempre va primero.
				super.paintComponent(g);

				// Garantiza que el lienzo existe y tiene tamano suficiente.
				asegurarLienzo();

				// Pinta el lienzo (todo lo dibujado) en la posicion (0,0).
				// El ultimo parametro (ImageObserver) no hace falta: null.
				g.drawImage(lienzo, 0, 0, null);

				// PREVISUALIZACION: mientras el usuario arrastra una forma
				// (linea, circulo...), la dibujamos encima del lienzo pero
				// SIN guardarla. Asi se "estira" sin dejar rastro.
				if (arrastrando && !esTrazoLibre()) {
					// g.create() = copia del Graphics, para no alterar el
					// original con nuestro color y grosor.
					Graphics2D g2 = (Graphics2D) g.create();
					configurarPincel(g2);
					dibujarForma(g2, xInicio, yInicio, xActual, yActual);
					// Libera la copia. Siempre dispose() tras create().
					g2.dispose();
				}
			}
		};

		panelDibujo.setBackground(Color.WHITE);
		// Lo colocamos en el centro de la ventana.
		add(panelDibujo, BorderLayout.CENTER);
	}

	// ============================================================
	// LIENZO (BufferedImage)
	// ============================================================

	// Crea el lienzo o lo agranda si el panel ha crecido.
	// Problema que resuelve: el tamano del panel no se conoce al crearlo
	// (vale 0) y puede cambiar si el usuario agranda la ventana.
	// El lienzo solo crece, nunca se encoge, y conserva lo dibujado.
	private void asegurarLienzo() {
		// Math.max(..., 1): un BufferedImage no puede medir 0 de ancho/alto.
		int w = Math.max(panelDibujo.getWidth(), 1);
		int h = Math.max(panelDibujo.getHeight(), 1);

		// Hay que crear uno nuevo si no existe o si el panel es mas grande.
		if (lienzo == null || lienzo.getWidth() < w || lienzo.getHeight() < h) {

			// Nuevo tamano: el mayor entre el panel y el lienzo actual.
			// (operador ternario: condicion ? siTrue : siFalse)
			int nuevoW = lienzo == null ? w : Math.max(w, lienzo.getWidth());
			int nuevoH = lienzo == null ? h : Math.max(h, lienzo.getHeight());

			// TYPE_INT_ARGB: cada pixel guarda transparencia + rojo + verde + azul.
			BufferedImage nuevo = new BufferedImage(nuevoW, nuevoH, BufferedImage.TYPE_INT_ARGB);

			// createGraphics() devuelve un Graphics2D para dibujar sobre la imagen.
			Graphics2D g2 = nuevo.createGraphics();

			// Una imagen nueva es transparente: la rellenamos de blanco.
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, nuevoW, nuevoH);

			// Si habia un lienzo antiguo, lo copiamos encima para no perder el dibujo.
			if (lienzo != null) {
				g2.drawImage(lienzo, 0, 0, null);
			}

			g2.dispose();
			lienzo = nuevo;
		}
	}

	// ============================================================
	// BORRAR TODO
	// ============================================================

	private void borrarTodo() {
		asegurarLienzo();
		Graphics2D g2 = lienzo.createGraphics();
		// Rellenamos todo el lienzo de blanco.
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, lienzo.getWidth(), lienzo.getHeight());
		g2.dispose();
		// repaint() es obligatorio: cambiar el lienzo no actualiza la
		// pantalla por si solo.
		panelDibujo.repaint();
	}

	// ============================================================
	// METODOS AUXILIARES DE DIBUJO
	// ============================================================

	// Devuelve true si la herramienta pinta de forma continua al arrastrar
	// (lapiz y goma). Las formas (linea, circulo...) devuelven false.
	// Se usa .equals() y no == para comparar Strings.
	private boolean esTrazoLibre() {
		return forma.equals("LIBRE") || forma.equals("GOMA");
	}

	// Configura el "pincel" (Graphics2D) antes de dibujar.
	private void configurarPincel(Graphics2D g2) {
		// Antialiasing: suaviza los bordes (evita el efecto "escalera").
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// La goma pinta en blanco (color del fondo); el resto usa el color elegido.
		g2.setColor(forma.equals("GOMA") ? Color.WHITE : color);

		// BasicStroke(grosor, extremos, uniones):
		// CAP_ROUND  = extremos de linea redondeados.
		// JOIN_ROUND = uniones entre segmentos redondeadas
		// (sin esto el trazo libre se ve "a trozos").
		g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}

	// Dibuja linea, circulo, ovalo o cuadrado entre dos puntos.
	// El usuario puede arrastrar en cualquier direccion, pero drawOval y
	// drawRect esperan la ESQUINA SUPERIOR IZQUIERDA y ancho/alto POSITIVOS.
	private void dibujarForma(Graphics2D g2, int x1, int y1, int x2, int y2) {
		// Math.abs = valor absoluto: ancho y alto siempre positivos.
		int ancho = Math.abs(x2 - x1);
		int alto = Math.abs(y2 - y1);

		// CIRCULO y CUADRADO necesitan ancho = alto: usamos el lado mayor.
		int lado = Math.max(ancho, alto);
		// Si arrastras a derecha/abajo, la esquina es el punto inicial.
		// Si arrastras a izquierda/arriba, hay que restar el lado.
		int xLado = x2 >= x1 ? x1 : x1 - lado;
		int yLado = y2 >= y1 ? y1 : y1 - lado;

		// OVALO: esquina superior izquierda real del rectangulo formado
		// por los dos puntos.
		int xCaja = Math.min(x1, x2);
		int yCaja = Math.min(y1, y2);

		// switch con Strings (Java 7+). Cada case necesita break.
		switch (forma) {
		case "LINEA":
			// drawLine(x1, y1, x2, y2): linea entre dos puntos.
			g2.drawLine(x1, y1, x2, y2);
			break;
		case "CIRCULO":
			// drawOval(x, y, ancho, alto): elipse inscrita en esa caja.
			// Con ancho = alto es un circulo.
			g2.drawOval(xLado, yLado, lado, lado);
			break;
		case "OVALO":
			g2.drawOval(xCaja, yCaja, ancho, alto);
			break;
		case "CUADRADO":
			// drawRect(x, y, ancho, alto): rectangulo (cuadrado si ancho = alto).
			// drawXxx dibuja solo el contorno; fillXxx rellena.
			g2.drawRect(xLado, yLado, lado, lado);
			break;
		}
	}

	// ============================================================
	// MENU
	// Estructura: JMenuBar -> JMenu -> JMenuItem
	// ============================================================

	private void crearMenu() {
		JMenuBar barraMenu = new JMenuBar();

		// ----- Menu Color -----
		JMenu menuColor = new JMenu("Color");
		JMenuItem cambiarColor = new JMenuItem("Cambiar color");

		// addActionListener registra QUE hacer al pulsar la opcion.
		// actionPerformed es el unico metodo de ActionListener.
		cambiarColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				elegirColor();
			}

		});

		menuColor.add(cambiarColor);

		// ----- Menu Forma -----
		JMenu menuForma = new JMenu("Forma");
		JMenuItem dibujoLibre = new JMenuItem("Dibujo libre");
		JMenuItem linea = new JMenuItem("Línea recta");
		JMenuItem circulo = new JMenuItem("Círculo");
		JMenuItem ovalo = new JMenuItem("Óvalo");
		JMenuItem cuadrado = new JMenuItem("Cuadrado");
		JMenuItem goma = new JMenuItem("Goma de borrar");

		// Estas opciones NO dibujan: solo cambian la herramienta activa.
		// El dibujo ocurre en los eventos del raton.
		dibujoLibre.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "LIBRE";
			}

		});

		linea.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "LINEA";
			}

		});

		circulo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "CIRCULO";
			}

		});

		ovalo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "OVALO";
			}

		});

		cuadrado.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "CUADRADO";
			}

		});

		goma.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "GOMA";
			}

		});

		menuForma.add(dibujoLibre);
		menuForma.add(linea);
		menuForma.add(circulo);
		menuForma.add(ovalo);
		menuForma.add(cuadrado);
		menuForma.add(goma);

		// ----- Menu Editar -----
		JMenu menuEditar = new JMenu("Editar");
		// Se llama itemBorrarTodo para no confundirse con el metodo borrarTodo().
		JMenuItem itemBorrarTodo = new JMenuItem("Borrar todo");

		itemBorrarTodo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				borrarTodo();
			}

		});

		menuEditar.add(itemBorrarTodo);

		// Metemos los menus en la barra y la barra en la ventana.
		barraMenu.add(menuColor);
		barraMenu.add(menuForma);
		barraMenu.add(menuEditar);

		setJMenuBar(barraMenu);
	}

	// ============================================================
	// BARRA DE HERRAMIENTAS
	// ============================================================

	private void crearBarraHerramientas() {
		JToolBar barraHerramientas = new JToolBar();
		JButton botonLibre = new JButton("Libre");
		JButton botonLinea = new JButton("Línea");
		JButton botonCirculo = new JButton("Círculo");
		JButton botonOvalo = new JButton("Óvalo");
		JButton botonCuadrado = new JButton("Cuadrado");
		JButton botonGoma = new JButton("Borrar");
		JButton botonBorrarTodo = new JButton("Borrar todo");
		JButton botonColor = new JButton("Color");

		botonLibre.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "LIBRE";
			}

		});

		botonLinea.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "LINEA";
			}

		});

		botonCirculo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "CIRCULO";
			}

		});

		botonOvalo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "OVALO";
			}

		});

		botonCuadrado.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "CUADRADO";
			}

		});

		botonGoma.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forma = "GOMA";
			}

		});

		botonBorrarTodo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				borrarTodo();
			}

		});

		botonColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				elegirColor();
			}

		});

		// JSlider(minimo, maximo, valorInicial).
		// final: una clase anonima solo puede usar variables locales del
		// metodo si son final (o "effectively final").
		final JSlider sliderGrosor = new JSlider(1, 20, 3);
		// Texto que aparece al dejar el raton encima.
		sliderGrosor.setToolTipText("Ancho de la brocha / goma");

		// El slider usa ChangeListener (no ActionListener): emite eventos de
		// CAMBIO. stateChanged se dispara cada vez que lo mueves.
		sliderGrosor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// getValue() = valor actual del slider.
				grosor = sliderGrosor.getValue();
			}

		});

		barraHerramientas.add(botonLibre);
		barraHerramientas.add(botonLinea);
		barraHerramientas.add(botonCirculo);
		barraHerramientas.add(botonOvalo);
		barraHerramientas.add(botonCuadrado);
		barraHerramientas.add(botonGoma);
		barraHerramientas.add(botonBorrarTodo);
		barraHerramientas.add(botonColor);
		barraHerramientas.add(sliderGrosor);

		// La barra va en la zona superior de la ventana.
		add(barraHerramientas, BorderLayout.NORTH);
	}

	// ============================================================
	// ELEGIR COLOR
	// Metodo aparte porque se usa desde el menu y desde el boton
	// (asi no se repite codigo).
	// ============================================================

	private void elegirColor() {
		// showDialog(padre, titulo, colorInicial) abre el dialogo y
		// DEVUELVE el color elegido, o null si se cancela.
		// Paint.this = la ventana exterior. Dentro de una clase anonima,
		// "this" seria la clase anonima, no la ventana.
		Color nuevoColor = JColorChooser.showDialog(PaintExplicaciones.this, "Seleccionar color", color);

		// Si se cancela devuelve null, por eso hay que comprobarlo.
		if (nuevoColor != null) {
			color = nuevoColor;
		}
	}

	// ============================================================
	// RATON
	// Dos listeners porque son dos tipos de eventos:
	// MouseListener       -> botones (pressed, released, clicked, entered, exited)
	// MouseMotionListener -> movimiento (dragged = con boton pulsado,
	//                        moved = sin pulsar)
	// ============================================================

	private void configurarRaton() {
		panelDibujo.addMouseListener(new MouseAdapter() {

			// Se ejecuta al PULSAR el boton del raton.
			@Override
			public void mousePressed(MouseEvent e) {
				// e.getX() y e.getY() = posicion del raton RELATIVA al panel.
				xInicio = e.getX();
				yInicio = e.getY();
				xActual = e.getX();
				yActual = e.getY();
				// Activamos la bandera: empieza el arrastre.
				arrastrando = true;
			}

			// Se ejecuta al SOLTAR el boton del raton.
			@Override
			public void mouseReleased(MouseEvent e) {
				xActual = e.getX();
				yActual = e.getY();
				arrastrando = false;

				// Si es una forma, ahora SI se dibuja definitivamente en el lienzo.
				// (El lapiz y la goma ya dibujaron mientras arrastrabas.)
				if (!esTrazoLibre()) {
					asegurarLienzo();
					Graphics2D g2 = lienzo.createGraphics();
					configurarPincel(g2);
					dibujarForma(g2, xInicio, yInicio, xActual, yActual);
					g2.dispose();
				}

				panelDibujo.repaint();
			}
		});

		panelDibujo.addMouseMotionListener(new MouseMotionAdapter() {

			// Se ejecuta al MOVER el raton con el boton PULSADO.
			@Override
			public void mouseDragged(MouseEvent e) {
				xActual = e.getX();
				yActual = e.getY();

				if (esTrazoLibre()) {
					// Lapiz y goma: dibujan una linea pequena desde el punto
					// anterior hasta el actual, directamente en el lienzo.
					asegurarLienzo();
					Graphics2D g2 = lienzo.createGraphics();
					configurarPincel(g2);
					g2.drawLine(xInicio, yInicio, xActual, yActual);
					g2.dispose();

					// El punto actual pasa a ser el nuevo inicio, asi el
					// trazo se forma con muchos segmentitos encadenados.
					xInicio = xActual;
					yInicio = yActual;
				}
				// Para las formas NO se toca xInicio: la forma parte siempre
				// del punto donde se pulso. La previsualizacion la dibuja
				// paintComponent.

				// Pedimos repintar para ver el cambio.
				panelDibujo.repaint();
			}
		});
	}

	// ============================================================
	// MAIN
	// ============================================================

	public static void main(String[] args) {
		PaintExplicaciones ventana = new PaintExplicaciones();
		// Sin setVisible(true) la ventana no se ve.
		ventana.setVisible(true);
	}

}