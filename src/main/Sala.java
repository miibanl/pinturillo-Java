package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Sala {

	private String nombre;
	private int tamanio = 4;

	private List<String> listaParticipantes = new ArrayList<>(tamanio);
	private List<BufferedWriter> listaWriters = new ArrayList<>(tamanio);
	private List<BufferedReader> listaReaders = new ArrayList<>(tamanio);
	private CyclicBarrier barrera = new CyclicBarrier(tamanio);
	private JuegoPinturillo juegoPinturillo = new JuegoPinturillo(this);

	public Sala(String nombre) {
		this.nombre = nombre;
	}

	public synchronized List<String> getListaParticipantes() {
		return this.listaParticipantes;
	}

	public synchronized List<BufferedWriter> getListaWriters() {
		return this.listaWriters;
	}

	public synchronized List<BufferedReader> getListaReaders() {
		return this.listaReaders;
	}

	public String getNombre() {
		return this.nombre;
	}

	public CyclicBarrier getBarrera() {
		return this.barrera;
	}

	// Método sincronizado para añadir elementos a las dos listas
	public synchronized boolean agregarElementos(String participante, BufferedWriter salida, BufferedReader entrada) {
		if (listaParticipantes.size() >= this.tamanio) {
			return false;
		} else {
			listaParticipantes.add(participante);
			listaWriters.add(salida);
			listaReaders.add(entrada);
			return true;
		}
	}

	// Método sincronizado para eliminar elementos de ambas listas
	public synchronized void eliminarElementos(String participante, BufferedWriter salida, BufferedReader entrada) {
			listaParticipantes.remove(participante);
			listaWriters.remove(salida);
			listaReaders.remove(entrada);
	}
	
	public void iniciarPartida() {
		this.juegoPinturillo.iniciarPartida();
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(nombre+" ");

		if (listaParticipantes != null && !listaParticipantes.isEmpty()) {
			sb.append(String.join(", ", listaParticipantes));
		} else {
			sb.append("No hay participantes ");
		}

		return sb.toString();
	}

}
