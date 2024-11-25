package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Sala {

	private String nombre;
	private int tamanio = 4;

	private List<String> listaParticipantes;
	private List<BufferedWriter> listaWriters;
	private List<BufferedReader> listaReaders;
	private CyclicBarrier barrera = new CyclicBarrier(tamanio);

	public Sala(String nombre) {
		this.nombre = nombre;
		this.listaParticipantes = new ArrayList<>(tamanio);
		this.listaWriters = new ArrayList<>(tamanio);
		this.listaReaders = new ArrayList<>(tamanio);
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
