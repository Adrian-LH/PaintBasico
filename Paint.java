package desarrolloInterfaces;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Paint extends JFrame {

	// ATRIBUTOS

	private JPanel panelDibujo;
	private BufferedImage lienzo;
	private Color color = Color.BLACK;
	private int grosor = 3;
	private String forma = "LIBRE";
	private boolean arrastrando = false;

	private int xInicio;
	private int yInicio;

	private int xActual;
	private int yActual;

	// CONSTRUCTOR

	public Paint() {
		crearVentana();
		crearPanelDibujo();
		crearMenu();
		crearBarraHerramientas();
		configurarRaton();
	}

	// METODO PARA CREAR LA VENTANA

	private void crearVentana() {
		setTitle("Paint Basico Adrián Luque");
		setSize(1000, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
	}

	// PANEL DE DIBUJO

	private void crearPanelDibujo() {
		panelDibujo = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				asegurarLienzo();
				g.drawImage(lienzo, 0, 0, null);

				// Previsualizacion mientras se arrastra (solo formas)
				if (arrastrando && !esTrazoLibre()) {
					Graphics2D g2 = (Graphics2D) g.create();
					configurarPincel(g2);
					dibujarForma(g2, xInicio, yInicio, xActual, yActual);
					g2.dispose();
				}
			}
		};
		panelDibujo.setBackground(Color.WHITE);
		add(panelDibujo, BorderLayout.CENTER);
	}

	// LIENZO

	private void asegurarLienzo() {
		int w = Math.max(panelDibujo.getWidth(), 1);
		int h = Math.max(panelDibujo.getHeight(), 1);

		if (lienzo == null || lienzo.getWidth() < w || lienzo.getHeight() < h) {
			int nuevoW = lienzo == null ? w : Math.max(w, lienzo.getWidth());
			int nuevoH = lienzo == null ? h : Math.max(h, lienzo.getHeight());

			BufferedImage nuevo = new BufferedImage(nuevoW, nuevoH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = nuevo.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, nuevoW, nuevoH);
			if (lienzo != null) {
				g2.drawImage(lienzo, 0, 0, null);
			}
			g2.dispose();
			lienzo = nuevo;
		}
	}

	// BORRAR TODO

	private void borrarTodo() {
		asegurarLienzo();
		Graphics2D g2 = lienzo.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, lienzo.getWidth(), lienzo.getHeight());
		g2.dispose();
		panelDibujo.repaint();
	}

	// Libre y goma dibujan trazo continuo; el resto son formas

	private boolean esTrazoLibre() {
		return forma.equals("LIBRE") || forma.equals("GOMA");
	}

	private void configurarPincel(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// La goma pinta de blanco (color del fondo)
		g2.setColor(forma.equals("GOMA") ? Color.WHITE : color);
		g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}

	// Dibuja linea, circulo, ovalo o cuadrado entre dos puntos

	private void dibujarForma(Graphics2D g2, int x1, int y1, int x2, int y2) {
		int ancho = Math.abs(x2 - x1);
		int alto = Math.abs(y2 - y1);

		// Para circulo y cuadrado: lado igual, respetando la direccion del arrastre
		int lado = Math.max(ancho, alto);
		int xLado = x2 >= x1 ? x1 : x1 - lado;
		int yLado = y2 >= y1 ? y1 : y1 - lado;

		// Para ovalo: caja entre los dos puntos
		int xCaja = Math.min(x1, x2);
		int yCaja = Math.min(y1, y2);

		switch (forma) {
		case "LINEA":
			g2.drawLine(x1, y1, x2, y2);
			break;
		case "CIRCULO":
			g2.drawOval(xLado, yLado, lado, lado);
			break;
		case "OVALO":
			g2.drawOval(xCaja, yCaja, ancho, alto);
			break;
		case "CUADRADO":
			g2.drawRect(xLado, yLado, lado, lado);
			break;
		}
	}

	// MENU

	private void crearMenu() {
		JMenuBar barraMenu = new JMenuBar();

		JMenu menuColor = new JMenu("Color");
		JMenuItem cambiarColor = new JMenuItem("Cambiar color");

		cambiarColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				elegirColor();
			}

		});

		menuColor.add(cambiarColor);

		JMenu menuForma = new JMenu("Forma");
		JMenuItem dibujoLibre = new JMenuItem("Dibujo libre");
		JMenuItem linea = new JMenuItem("Línea recta");
		JMenuItem circulo = new JMenuItem("Círculo");
		JMenuItem ovalo = new JMenuItem("Óvalo");
		JMenuItem cuadrado = new JMenuItem("Cuadrado");
		JMenuItem goma = new JMenuItem("Goma de borrar");

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

		JMenu menuEditar = new JMenu("Editar");
		JMenuItem itemBorrarTodo = new JMenuItem("Borrar todo");

		itemBorrarTodo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				borrarTodo();
			}

		});

		menuEditar.add(itemBorrarTodo);

		barraMenu.add(menuColor);
		barraMenu.add(menuForma);
		barraMenu.add(menuEditar);

		setJMenuBar(barraMenu);
	}

	// BARRA DE HERRAMIENTAS

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

		final JSlider sliderGrosor = new JSlider(1, 20, 3);
		sliderGrosor.setToolTipText("Ancho de la brocha / goma");

		sliderGrosor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
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

		add(barraHerramientas, BorderLayout.NORTH);
	}

	private void elegirColor() {
		Color nuevoColor = JColorChooser.showDialog(Paint.this, "Seleccionar color", color);
		if (nuevoColor != null) {
			color = nuevoColor;
		}
	}

	// RATON

	private void configurarRaton() {
		panelDibujo.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				xInicio = e.getX();
				yInicio = e.getY();
				xActual = e.getX();
				yActual = e.getY();
				arrastrando = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				xActual = e.getX();
				yActual = e.getY();
				arrastrando = false;

				// Al soltar, la forma se dibuja de forma definitiva en el lienzo
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

			@Override
			public void mouseDragged(MouseEvent e) {
				xActual = e.getX();
				yActual = e.getY();

				if (esTrazoLibre()) {
					// Dibujo libre y goma: se pinta directamente sobre el lienzo
					asegurarLienzo();
					Graphics2D g2 = lienzo.createGraphics();
					configurarPincel(g2);
					g2.drawLine(xInicio, yInicio, xActual, yActual);
					g2.dispose();

					xInicio = xActual;
					yInicio = yActual;
				}

				panelDibujo.repaint();
			}
		});
	}

	public static void main(String[] args) {
		Paint ventana = new Paint();
		ventana.setVisible(true);
	}

}