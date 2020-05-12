package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports() {
		String sql = "SELECT * FROM airports";
		List<Airport> result = new ArrayList<Airport>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				result.add(airport);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	//creo un metodo che mi dia x compagnie aeree
	public int getAirlinesNumber(Airport a) {
		String sql = "SELECT COUNT(DISTINCT airline_id) AS tot" + 
				" FROM flights" + 
				" WHERE (ORIGIN_AIRPORT_ID = ? ) || (DESTINATION_AIRPORT_ID = ? )";
		int ris =0;
		
		try {
			Connection con = ConnectDB.getConnection();
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, a.getId());
			st.setInt(2, a.getId());
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				ris = res.getInt("tot");
			}
			
			con.close();
			
			return ris;
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	public List<Rotta> getRotte(Map<Integer, Airport> idMap) {
		String sql = " SELECT ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID, COUNT(*) AS tot"
				+" FROM flights"
				+" GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID";
		List<Rotta> lista = new ArrayList<>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				Airport sorg = idMap.get(res.getInt("ORIGIN_AIRPORT_ID"));
				Airport dest = idMap.get(res.getInt("DESTINATION_AIRPORT_ID"));
				if (sorg != null &&  dest!= null) {
					lista.add(new Rotta (sorg, dest, res.getInt("tot")));
				}else {
					System.out.println("ERRORE NELLE ROTTE.");
				}
			}
			
			conn.close();
			
			return lista;
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
