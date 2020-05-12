package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	//grafo semplice pesato non orientato
	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer,Airport> idMap;
	private ExtFlightDelaysDAO dao;
	
	//private Map<Integer, Airport> idRotte;
	private List<Rotta> idRotte;
	
	//mappa di padre-figlio: coppie di aeroporti in modo che uno è collegato all'altro
	private Map<Airport, Airport> visita = new HashMap<>();
	
	public Model() {
		
		this.dao = new ExtFlightDelaysDAO();
		
		this.idMap = new HashMap<Integer, Airport>();
		for(Airport a: dao.loadAllAirports()) {
			if(!idMap.containsKey(a.getId())) {
				idMap.put(a.getId(), a);
			}
		}
		
		this.idRotte = dao.getRotte(idMap);
		
	}
	
	//metodo per creare il grafo: riceve dall'esterno dei parametri che implicano la creazione del grafo (numero di compagnie aeree)
	public void creaGrafo(int x) {
		//definisco grafo
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo vertici
		for(Airport a: idMap.values()) {
			//devo prendere gli aereoporti con un numero di compagnie minimo di x
			if(dao.getAirlinesNumber(a) > x) {
				this.grafo.addVertex(a);
			}
			
			//mi faccio dare le rotte da A a B e da B ad A
			for(Rotta r: idRotte) {
				
				//faccio questo controllo perche altrimnti aggiungerei dei vertici che ho escluso
				if(this.grafo.containsVertex(r.getAp()) && this.grafo.containsVertex(r.getAd())) {
					
					DefaultWeightedEdge e = this.grafo.getEdge(r.ap, r.ad);
					
					if(e == null) {
						Graphs.addEdgeWithVertices(this.grafo, r.ap, r.ad, r.getPeso());
					}
					else {
						//faccio la somma dei pesi
						double pesoVecchio = this.grafo.getEdgeWeight(e);
						double pesoNuovo = pesoVecchio + r.getPeso();
						this.grafo.setEdgeWeight(e, pesoNuovo);
					}
				}
				
			}
			
		}
	}
	
	public int contaVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int contaArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public Collection<Airport> getAeroportiGrafo(){
		return this.grafo.vertexSet();
	}
	
	//metodo che mi ritorna una lista di aeroporti che mi coinvolge gli aeroporti: se ritorna null allora gli aeroporti non sono connessi
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new ArrayList<>();
		
		//devo tenere traccia del percorso: definisco una mappa di padre-figlio
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, a1);

		//salvo la radice del mio albero di visita
		visita.put(a1, null);
		
		//mi richiamo la classe anonima, senza crearne una noi
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				//mi devo salvare l'arco fra padre e figlio: prendo nodo sorgente e destinazione
				Airport sorg = grafo.getEdgeSource(e.getEdge());
				Airport dest = grafo.getEdgeTarget(e.getEdge());
				
				//salvo l'albero di visita : da sorgente a destinazione
				if(!visita.containsKey(dest) && visita.containsKey(sorg)) {
					//dest deriva da sorgente
					visita.put(dest, sorg);
				} else {
					if(!visita.containsKey(sorg) && visita.containsKey(dest)) {
						visita.put(sorg, dest);
					}
				}
				
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//ora visito l'albero
		while (it.hasNext()) {
			it.next();
		}
		
		//vedo se gli aeroporti sono collegati o meno
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			//i due aeroporti non sono collegati
			return null;
		}

		Airport step = a2;
		
		//dalla destinazione arrivo alla partenza
		while(!step.equals(a1)) {
			//aggiungo la destinazione (vado a ritroso)
			percorso.add(step);
			//prendo il nodo con cui potevo raggiungere il nodo step fino ad arrivare alla sorgente
			step = visita.get(step);
		}
		
		//aggiungo la partenza
		percorso.add(a1);
		
		return percorso;
		
	}
}
