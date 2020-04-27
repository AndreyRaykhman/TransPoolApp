package classes;

import exception.*;
import jaxb.schema.generated.Path;
import jaxb.schema.generated.Stop;
import jaxb.schema.generated.TransPool;
import jaxb.schema.generated.TransPoolTrip;
import javax.management.InstanceAlreadyExistsException;
import java.util.*;

import static classes.Ride.createRideFromRoads;

public class LogicHandler {
    WorldMap map;
    List<Ride> rides;
    List<TrempRequest> trempRequests;
    Map<String, User> usersNameToObject;

    public LogicHandler() {
        trempRequests = new ArrayList<>();
        usersNameToObject = new HashMap<>();
    }

    public void loadXMLFile(String pathToFile){
        XMLHandler loadXML = new XMLHandler(pathToFile);
        TransPool transPool = loadXML.LoadXML();

        initWorldMap(transPool);


    }

    private void initRoads(TransPool transPool) {

        for (Path path : transPool.getMapDescriptor().getPaths().getPath()) {

            try {
                Road road = new Road(getStationFromName(path.getFrom()), getStationFromName(path.getTo()));
                road.setFuelUsagePerKilometer(path.getFuelConsumption());
                road.setLengthInKM(path.getLength());
                road.setMaxSpeed(path.getSpeedLimit());

                map.addNewRoad(road);
            } catch (InstanceAlreadyExistsException e) {
                e.printStackTrace();
            } catch (StationNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("from: " + path.getFrom() + " to: " + path.getTo());
        }
    }

    private void initStations(TransPool transPool) {

        for (Stop stop : transPool.getMapDescriptor().getStops().getStop()) {
            try {
                map.addNewStation(new Station(new Coordinate(stop.getX(),stop.getY()),stop.getName()));
            } catch (InstanceAlreadyExistsException e) {
                e.printStackTrace();
            } catch (StationNameAlreadyExistsException e) {
                e.printStackTrace();
            } catch (StationAlreadyExistInCoordinate e) {
                e.printStackTrace();
            } catch (StationCoordinateoutOfBoundriesException e) {
                e.printStackTrace();
            }
            System.out.println(stop.getName());
        }
    }

    private void initRides(TransPool transPool) {
        for (TransPoolTrip ride : transPool.getPlannedTrips().getTransPoolTrip()) {
            String routes = "...";
            List<Road> roads;
            List<String> elephantList = Arrays.asList(routes.split(","));
            //createRideFromRoads(ride.getOwner(),ride.getRoute(),)
        }
    }

    private void initWorldMap(TransPool transPool) {
        map = new WorldMap(transPool.getMapDescriptor().getMapBoundries().getWidth(),transPool.getMapDescriptor().getMapBoundries().getLength());

        initStations(transPool);
        initRoads(transPool);
        initRides(transPool);

    }



    public Ride createNewEmptyRide(User rideOwner, List<Road> roads, int capacity){
        Ride newRide = createRideFromRoads(rideOwner, roads, capacity);

//        this.rides.add(newRide);  moved to a seperate func

        return newRide;
    }
    public void addRide(Ride rideToAdd){
        this.rides.add(rideToAdd);
    }

    public TrempRequest createNewEmptyTrempRequest(Station start, Station end) throws NoPathExistBetweenStationsException{
        //TODO: validate path is exist
        if (!start.canReachStation(end)){
            throw new NoPathExistBetweenStationsException();
        }
        TrempRequest newTrempRequest = new TrempRequest(start, end);
//        trempRequests.add(newTrempRequest); moved to a seperate func
        return newTrempRequest;
    }

    public void addTrempRequest(TrempRequest trempRequest){
        trempRequests.add(trempRequest);
    }

    public Station getStationFromName(String name) throws StationNotFoundException {
        return this.map.getStationByName(name);
    }

    public User getUserByName(String name){
        if (!usersNameToObject.containsKey(name)) {
            User user = new User(name);
        }
        return usersNameToObject.get(name);
    }

    public List<Ride> getAllRides(){
        return this.rides;
    }

    public List<TrempRequest> getAllTrempRequests(){
        return this.trempRequests;
    }

}
