package main;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;

public class PanelReceptor extends JPanel {
	private static final long serialVersionUID = -7652072306991442398L;
	private int lastX = -1, lastY = -1;

	public PanelReceptor(BufferedReader entrada, CountDownLatch latch) {
		// Crear un hilo para recibir los puntos desde el servidor
		// Necesario para evitar bloquear la interfaz grafica
		new Thread(() -> {
			try {
				String mensaje;
				while ((mensaje = entrada.readLine()) != null) {
					if (mensaje.equals("fin")) {
						// Cuando hemos terminado de recibir puntos salimos
						latch.countDown();
						return;
					} else if (mensaje.equals("fin linea")) {
						lastX = -1;
						lastY = -1;
					} else {
						String[] partes = mensaje.split(":");
						if (partes.length == 3) {
							try {
								// Convertir el punto
								int x = Integer.parseInt(partes[0]);
								int y = Integer.parseInt(partes[1]);
								int rgbColor = Integer.parseInt(partes[2]);

				                Graphics g = getGraphics();
				                g.setColor(new Color(rgbColor));

				                if (lastX != -1 && lastY != -1) {
				                    // Dibuja una línea desde la última posición a la nueva
				                    g.drawLine(lastX, lastY, x, y);
				                }

				                // Actualizar las últimas coordenadas
				                lastX = x;
				                lastY = y;

				                g.dispose(); 
							} catch (NumberFormatException e) {
								System.err.println("Error al convertir las coordenadas: " + mensaje);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        // Este método queda vacío porque estamos dibujando directamente
	}
}