package main;

import java.util.Scanner;

public class HiloLeerConsolaAdivinarPalabra extends Thread {

	private static final int TIEMPO_LIMITE = 60000; // 1 minuto en milisegundos
	private final String palabraAdivinar; // La palabra a adivinar
	private int puntuacion; // Puntuación que obtiene el jugador
	private Scanner scanner;

	public HiloLeerConsolaAdivinarPalabra(String palabraAdivinar, Scanner scanner) {
		this.palabraAdivinar = palabraAdivinar;
		this.puntuacion = 0; // Inicializar puntuación
		this.scanner = scanner;
	}

	@Override
	public void run() {
		long tiempoInicio = System.currentTimeMillis();

			while (System.currentTimeMillis() - tiempoInicio < TIEMPO_LIMITE) {
				System.out.print("Escribe tu intento: ");
				String entradaUsuario = scanner.nextLine();
				

				if (entradaUsuario != null) {
					if (entradaUsuario.equalsIgnoreCase(palabraAdivinar)) {
						long tiempoEmpleado = System.currentTimeMillis() - tiempoInicio;
						puntuacion = Math.max(0, 60 - (int) (tiempoEmpleado / 1000)); // Calcula puntuación
						System.out.println("¡Correcto! Has adivinado la palabra.");
						break; // Se ha adivinado la palabra
					} else {
						System.out.println("Incorrecto, sigue intentando.");
					}
				}
			}
		

	}

	public int getPuntuacion() {
		return puntuacion;
	}

}
