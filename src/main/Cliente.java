package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Cliente {

	private static final String HOST = "localhost";
	private static final int PUERTO = 55555;


	public static void main(String[] args) {
		
		
        try(Scanner scanner = new Scanner(System.in);
        	Socket socket = new Socket(HOST,PUERTO);
        	BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
        	BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"))){
        	
	        int opcion=0;
	        String sala="";
						
	        while(opcion!=5) {
		        System.out.println("Selecciona una opción:");
		        System.out.println("1.- Ver las salas disponibles.");
		        System.out.println("2.- Entrar en una sala.");
		        System.out.println("3.- Salir de la sala.");
		        System.out.println("4.- Iniciar la sala.");

		        System.out.println("5.- Salir");
		        try{
		        	opcion = scanner.nextInt();
		        }catch (InputMismatchException e) {
					// No hacer nada
				}
            	scanner.nextLine(); // Hay que consumir el salto de linea

	        	
	        	switch (opcion) {
	            case 1:
	                // Ver las salas
	            	salida.write("1\n");
	            	salida.flush();
	            	System.out.println(entrada.readLine());
	            	
	                break;
	            case 2:
	            	// Entrar en una sala
	            	salida.write("2\n");
	            	salida.flush();
	            	System.out.println("Elige una sala de la lista anterior(escribe el nombre completo de la misma):");
	    	        sala = scanner.nextLine();
	    	        salida.write(sala+"\n");
	            	salida.flush();
	    	        System.out.println(entrada.readLine());
	                break;
	            case 3:
	                // Salir de la sala
	            	salida.write("3\n");
	            	salida.flush();
	    	        System.out.println(entrada.readLine());   	    		
	                break;
	            case 4:
	            	// Iniciar la sala
	            	salida.write("4\n");
	            	salida.flush();
	            	String linea = entrada.readLine();
            		System.out.println(linea);
	            	if(linea.contains("No pertenece a ninguna sala")) {
	            		break;
	            	}
            		while(true) {
            			
		            	linea = entrada.readLine();
		            	if(linea.contains("dibujar")) {
			            	String palabraAdivinar = linea.split(":")[1];
		            		
			            	// Rol de pintor
		            		System.out.println(linea);
		            		
		                    // Para esperar el temporizador
		                    CountDownLatch latch = new CountDownLatch(1);
		                    
		            		PanelPintor panelPintor = new PanelPintor(salida,latch);
		            		// Crear el JFrame para la ventana
		            		JFrame frame = new JFrame("Dibuja la palabra: "+palabraAdivinar);
		            		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		            		frame.setSize(600, 600);
		            		frame.setLayout(new BorderLayout());
		            		frame.add(panelPintor, BorderLayout.CENTER);
		            		frame.setVisible(true);
		            		
		            		// Botones para los colores
		            		crearBotones(frame,panelPintor);
		            		
		            		try {
								latch.await();
						        JOptionPane.showMessageDialog(frame, "¡El tiempo ha terminado!", "Tiempo agotado", JOptionPane.INFORMATION_MESSAGE);
								frame.dispose();
								// El pintor también envia puntuación para que la partida no siga sin él
								salida.write(0+"\n");
								salida.flush();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		            		
		            	}else if (linea.contains("adivinar")){
			            	String palabraAdivinar = linea.split(":")[1];

			            	// Rol de escritor
		            		
		            		// Para esperar hasta que termine de recibir puntos
		                    CountDownLatch latch = new CountDownLatch(1);

		            		PanelReceptor panelReceptor = new PanelReceptor(entrada, latch);
		            		// Crear el JFrame para la ventana
		            		JFrame frame = new JFrame("Adivina la palabra");
		            		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		            		frame.setSize(600, 600);
		            		frame.setLayout(new BorderLayout());
		            		frame.add(panelReceptor, BorderLayout.CENTER);
		            		frame.setVisible(true);
		            		try {
		            			// Hilo para recibir las palabras del usuario
		            			HiloAdivinarPalabra hiloAdivinarPalabra = new HiloAdivinarPalabra(palabraAdivinar,scanner);
		            			hiloAdivinarPalabra.start();
		            			hiloAdivinarPalabra.join();
								latch.await();
						        JOptionPane.showMessageDialog(frame, "¡El tiempo ha terminado!", "Tiempo agotado", JOptionPane.INFORMATION_MESSAGE);
								frame.dispose();
								// Cuando ha terminado todo enviamos la puntuacion al servidor
								salida.write(hiloAdivinarPalabra.getPuntuacion()+"\n");
								salida.flush();
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		            		
		            	} else {
		            		// Partida terminada
		            		System.out.println("Partida terminada");
		            		System.out.println(linea);
		            		break;
		            	}	
	            	}

	            	break;
	            case 5:
	                // Salir del programa
	            	salida.write("5\n");
	            	salida.flush();
	                System.out.println("Saliendo del programa...");
	                break;
	            default:
	                System.out.println("Opción no válida. Inténtalo de nuevo.");
	                break;
	                
	        	}
	        }			
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void crearBotones(JFrame frame, PanelPintor panelPintor) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		// Crear botones
		JButton blackButton = new JButton("Negro");
		JButton redButton = new JButton("Rojo");
		JButton blueButton = new JButton("Azul");
		JButton greenButton = new JButton("Verde");

		// Poner los colores
		blackButton.setBackground(Color.BLACK);
		blackButton.setForeground(Color.WHITE);
		redButton.setBackground(Color.RED);
		redButton.setForeground(Color.WHITE);
		blueButton.setBackground(Color.BLUE);
		blueButton.setForeground(Color.WHITE);
		greenButton.setBackground(Color.GREEN);
		greenButton.setForeground(Color.BLACK);

		// Añadir botones al panel de botones
		buttonPanel.add(blackButton);
		buttonPanel.add(redButton);
		buttonPanel.add(blueButton);
		buttonPanel.add(greenButton);

		// Añadir el panel de botones al JFrame
		frame.add(buttonPanel, BorderLayout.SOUTH);
		
		blackButton.addActionListener(e -> panelPintor.setColor(Color.BLACK));
		redButton.addActionListener(e -> panelPintor.setColor(Color.RED));
		blueButton.addActionListener(e -> panelPintor.setColor(Color.BLUE));
		greenButton.addActionListener(e -> panelPintor.setColor(Color.GREEN));
	}
	
}

        


