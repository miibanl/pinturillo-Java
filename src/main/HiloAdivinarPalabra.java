package main;

import java.util.Scanner;

public class HiloAdivinarPalabra extends Thread {
	private final String palabraAdivinar;
	private int puntuacion;
	private Scanner scanner;

	public HiloAdivinarPalabra(String palabraAdivinar, Scanner scanner) {
		this.palabraAdivinar = palabraAdivinar;
		this.puntuacion = 0;
		this.scanner = scanner;
	}

	@Override
	public void run() {
			System.out.println("¡Juego iniciado! Tienes 1 minuto para adivinar la palabra.");			

			HiloLeerConsolaAdivinarPalabra hiloLeerConsolaAdivinarPalabra = new HiloLeerConsolaAdivinarPalabra(palabraAdivinar, scanner);
			hiloLeerConsolaAdivinarPalabra.start();
			

			try {
			    Thread.sleep(60000); // Duerme durante 1 minuto
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
			
			puntuacion = hiloLeerConsolaAdivinarPalabra.getPuntuacion();

			if(puntuacion==0) {
				// Informamos que se ha acabado y esperamos a que el hilo se termine 
				System.out.println("\nSe ha terminado el tiempo. Pulsa cualquier tecla para continuar el juego");
				try {
					hiloLeerConsolaAdivinarPalabra.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Puntuación conseguida: "+puntuacion);
	}

	public int getPuntuacion() {
		return puntuacion;
	}
}
