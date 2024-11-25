package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;

public class HiloServidor implements Callable<String> {

	private Socket cliente;
	private List<Sala> listaSalas;
	private List<JuegoPinturillo> listaJuegos;

	public HiloServidor(Socket cliente, List<Sala> listaSalas, List<JuegoPinturillo> listaJuegos) {
		this.cliente = cliente;
		this.listaSalas = listaSalas;
		this.listaJuegos = listaJuegos;
	}

	@Override
	public String call() throws Exception {

		try (BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream(), "UTF-8"));
				BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream(), "UTF-8"))) {

			String sala = "";
			int opcion = 0;

			while (opcion != 5) {
				opcion = Integer.parseInt(entrada.readLine());

				switch (opcion) {
				case 1:
					// Ver salas
					salida.write(listaSalas.toString() + "\n");
					salida.flush();

					break;
				case 2:
					// Añadir a sala
					String lineaSala = entrada.readLine();
					if (sala.equals("")) {
						sala = lineaSala;
						boolean yaHayMensaje = false;

						for (Sala salaAux : listaSalas) {
							if (salaAux.getNombre().equals(sala)) {
								if (!salaAux.getListaParticipantes()
										.contains(cliente.getInetAddress() + ":" + cliente.getPort())) {
									if (salaAux.agregarElementos(cliente.getInetAddress() + ":" + cliente.getPort(),
											salida, entrada)) {
										salida.write("Añadido correctamente a la sala\n");
										salida.flush();
										yaHayMensaje = true;
										break;
									} else {
										sala = "";
										salida.write("La sala está llena\n");
										salida.flush();
										yaHayMensaje = true;
										break;
									}
								}
							}
						}
						if (!yaHayMensaje) {
							sala = "";
							salida.write("Sala no válida\n");
							salida.flush();
						}

					} else {
						salida.write("Ya pertenece a una sala\n");
						salida.flush();
					}

					break;
				case 3:
					// Salir de sala
					for (Sala salaAux : listaSalas) {
						if (salaAux.getNombre().equals(sala)) {
							if (salaAux.getListaParticipantes()
									.contains(cliente.getInetAddress() + ":" + cliente.getPort())) {
								salaAux.eliminarElementos(cliente.getInetAddress() + ":" + cliente.getPort(), salida,
										entrada);
								salida.write("Ha salido correctamente de la sala\n");
								sala = "";
							} else {
								salida.write("Fallo al salir de la sala\n");
							}
							salida.flush();
							break;
						} else {
							salida.write("No pertenece a ninguna sala\n");
							salida.flush();
							break;
						}
					}

					break;
				case 4:
					// Iniciar sala
					if (sala.equals("")) {
						salida.write("No pertenece a ninguna sala\n");
						salida.flush();
						break;
					}
					for (Sala salaAux : listaSalas) {
						if (salaAux.getNombre().equals(sala)) {
		            		salida.write("Iniciando partida, esperando a que el resto de participantes esten listos...\n");;
		            		salida.flush();
							// Los hilos esperan hasta que todos los jugadores estan listos
							salaAux.getBarrera().await();
							salaAux.getBarrera().reset();
							JuegoPinturillo juegoPinturillo = listaJuegos.get(Integer.parseInt(sala.split(" ")[1])-1);
							juegoPinturillo.iniciarPartida(listaSalas);
							// Todos los hilos esperan hasta que el hilo principal termine la ejecucion del
							// juego
							break;
						}

					}
					break;
				}
			}
			
			// Como se quiere desconectar hay que eliminarlo de la sala primero
			
			for (Sala salaAux : listaSalas) {
				if (salaAux.getNombre().equals(sala)) {
					if (salaAux.getListaParticipantes()
							.contains(cliente.getInetAddress() + ":" + cliente.getPort())) {
						salaAux.eliminarElementos(cliente.getInetAddress() + ":" + cliente.getPort(), salida,
								entrada);
					}
				}
			}
			

		}

		return "Cliente " + cliente.getInetAddress() + ":" + cliente.getPort() + " desconectado";
	}

}
