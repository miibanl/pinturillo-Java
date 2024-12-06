package main;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;


public class Servidor {

	private static final int PUERTO = 55555;
	private static final int NUMERO_HILOS = 10;




	public static void main(String[] args) {
		try (ServerSocket servidor = new ServerSocket(PUERTO)) {
			System.out.println("Servidor escuchando en el puerto " + PUERTO);
			ExecutorService poolDeHilos = Executors.newFixedThreadPool(NUMERO_HILOS);
			ExecutorService poolResultados = Executors.newSingleThreadExecutor(); // Para manejar resultados
			Sala sala1 = new Sala("Sala 1");
			Sala sala2 = new Sala("Sala 2");
			Sala sala3 = new Sala("Sala 3");
			Sala sala4 = new Sala("Sala 4");
			List<Sala> listaSalas = new ArrayList<>();
			listaSalas.add(sala1);
			listaSalas.add(sala2);
			listaSalas.add(sala3);
			listaSalas.add(sala4);

			

			while (true) {

				Socket cliente = servidor.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress()+":"+cliente.getPort());

				HiloServidor tarea = new HiloServidor(cliente, listaSalas);

				Future<String> resultado = poolDeHilos.submit(tarea);
				// Manejar el resultado en otro hilo
				poolResultados.submit(() -> {
					try {
						String resultadoTarea = resultado.get(); // Bloquea aquí solo este hilo
						System.out.println("Resultado de la tarea: " + resultadoTarea);
					} catch (RejectedExecutionException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				});

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}





}
