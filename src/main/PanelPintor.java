package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

public class PanelPintor extends JPanel {

	private static final long serialVersionUID = -5574969337552916072L;
	private int lastX = -1, lastY = -1;
    private Color colorActual = Color.BLACK;
    private final CountDownLatch latch;

    public PanelPintor(BufferedWriter salida, CountDownLatch latch) {
        this.latch = latch;

        // Para dibujar mientras el ratón se mueve
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                Graphics g = getGraphics();
                g.setColor(colorActual);

                if (lastX != -1 && lastY != -1) {
                    // Dibuja una línea desde la última posición a la nueva
                    g.drawLine(lastX, lastY, x, y);
                }

                // Actualizar las últimas coordenadas
                lastX = x;
                lastY = y;

                g.dispose();

                // Enviar las coordenadas al servidor
                try {
                    salida.write(x + ":" + y + ":" + colorActual.getRGB() + "\n");
                    salida.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // Para manejar cuando se suelta el botón del ratón
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // Restablecer las últimas coordenadas para separar trazos
                lastX = -1;
                lastY = -1;
                try {
                    salida.write("fin linea\n");
                    salida.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // Timer para detener el dibujo después de 1 minuto
        Timer timer = new Timer(60000, e -> finDibujo(salida)); // 60000 ms = 1 minuto
        timer.setRepeats(false); // Solo una vez
        timer.start();
    }

    private void finDibujo(BufferedWriter salida) {
        try {
            salida.write("fin\n");
            salida.flush();
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cambiar el color actual
    public void setColor(Color color) {
        this.colorActual = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Este método queda vacío porque estamos dibujando directamente
    }
}
