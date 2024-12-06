package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JuegoPinturillo {
	private final Lock lock = new ReentrantLock(); // Un solo lock para toda la clase
	private static final int VECES_DIBUJAN = 1; // Numero de veces que dibuja cada jugador
	private static final int TAMANIO_SALA = 4; // Numero de jugadores en la sala
	private Sala sala;
	private List<BufferedWriter> listaWriters = new ArrayList<>(TAMANIO_SALA);
	private List<BufferedReader> listaReaders = new ArrayList<>(TAMANIO_SALA);
	private List<Integer> listaPuntuaciones = new ArrayList<>(TAMANIO_SALA);
	private CyclicBarrier barrera = new CyclicBarrier(TAMANIO_SALA);

	public static final List<String> PALABRAS = Arrays.asList("Casa", "Perro", "Avión", "Futbol", "Sol", "Flor",
			"Montaña", "Coche", "Luna", "Pelota", "Madera", "Manzana", "Televisión", "Guitarra", "Elefante", "Tigre",
			"Arbol", "Bicicleta", "Delfín", "Mesa", "Cielo", "Reloj", "Zapato", "Pescado", "Helado", "Reina", "Gato",
			"Ratón", "Cafetera", "Cuchara", "Comida", "Espejo", "Zapato", "Parque", "Escuela", "Comedia", "Robot",
			"Fruta", "Cielo", "Camiseta", "Escalera", "Ventana", "Estrella", "Película", "Hombre", "Mujer", "Niño",
			"Niña", "Música", "Piano", "Silla", "Guitarra", "Bajo", "Conductor", "Viento", "Relámpago", "Corazón",
			"Fuego", "Aire", "Mar", "Montaña", "Almohada", "Libertad", "Amistad", "País", "Fiesta", "Lámpara", "Camión",
			"Nieve", "Foca", "Bajo", "Taza", "Cuerda");

	public JuegoPinturillo(Sala sala) {
		this.sala = sala;
		for (int i = 0; i < TAMANIO_SALA; i++) {
			listaPuntuaciones.add(0);
		}

	}

	public void iniciarPartida() {
		// Solo un hilo puede ejecutar esta parte a la vez (Intentará obtener el lock
		// sin bloquearse, esto asegura una única ejecución de la partida)
		if (lock.tryLock()) {
			try {

				// Cogemos las listas de participantes de la sala
				this.listaWriters = sala.getListaWriters();
				this.listaReaders = sala.getListaReaders();

				System.out.println("Iniciando la partida...");

				int turnoActual = 0; // El jugador que toma el rol de pintor
				Random random = new Random();
				String palabra;
				int vuelta = 1;

				while (turnoActual < TAMANIO_SALA * VECES_DIBUJAN) {
					// Conexion con el pintor
					palabra = PALABRAS.get(random.nextInt(PALABRAS.size()));
					listaWriters.get(turnoActual).write("Te toca dibujar la palabra:" + palabra + "\n");
					listaWriters.get(turnoActual).flush();

					// Conexion con los escritores
					for (int i = 0; i < listaWriters.size(); i++) {
						if (i != turnoActual) {
							listaWriters.get(i).write("Te toca adivinar la palabra:" + palabra + "\n");
							listaWriters.get(i).flush();
						}
					}

					// Bucle de recibimiento y envio de coordenadas del dibujo
					String punto;
					int contador = 0; // Para reducir el número de flush
					while ((punto = listaReaders.get(turnoActual).readLine()) != null) {
						// Conexion con los escritores
						for (int i = 0; i < listaWriters.size(); i++) {
							if (i != turnoActual) {
								listaWriters.get(i).write(punto + "\n");
								
								if(punto.equals("fin") || punto.equals("fin linea")){
									listaWriters.get(i).flush();
								} else if(contador == 90) {
									listaWriters.get(i).flush();
								} else {
									contador++;
								}
							}
						}
						// Si hemos llegado al final salimos del bucle
						if (punto.equals("fin")) {
							break;
						}
					}

					// Guardamos las puntuaciones
					for (int i = 0; i < listaReaders.size(); i++) {
						int puntuacion = Integer.parseInt(listaReaders.get(i).readLine());
						listaPuntuaciones.set(i, listaPuntuaciones.get(i) + puntuacion);
					}

					if (turnoActual == TAMANIO_SALA - 1) {
						// Si completamos una vuelta reseteamos el turnoActual e incrementamos el
						// contador de vueltas
						if (vuelta == VECES_DIBUJAN) {
							// Si hemos terminado todas las vueltas salimos
							break;

						} else {
							// Sino sumamos una vuelta y reiniciamos el turno
							vuelta++;
							turnoActual = 0;
						}
					} else {
						turnoActual++;
					}
				}

				// El juego ha terminado, enviamos las puntuaciones
				List<Integer> puntuacionesOrdenadas = new ArrayList<>(listaPuntuaciones);
				Collections.sort(puntuacionesOrdenadas, Collections.reverseOrder()); // Ordena de mayor a menor
				int posicion = 1; // Empezamos desde la posición 1 para el primer lugar
				for (int i = 0; i < puntuacionesOrdenadas.size(); i++) {
					int puntuacion = puntuacionesOrdenadas.get(i);
					int index = listaPuntuaciones.indexOf(puntuacion);

					listaWriters.get(index).write(
							"Tu puntuacion es " + puntuacion + " y has terminado en la posición " + posicion + "\n");
					listaWriters.get(index).flush();
					System.out.println("Puntuacion enviada");

					posicion++;
				}

				System.out.println("Partida terminada...");

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Liberamos el lock al finalizar
				lock.unlock();
				// Último hilo en alcanzar la barrera
				try {
					barrera.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				barrera.reset();

			}
		} else {
			// Todos los hilos esperan hasta que el hilo principal termine la ejecucion del
			// juego
			try {
				barrera.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}

}
