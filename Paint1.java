package desarrolloInterfaces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Paint1 extends JFrame {
	
	//	Atributos
	
	private JPanel panelDibujo; //el panel blanco donde se dibuja
	private BufferedImage lienzo;
	private int grosor = 3;
	private Color color = Color.BLACK;
	private String forma = "LIBRE";
	private boolean arrastrando;
	
	// Atributos coords
	
	private int xInicio;
	private int yInicio;
	
	private int xActual;
	private int yActual;

	//	Constructor
	
	public Paint1() {
		crearVentana();
		crearPanelDibujo();
		crearMenu();
		crearBarraHerramientas();
		configurarRaton();
	}

	//	Metodos
	
	//ventana
	

	private void crearVentana() {
		setTitle("PAINT BASICO");
		setSize(900,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		
	}

	private void crearPanel() {
		panelDibujo = new JPanel() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				asegurarLienzo();
				g.drawImage(lienzo, 0, 0, null);
			
				if(arrastrando && !esTrazoLibre()) {
					Graphics2D g2 = (Graphics2D) g.create();
					configurarPincel(g2);
					configurarFormas(g2, xInicio, yInicio, xActual, yActual);
					g2.dispose();
				}
			}
		};
		panelDibujo.setBackground(Color.WHITE);
		add(panelDibujo, BorderLayout.CENTER);
	}
	
	private void asegurarLienzo() {
		int w = Math.max(panelDibujo.getWidth(), 1);
		int h = Math.max(panelDibujo.getHeight(), 1);
		
		if(lienzo == null || lienzo.getWidth()< w || lienzo.getHeight() < h) {
			int nuevoW = lienzo == null ? w : Math.max(w, lienzo.getWidth());
			int nuevoH = lienzo == null ? h : Math.max(h, lienzo.getHeight());
			
			BufferedImage nuevo = new BufferedImage(nuevoH, nuevoW, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = nuevo.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, nuevoW, nuevoH);
			if(lienzo != null) {
				g2.drawImage(lienzo, 0, 0, null);
			}
			g2.dispose();
			lienzo = nuevo;
		}
		
	}
	
	private void configurarFormas(Graphics2D g2, int xInicio, int yInicio, int xActual, int yActual) {
		// TODO Auto-generated method stub
		
	}
	
	private void configurarPincel(Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}
	
	
	private boolean esTrazoLibre() {
		// TODO Auto-generated method stub
		return false;
	}
	private void crearMenu() {
		// TODO Auto-generated method stub
		
	}

	private void crearBarraHerramientas() {
		// TODO Auto-generated method stub
		
	}

	private void configurarRaton() {
		// TODO Auto-generated method stub
		
	}
	
	


	//	Main

	public static void main(String[]args) {
		Paint1 ventana = new Paint1();
		ventana.setVisible(true);
	}


}
